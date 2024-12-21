package com.pmi.tpd.database.jpa;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.api.paging.IFilterable;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;

/**
 * Default Spring JPA implementation using QueryDSL and implementing specific methods to manipulate {@link IFilterable}
 * page request.
 *
 * @author Christophe Friederich
 * @since 1.3
 * @param <T>
 *                the type of entity to handle
 * @param <ID>the
 *                type of entity identifier.
 */
public abstract class DefaultJpaRepository<T, ID extends Serializable> extends QueryDslJpaRepository<T, ID>
        implements IDslAccessor<T, ID> {

    /**
     * @param domainClass
     *                             the domain class associated to this repository.
     * @param entityManagerFactory
     *                             the JPA the entity manager factory for the persistence unit.
     */
    @SuppressWarnings("unchecked")
    public DefaultJpaRepository(final Class<T> domainClass, final EntityManager entityManager) {
        this((JpaEntityInformation<T, ID>) JpaEntityInformationSupport.getEntityInformation(domainClass, entityManager),
                entityManager);
    }

    /**
     * @param entityInformation
     *                          the JPA specific information about associated domain class (see
     *                          {@link #getMetadata(Class, EntityManagerFactory)} for information).
     * @param entityManager
     *                          the JPA the entity manager for the persistence unit.
     */
    public DefaultJpaRepository(final JpaEntityInformation<T, ID> entityInformation,
            final EntityManager entityManager) {
        this(entityInformation, entityManager, DEFAULT_ENTITY_PATH_RESOLVER);
    }

    /**
     * @param entityInformation
     *                          the JPA specific information about associated domain class (see
     *                          {@link #getMetadata(Class, entityManager)} for information).
     * @param entityManager
     *                          the JPA the entity manager for the persistence unit.
     * @param resolver
     *                          the specific {@link EntityPathResolver} resolver (default
     *                          {@link SimpleEntityPathResolver#INSTANCE})
     */
    public DefaultJpaRepository(final JpaEntityInformation<T, ID> entityInformation, final EntityManager entityManager,
            final EntityPathResolver resolver) {
        super(entityInformation, entityManager, resolver);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JPQLQuery<T> from() {
        return from(entity());
    }

    /**
     * @param entity
     *               the entity to use.
     * @return Returns new instance of {@link JPAQuery} for entity.
     * @param <E>
     *            result type
     */
    @Nonnull
    public <E> JPQLQuery<E> from(final EntityPathBase<E> entity) {
        return new JPAQuery<E>(entityManager).from(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JPADeleteClause deleteFrom() {
        return deleteFrom(entity());
    }

    /**
     * @param entity
     *               the entity to use.
     * @return Returns new instance of {@link JPADeleteClause} for entity.
     * @param <E>
     *            result type
     */
    @Nonnull
    public <E> JPADeleteClause deleteFrom(final EntityPathBase<E> entity) {
        return new JPADeleteClause(entityManager, entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JPQLQuery<T> pageQuery(final @Nonnull Pageable request) {
        return querydsl().applyPagination(request, from());
    }

    @Override
    @Nonnull
    public <R> R detach(@Nonnull final R entity) {
        Objects.requireNonNull(entity, "entity is required");
        entityManager.detach(entity);
        return entity;
    }

    @Override
    public <R> void refresh(@Nonnull final R entity) {
        Objects.requireNonNull(entity, "entity is required");
        entityManager.refresh(entity);

    }

    /**
     * @param <E>
     * @param query
     * @return
     */
    @Nonnull
    @Override
    public Stream<T> stream(@Nonnull final Pageable pageable) {
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, entity());
        JPQLQuery<T> query = createQuery(predicates);
        query = querydsl().applySorting(pageable.getSort(), query);
        return query.stream();
    }

}
