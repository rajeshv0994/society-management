package com.societyapp.application.port.out.persistence;

import java.util.List;
import java.util.Optional;

/** Framework-free persistence contract shared by application output ports. */
public interface CrudRepositoryPort<T, ID> {
    <S extends T> S save(S entity);

    <S extends T> List<S> saveAll(Iterable<S> entities);

    <S extends T> S saveAndFlush(S entity);

    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findAllById(Iterable<ID> ids);

    boolean existsById(ID id);

    long count();

    void delete(T entity);

    void deleteById(ID id);

    void deleteAll();

    void flush();
}
