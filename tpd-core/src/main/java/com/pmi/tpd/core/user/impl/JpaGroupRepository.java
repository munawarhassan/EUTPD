package com.pmi.tpd.core.user.impl;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.google.common.base.Strings;
import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.QGroupEntity;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;

/**
 * Default JPA implementation of {@link IGroupRepository}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@Repository
public class JpaGroupRepository extends DefaultJpaRepository<GroupEntity, Long> implements IGroupRepository {

    /**
     * @param entityManager
     *            the JPA entity manager
     */
    public JpaGroupRepository(final EntityManager entityManager) {
        super(GroupEntity.class, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QGroupEntity entity() {
        return QGroupEntity.groupEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(@Nonnull final String groupName) {
        Assert.checkHasText(groupName, "groupName");
        final long i = count(entity().name.equalsIgnoreCase(groupName));
        return i == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public GroupEntity findByName(@Nullable final String name) {
        if (name == null || Strings.isNullOrEmpty(name.trim())) {
            return null;
        }
        return from().where(entity().name.equalsIgnoreCase(name)).fetchOne();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<GroupEntity> findGroups(@Nonnull final Pageable request) {
        Assert.checkNotNull(request, "request");
        Predicate predicate = null;
        if (request instanceof IFilterable) {
            final IFilterable filterable = (IFilterable) request;
            final String groupName = filterable.getQuery();
            if (StringUtils.isNotEmpty(groupName) && !"*".equals(groupName)) {
                predicate = DslPagingHelper.createLikeIgnoreCase(QGroupEntity.groupEntity.name, groupName);
            }
        }
        return findAll(predicate, request);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GroupEntity> findGroupsByName(@Nullable final String groupName, @Nonnull final Pageable pageRequest) {
        Predicate predicate = null;
        if (!Strings.isNullOrEmpty(groupName)) {
            predicate = entity().name.startsWithIgnoreCase(groupName);
        }
        return this.findAll(predicate, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GroupEntity> findGroupsByUser(final String username,
        final String groupName,
        final Pageable pageRequest) {
        BooleanExpression predicate = QUserEntity.userEntity.username.eq(username);
        if (!Strings.isNullOrEmpty(groupName)) {
            predicate = predicate.and(QGroupEntity.groupEntity.name.containsIgnoreCase(groupName));
        }
        return toPage(from().innerJoin(QGroupEntity.groupEntity.users, QUserEntity.userEntity).where(predicate),
            pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GroupEntity> findGroupsWithoutUser(final String username,
        final String groupName,
        final Pageable pageRequest) {
        final BooleanExpression predicate = QUserEntity.userEntity.username.eq(username);
        return toPage(from()
                .where(entity().id.notIn(JPAExpressions.selectFrom(entity())
                        .innerJoin(QGroupEntity.groupEntity.users, QUserEntity.userEntity)
                        .where(predicate)
                        .select(entity().id)))
                .where(!Strings.isNullOrEmpty(groupName) ? QGroupEntity.groupEntity.name.containsIgnoreCase(groupName)
                        : null),
            pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<GroupEntity> findByDeletedDateEarlierThan(@Nonnull final Date date, @Nonnull final Pageable request) {
        return this.findAll(entity().deletedDate.before(date), request);
    }

}
