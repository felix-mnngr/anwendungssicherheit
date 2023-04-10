package de.hsaalen.cloudcomputing.repository;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient;
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings;
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest;
import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.BigtableDataSettings;
import com.google.cloud.bigtable.data.v2.models.*;
import com.google.protobuf.ByteString;
import de.hsaalen.cloudcomputing.repository.annotation.Column;
import de.hsaalen.cloudcomputing.repository.annotation.Entity;
import de.hsaalen.cloudcomputing.repository.annotation.OneToMany;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@ApplicationScoped
public class RepositoryProducer {

    @ConfigProperty(name = "big-table-project-id")
    String projectId;
    @ConfigProperty(name = "big-table-instance-id")
    String instanceId;

    private BigtableDataClient dataClient;

    private static void createTable(BigtableTableAdminClient adminClient, String tableId, List<String> families) {
        if (!adminClient.exists(tableId)) {
            CreateTableRequest createTableRequest = CreateTableRequest.of(tableId);
            families.forEach(createTableRequest::addFamily);
            adminClient.createTable(createTableRequest);
        }
    }

    private static String oneToManyFamily(OneToMany annotation) {
        return RepositoryImpl.ONE_TO_MANY_PREFIX + annotation.family();
    }

    @PostConstruct
    void createDataClient() {
        try {
            dataClient = BigtableDataClient.create(BigtableDataSettings.newBuilder().setProjectId(projectId).setInstanceId(instanceId).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    void closeDataClient() {
        assertDataClientIsActive();
        dataClient.close();
    }

    private void assertDataClientIsActive() {
        if (dataClient == null) {
            throw new RuntimeException("No active Data-Client found!");
        }
    }

    @Produces
    @SuppressWarnings({"unchecked", "Optional.ifPresent"})
    public <T extends AbstractEntity> Repository<T> produceRepository(InjectionPoint injectionPoint) {
        assertDataClientIsActive();
        BigtableTableAdminSettings adminSettings;
        try {
            adminSettings = BigtableTableAdminSettings.newBuilder()
                    .setProjectId(projectId)
                    .setInstanceId(instanceId)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (BigtableTableAdminClient adminClient = BigtableTableAdminClient.create(adminSettings)) {
            ParameterizedType repositoryType = (ParameterizedType) injectionPoint.getType();

            if (!(repositoryType.getActualTypeArguments()[0] instanceof Class)) {
                throw new IllegalStateException(repositoryType + " does not specify an entity class");
            }

            Class<T> entityClass = (Class<T>) repositoryType.getActualTypeArguments()[0];
            Entity entityMetadata = Optional.ofNullable(entityClass.getAnnotation(Entity.class))
                    .orElseThrow(() -> new IllegalStateException("The generic Type of Repository must be annotated as Entity."));
            List<String> families = Arrays.stream(entityClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(OneToMany.class))
                    .map(f -> Optional.ofNullable(f.getAnnotation(Column.class)).map(Column::family).orElse(Optional.ofNullable(f.getAnnotation(OneToMany.class)).map(RepositoryProducer::oneToManyFamily).orElse(null)))
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            String tableId = entityMetadata.id().isEmpty() ? entityClass.getSimpleName() : entityMetadata.id();
            createTable(adminClient, tableId, families);
            adminClient.close();
            return new RepositoryImpl<>(dataClient, entityClass, tableId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record RepositoryImpl<T extends AbstractEntity>(BigtableDataClient dataClient, Class<T> entityClass,
                                                            String tableId) implements Repository<T> {
        private static final String ONE_TO_MANY_PREFIX = "one-to-many-";

        private static ByteString getColumnName(Field column) {
            return ByteString.copyFromUtf8(column.getAnnotation(Column.class).qualifier().isEmpty() ? column.getName() : column.getAnnotation(Column.class).qualifier());
        }

        private static String getColumnFamily(Field column) {
            return column.isAnnotationPresent(Column.class) ? column.getAnnotation(Column.class).family() : ONE_TO_MANY_PREFIX + column.getAnnotation(OneToMany.class).family();
        }

        private static <T> ByteString getColumnValue(Field column, T entity) {
            try {
                column.setAccessible(true);
                return RepositoryUtils.mapObjectToByteArrayString(column.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private static List<Object> addOneTwoMany(Map<String, List<Object>> oneToManyColumns, String key, Object objToAdd) {
            oneToManyColumns.computeIfAbsent(key, k -> new ArrayList<>());
            oneToManyColumns.get(key).add(objToAdd);
            return oneToManyColumns.get(key);
        }

        @Override
        public Optional<T> findById(UUID uuid) {
            return findByFilter(
                    Filters.FILTERS.chain()
                            .filter(Filters.FILTERS.key().exactMatch(uuid.toString()))
                            .filter(Filters.FILTERS.family().exactMatch(Column.DEFAULT_FAMILY))
            ).stream().findFirst();
        }

        @Override
        public Optional<T> findByIdWithFilter(UUID uuid, Filters.Filter filter) {
            return findByFilter(
                    Filters.FILTERS.condition(Filters.FILTERS.key().exactMatch(uuid.toString()))
                            .then(filter)
            ).stream().findFirst();
        }

        @Override
        public List<T> findByFilter(Filters.Filter filter) {
            return StreamSupport.stream(dataClient.readRows(Query.create(tableId).filter(filter)).spliterator(), false).map(this::mapRowToEntity).toList();
        }

        @Override
        public T update(T entity) {
            RowMutation rowMutation = RowMutation.create(tableId, entity.getUuid().toString());
            getCells(entity).forEach(cell -> rowMutation.setCell(cell.familyName, cell.columnName, cell.value));
            try {
                dataClient.mutateRow(rowMutation);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            return entity;
        }

        @Override
        public T create(T entity) {
            boolean foundUnusedID = false;
            while (!foundUnusedID) {
                entity.setUuid(UUID.randomUUID());
                foundUnusedID = findById(entity.getUuid()).isEmpty();
            }
            return update(entity);
        }

        @Override
        public void deleteById(UUID uuid) {
            dataClient.mutateRow(RowMutation.create(tableId, uuid.toString()).deleteRow());
        }

        private List<Cell> getCells(T entity) {
            return Stream.concat(getOneToManyCells(entity), getSimpleCells(entity)).toList();
        }

        private Stream<Cell> getSimpleCells(T entity) {
            return getSimpleColumnDefs().map(f -> new Cell(getColumnFamily(f), getColumnName(f), getColumnValue(f, entity)));
        }

        private Stream<Field> getSimpleColumnDefs() {
            return Arrays.stream(entityClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Column.class));
        }

        @SuppressWarnings("unchecked")
        private Stream<Cell> getOneToManyCells(T entity) {
            return getOneToManyColumnDefs().flatMap(f -> {
                try {
                    f.setAccessible(true);
                    List<Object> values = Optional.ofNullable(((List<Object>) f.get(entity))).orElse(new ArrayList<>());
                    return IntStream.range(0, values.size()).mapToObj(i -> new Cell(ONE_TO_MANY_PREFIX + f.getAnnotation(OneToMany.class).family(),
                            ByteString.copyFromUtf8(f.getAnnotation(OneToMany.class).columnNamePrefix() + i), RepositoryUtils.mapObjectToByteArrayString(values.get(i))));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private Stream<Field> getOneToManyColumnDefs() {
            return Arrays.stream(entityClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(OneToMany.class));
        }

        private T mapRowToEntity(Row row) {
            try {
                T entity = entityClass.getConstructor().newInstance();
                Map<String, List<Object>> oneToManyColumns = new HashMap<>();
                Map<Field, Long> timestamps = new HashMap<>();
                entity.setUuid(UUID.fromString(row.getKey().toStringUtf8()));
                row.getCells().forEach(rowCell -> mapRowCellToEntity(rowCell, entity, oneToManyColumns, timestamps));
                return entity;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new IllegalStateException(String.format("Entity %s has needs accessible No-Args-Constructor but has not.", entityClass));
            }
        }

        private void mapRowCellToEntity(RowCell rowCell, T entity, Map<String, List<Object>> oneToManyColumns, Map<Field, Long> timestamps) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(rowCell.getValue().toByteArray()); ObjectInputStream in = new ObjectInputStream(bis)) {
                Supplier<RuntimeException> orElseThrowClause = () -> new IllegalStateException(String.format("No column definition found for %s in Entity %s", rowCell.getQualifier().toStringUtf8(), entityClass.getName()));
                Field column = rowCell.getFamily().contains(ONE_TO_MANY_PREFIX) ?
                        getOneToManyColumnDefs().filter(col -> getColumnFamily(col).equals(rowCell.getFamily())).findFirst().orElseThrow(orElseThrowClause) :
                        getSimpleColumnDefs().filter(col -> getColumnName(col).equals(rowCell.getQualifier())).findFirst().orElseThrow(orElseThrowClause);
                Object object = column.isAnnotationPresent(OneToMany.class) ? addOneTwoMany(oneToManyColumns, rowCell.getFamily(), in.readObject()) : in.readObject();
                column.setAccessible(true);
                if (timestamps.get(column) == null || timestamps.get(column) < rowCell.getTimestamp()) {
                    column.set(entity, object);
                    timestamps.put(column, rowCell.getTimestamp());
                }
            } catch (IOException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        private record Cell(String familyName, ByteString columnName, ByteString value) {
        }

    }
}
