package de.hsaalen.cloudcomputing.repository;

import com.google.cloud.bigtable.data.v2.models.Filters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T extends AbstractEntity> {

    Optional<T> findById(UUID uuid);

    Optional<T> findByIdWithFilter(UUID uuid, Filters.Filter filter);

    List<T> findByFilter(Filters.Filter filter);

    T update(T entity);

    T create(T entity);

    void deleteById(UUID uuid);

}
