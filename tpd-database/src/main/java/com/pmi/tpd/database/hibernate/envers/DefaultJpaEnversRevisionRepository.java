package com.pmi.tpd.database.hibernate.envers;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.order.AuditOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.AnnotationRevisionMetadata;
import org.springframework.data.history.Revision;
import org.springframework.data.history.RevisionMetadata;
import org.springframework.data.history.RevisionSort;
import org.springframework.data.history.Revisions;
import org.springframework.data.repository.history.support.RevisionEntityInformation;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;

/**
 * Repository implementation using Hibernate Envers to implement revision specific query methods.
 *
 * @author Christophe Friederich
 * @since 1.0
 * @param <E>
 *            the type of entity
 * @param <ID>
 *            the type of unique identifier of type E.
 * @param <N>
 *            the type of revision number
 */
public abstract class DefaultJpaEnversRevisionRepository<E, ID extends Serializable, N extends Number & Comparable<N>>
        extends DefaultJpaRepository<E, ID> implements IEnversRevisionRepository<E, ID, N> {

    /** */
    @SuppressWarnings("unused")
    private final RevisionEntityInformation revisionEntityInformation;

    public DefaultJpaEnversRevisionRepository(final Class<E> domainClass, final EntityManager entityManager) {

        super(domainClass, entityManager);
        this.revisionEntityInformation = Optional.ofNullable(domainClass)
                .filter(cl -> !cl.equals(DefaultRevisionEntity.class))
                .<RevisionEntityInformation> map(ReflectionRevisionEntityInformation::new)
                .orElseGet(DefaultRevisionEntityInformation::new);
    }

    @Override
    public Optional<Revision<N, E>> findLastChangeRevision(final ID id) {

        final List<?> singleResult = createBaseQuery(id).addOrder(AuditEntity.revisionProperty("timestamp").desc())
                .setMaxResults(1)
                .getResultList();

        Assert.state(singleResult.size() <= 1, "We expect at most one result.");

        if (singleResult.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(createRevision(new QueryResult<>(singleResult.get(0))));
    }

    @Override
    public Optional<Revision<N, E>> findRevision(final ID id, final N revisionNumber) {

        checkNotNull(id, "id!");
        checkNotNull(revisionNumber, "revisionNumber");

        @SuppressWarnings("unchecked")
        final List<Object[]> singleResult = createBaseQuery(id) //
                .add(AuditEntity.revisionNumber().eq(revisionNumber)) //
                .getResultList();

        state(singleResult.size() <= 1, "We expect at most one result.");

        if (singleResult.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(createRevision(new QueryResult<>(singleResult.get(0))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Revisions<N, E> findRevisions(final ID id) {

        final List<Object[]> resultList = createBaseQuery(id).getResultList();
        final List<Revision<N, E>> revisionList = new ArrayList<>(resultList.size());

        for (final Object[] objects : resultList) {
            revisionList.add(createRevision(new QueryResult<>(objects)));
        }

        return Revisions.of(revisionList);
    }

    @Override
    public Page<Revision<N, E>> findRevisions(final ID id, final Pageable pageable) {

        final List<AuditOrder> sorting = Lists.newArrayList();
        if (!pageable.getSort().isEmpty()) {
            pageable.getSort()
                    .toList()
                    .forEach(
                        (order) -> sorting.add(order.isAscending() ? AuditEntity.property(order.getProperty()).asc()
                                : AuditEntity.property(order.getProperty()).desc()));
        } else {
            sorting.add(RevisionSort.getRevisionDirection(pageable.getSort()).isDescending()
                    ? AuditEntity.revisionNumber().desc() : AuditEntity.revisionNumber().asc());
        }
        final AuditQuery query = createBaseQuery(id);
        sorting.forEach((order) -> query.addOrder(order));
        final List<?> resultList = query.setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        final Long count = (Long) createBaseQuery(id).addProjection(AuditEntity.revisionNumber().count())
                .getSingleResult();

        final List<Revision<N, E>> revisions = new ArrayList<>();

        for (final Object singleResult : resultList) {
            revisions.add(createRevision(new QueryResult<E>(singleResult)));
        }

        return new PageImpl<>(revisions, pageable, count);
    }

    private AuditQuery createBaseQuery(final ID id) {

        final Class<E> type = getDomainClass();
        final AuditReader reader = AuditReaderFactory.get(entityManager);

        return reader.createQuery().forRevisionsOfEntity(type, false, true).add(AuditEntity.id().eq(id));
    }

    @SuppressWarnings("unchecked")
    private Revision<N, E> createRevision(final QueryResult<E> queryResult) {
        return Revision.of((RevisionMetadata<N>) queryResult.createRevisionMetadata(), queryResult.getEntity());
    }

    static class QueryResult<T> {

        private final T entity;

        private final Object metadata;

        private final RevisionMetadata.RevisionType revisionType;

        @SuppressWarnings("unchecked")
        QueryResult(final Object data) {
            state(data.getClass().isArray(), "dta should be an array");
            final Object[] ar = (Object[]) data;
            checkNotNull(data, "Data must not be null");
            state(ar.length == 3, String.format("Data must have length three, but has length %d.", ar.length));
            state(ar[2] instanceof RevisionType,
                String.format("The third array element must be of type Revision type, but is of type %s",
                    ar[2].getClass()));

            entity = (T) ar[0];
            metadata = ar[1];
            revisionType = convertRevisionType((RevisionType) ar[2]);
        }

        public T getEntity() {
            return entity;
        }

        RevisionMetadata<?> createRevisionMetadata() {

            return metadata instanceof DefaultRevisionEntity
                    ? new DefaultRevisionMetadata((DefaultRevisionEntity) metadata, revisionType)
                    : new AnnotationRevisionMetadata<>(metadata, RevisionNumber.class, RevisionTimestamp.class,
                            revisionType);
        }

        private static RevisionMetadata.RevisionType convertRevisionType(final RevisionType datum) {

            switch (datum) {

                case ADD:
                    return RevisionMetadata.RevisionType.INSERT;
                case MOD:
                    return RevisionMetadata.RevisionType.UPDATE;
                case DEL:
                    return RevisionMetadata.RevisionType.DELETE;
                default:
                    return RevisionMetadata.RevisionType.UNKNOWN;
            }
        }
    }

}
