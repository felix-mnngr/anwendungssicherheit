package de.hsaalen.cloudcomputing.business;

import com.google.cloud.bigtable.data.v2.models.Filters;
import com.google.protobuf.Timestamp;
import de.hsaalen.cloudcomputing.*;
import de.hsaalen.cloudcomputing.domain.Konto;
import de.hsaalen.cloudcomputing.repository.Repository;
import de.hsaalen.cloudcomputing.repository.RepositoryUtils;
import de.hsaalen.cloudcomputing.repository.annotation.Column;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Singleton
@GrpcService
public class KontoService implements KontoGprc {

    private static final String NOT_FOUND = "Konto mit der angegebenen ID nicht gefunden.";
    private static final String NOT_CREATED = "Es konnte kein Konto fuer den den Benutzer angelegt werden.";
    private static final String NOT_PUT = "Die Kontobewegung konnte nicht persistiert werden.";
    private static final String NOT_DELETED = "Das Konto konnte nicht geloescht werden.";
    private static final String NOT_ALLOWED = "Der Benutzer hat keine Berechtigungen fuer dieses Konto.";

    @Inject
    Repository<Konto> repository;

    private static Optional<KontoReply> buildKontoReplyOptional(Optional<Konto> kontoOptional) {
        return kontoOptional.map(konto -> KontoReply.newBuilder()
                .setUuid(konto.getUuid().toString())
                .setEmail(konto.getEmailKontoinhaber())
                .setBeschreibung(konto.getBeschreibung())
                .setKontostand(konto.getKontostand() != null ? konto.getKontostand().doubleValue() : 0)
                .build());
    }

    private static KontoReply buildKontoReply(Konto konto) {
        return buildKontoReplyOptional(Optional.ofNullable(konto)).orElse(KontoReply.newBuilder().build());
    }

    @Override
    public Uni<KontoReply> getKontoById(KontoGetRequest request) {
        UUID uuid = UUID.fromString(request.getUuid());
        assertUserHasAccess(request.getEmail(), uuid);
        return Uni.createFrom().item(buildKontoReplyOptional(repository.findById(uuid))
                .orElseThrow(() -> new StatusRuntimeException(Status.NOT_FOUND.withDescription(NOT_FOUND))));
    }

    @Override
    public Multi<KontoBewegungReply> getKontoBewegungenById(KontoGetRequest request) {
        UUID uuid = UUID.fromString(request.getUuid());
        assertUserHasAccess(request.getEmail(), uuid);
        return Multi.createFrom().items(repository.findByIdWithFilter(uuid, Filters.FILTERS.pass()).map(konto -> Optional.ofNullable(konto.getKontobewegung()).orElse(Collections.emptyList())
                .stream().map(kontobewegung -> KontoBewegungReply.newBuilder()
                        .setBeschreibung(kontobewegung.getBeschreibung())
                        .setBetrag(kontobewegung.getBetrag().doubleValue())
                        .setTimestamp(Timestamp.newBuilder()
                                .setSeconds(kontobewegung.getZeitpunkt().toEpochSecond(ZoneOffset.UTC))
                                .setNanos(kontobewegung.getZeitpunkt().getNano())
                                .build())
                        .build())
        ).orElse(Stream.of(KontoBewegungReply.newBuilder().build())));
    }

    @Override
    public Multi<KontoReply> getKontenByEmail(KontenGetRequest request) {
        return Multi.createFrom().items(repository.findByFilter(
                Filters.FILTERS.condition(Filters.FILTERS
                                .chain()
                                .filter(Filters.FILTERS.qualifier().exactMatch(Konto.EMAIL_KONTOINHABER_PROPERTY))
                                .filter(Filters.FILTERS.value().exactMatch(RepositoryUtils.mapObjectToByteArrayString(request.getEmail()))))
                        .then(Filters.FILTERS.family().exactMatch(Column.DEFAULT_FAMILY))
        ).stream().map(KontoService::buildKontoReply));
    }

    @Override
    public Uni<KontoReply> createKonto(KontoCreateRequest request) {
        try {
            return Uni.createFrom().item(buildKontoReply(repository.create(new Konto(request.getEmail(), request.getBeschreibung()))));
        } catch (Exception e) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(NOT_CREATED).withCause(e));
        }

    }

    @Override
    public Uni<KontoReply> addKontobewegung(KontoPutRequest request) {
        try {
            UUID uuid = UUID.fromString(request.getUuid());
            assertUserHasAccess(request.getEmail(), uuid);
            Konto konto = repository.findById(uuid).orElseThrow(IllegalStateException::new);
            BigDecimal betrag = BigDecimal.valueOf(request.getBetrag());
            if (konto.getKontobewegung() == null) {
                konto.setKontobewegung(new ArrayList<>());
            }
            konto.getKontobewegung().add(new Konto.Kontobewegung(betrag, request.getBeschreibung(), LocalDateTime.now()));
            if (konto.getKontostand() == null) {
                konto.setKontostand(BigDecimal.ZERO);
            }
            konto.setKontostand(konto.getKontostand().add(betrag));
            repository.update(konto);
            return Uni.createFrom().item(buildKontoReply(null));
        } catch (Exception e) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(NOT_PUT).withCause(e));
        }
    }

    @Override
    public Uni<KontoReply> deleteKonto(KontoDeleteRequest request) {
        try {
            UUID uuid = UUID.fromString(request.getUuid());
            assertUserHasAccess(request.getEmail(), uuid);
            repository.deleteById(uuid);
            return Uni.createFrom().item(buildKontoReply(null));
        } catch (Exception e) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(NOT_DELETED).withCause(e));
        }
    }

    private void assertUserHasAccess(String email, UUID uuid) {
        repository.findByIdWithFilter(uuid, Filters.FILTERS
                .chain()
                .filter(Filters.FILTERS.qualifier().exactMatch(Konto.EMAIL_KONTOINHABER_PROPERTY))
                .filter(Filters.FILTERS.value().exactMatch(RepositoryUtils.mapObjectToByteArrayString(email)))).orElseThrow(() -> new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription(NOT_ALLOWED))
        );
    }
}
