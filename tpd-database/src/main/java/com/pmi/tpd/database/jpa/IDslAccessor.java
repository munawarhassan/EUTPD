package com.pmi.tpd.database.jpa;

import java.io.Serializable;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;

/**
 * @author Christophe Friederich
 * @since 1.3
 * @param <T>
 */
public interface IDslAccessor<T, ID extends Serializable>
        extends QuerydslPredicateExecutor<T>, JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * @return
     */
    @Nonnull
    EntityPathBase<T> entity();

    /**
     * @return
     */
    @Nonnull
    JPQLQuery<T> from();

    /**
     * @return
     */
    @Nonnull
    JPADeleteClause deleteFrom();

    /**
     * @param request
     * @param entityPath
     * @return
     */
    @Nonnull
    JPQLQuery<T> pageQuery(@Nonnull final Pageable request);

    /**
     * @param request
     * @param entityPath
     * @return
     */
    @Nonnull
    Stream<T> stream(@Nonnull final Pageable request);

    /**
     * @param entity
     * @return
     */
    @Nonnull
    <R> R detach(@Nonnull final R entity);

    /**
     * @param <R>
     * @param entity
     */
    <R> void refresh(@Nonnull final R entity);
}
