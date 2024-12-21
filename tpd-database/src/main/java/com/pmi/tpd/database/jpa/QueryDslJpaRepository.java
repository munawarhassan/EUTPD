package com.pmi.tpd.database.jpa;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaPredicateExecutor;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.support.PageableExecutionUtils;

import com.pmi.tpd.api.paging.DslPagingHelper;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;

/**
 * QueryDsl specific extension of {@link SimpleJpaRepository} which adds implementation for
 * {@link QueryDslPredicateExecutor}.
 *
 * @param <T>
 *             the type of entity to handle
 * @param <ID>
 *             the type of entity identifier.
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public class QueryDslJpaRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
        implements QuerydslPredicateExecutor<T> {

    /** */
    protected static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

    /** */
    private final EntityPath<T> path;

    /** */
    private final PathBuilder<T> builder;

    /** */
    protected final EntityManager entityManager;

    private QuerydslJpaPredicateExecutor<T> executor;

    /**
     * Creates a new {@link QueryDslJpaRepository} from the given domain class and {@link EntityManager}. This will use
     * the {@link SimpleEntityPathResolver} to translate the given domain class into an {@link EntityPath}.
     *
     * @param entityInformation
     *                             must not be {@literal null}.
     * @param entityManagerFactory
     *                             must not be {@literal null}.
     */
    public QueryDslJpaRepository(final JpaEntityInformation<T, ID> entityInformation,
            final EntityManager entityManager) {
        this(entityInformation, entityManager, DEFAULT_ENTITY_PATH_RESOLVER);
    }

    /**
     * Creates a new {@link QueryDslJpaRepository} from the given domain class and {@link entityManager} and uses the
     * given {@link EntityPathResolver} to translate the domain class into an {@link EntityPath}.
     *
     * @param entityInformation
     *                          must not be {@literal null}.
     * @param entityManager
     *                          must not be {@literal null}.
     * @param resolver
     *                          must not be {@literal null}.
     */
    public QueryDslJpaRepository(final JpaEntityInformation<T, ID> entityInformation, final EntityManager entityManager,
            final EntityPathResolver resolver) {

        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.path = resolver.createPath(entityInformation.getJavaType());
        this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
        this.executor = new QuerydslJpaPredicateExecutor<T>(entityInformation, entityManager, resolver, null);
    }

    /**
     * @return Create a new instance of {@link Querydsl}.
     */
    protected Querydsl querydsl() {
        return new Querydsl(entityManager, builder);
    }

    /**
     * @return Returns the {@link PathBuilder} associated to this repository.
     */
    protected PathBuilder<T> pathBuilder() {
        return builder;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findOne(com.mysema.query.types.Predicate)
     */
    @Override
    public Optional<T> findOne(final Predicate predicate) {
        return this.executor.findOne(predicate);
    }

    @Override
    public <S extends T, R> R findBy(final Predicate predicate,
        final Function<FetchableFluentQuery<S>, R> queryFunction) {
        return executor.findBy(predicate, queryFunction);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate)
     */
    @Override
    public List<T> findAll(final Predicate predicate) {
        return this.executor.findAll(predicate);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate,
     *      com.mysema.query.types.OrderSpecifier<?>[])
     */
    @Override
    public List<T> findAll(final Predicate predicate, final OrderSpecifier<?>... orders) {
        return this.executor.findAll(predicate, orders);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.Predicate,
     *      org.springframework.data.domain.Sort)
     */
    @Override
    public List<T> findAll(final Predicate predicate, final Sort sort) {
        return this.executor.findAll(predicate, sort);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#findAll(com.mysema.query.types.OrderSpecifier[])
     */
    @Override
    public List<T> findAll(final OrderSpecifier<?>... orders) {
        return this.executor.findAll(orders);
    }

    @Override
    public Page<T> findAll(final Predicate predicate, @Nonnull final Pageable pageable) {
        checkNotNull(pageable, "pageable");
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, path, predicate);
        return toPage(createQuery(predicates), pageable);

    }

    @Override
    public Page<T> findAll(@Nonnull final Pageable pageable) {
        checkNotNull(pageable, "pageable");
        if (pageable.isUnpaged()) {
            return new PageImpl<>(findAll());
        }
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, path);
        return toPage(createQuery(predicates), pageable);

    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#count(com.mysema.query.types.Predicate)
     */
    @Override
    public long count(final Predicate predicate) {
        return this.executor.count(predicate);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.springframework.data.querydsl.QueryDslPredicateExecutor#exists(com.mysema.query.types.Predicate)
     */
    @Override
    public boolean exists(final Predicate predicate) {
        return this.executor.exists(predicate);
    }

    /**
     * Execute the {@code query} and create a {@link Page} corresponding to {@link Pageable pageable} parameter.
     *
     * @param query
     *                 the query to execute.
     * @param pageable
     *                 the pagination request.
     * @return Returns the requested page of {@code E}, which may be empty but never {@code null}.
     * @param <E>
     *            the entity to query.
     */
    @Nonnull
    public <E> Page<E> toPage(@Nonnull JPQLQuery<E> query, @Nonnull final Pageable pageable) {
        query = querydsl().applyPagination(pageable, query);
        return PageableExecutionUtils.getPage(query.fetch(), pageable, query::fetchCount);
    }

    /**
     * Creates a new {@link JPQLQuery} for the given {@link Predicate}.
     *
     * @param predicate
     *                  filter conditions to be added (can be {@code null}).
     * @return the Querydsl {@link JPQLQuery}.
     * @param <E>
     *            the entity associated to {@link JPQLQuery}.
     */
    @SuppressWarnings("unchecked")
    protected <E> JPQLQuery<E> createQuery(@Nullable final Predicate... predicate) {

        AbstractJPAQuery<?, ?> query = querydsl().createQuery(path).where(predicate);
        final CrudMethodMetadata metadata = getRepositoryMethodMetadata();

        if (metadata == null) {
            return (JPQLQuery<E>) query;
        }

        final LockModeType type = metadata.getLockModeType();
        query = type == null ? query : query.setLockMode(type);

        getQueryHints().withFetchGraphs(entityManager).forEach(query::setHint);

        return (JPQLQuery<E>) query;
    }

}
