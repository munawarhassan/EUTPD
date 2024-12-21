package com.pmi.tpd.core.user.permission.impl;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.database.support.IdentifierUtils.toLowerCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.model.user.PermissionTypeEntity;
import com.pmi.tpd.core.model.user.PermittedGroup;
import com.pmi.tpd.core.model.user.PermittedUser;
import com.pmi.tpd.core.model.user.QGrantedPermission;
import com.pmi.tpd.core.model.user.QGroupEntity;
import com.pmi.tpd.core.model.user.QPermissionTypeEntity;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.permission.spi.IGrantedPermissionRepository;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;
import com.pmi.tpd.security.permission.IPermittedUser;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @param <E>
 */
public abstract class AbstractGrantedPermissionRepository<E extends GrantedPermission>
        extends DefaultJpaRepository<E, Long> implements IGrantedPermissionRepository<E> {

    /**
     * @param domainClass
     *                      the inherited domain class of {@link GrantedPermission} associated to this repository.
     * @param entityManager
     *                      the JPA the entity manager for the persistence unit.
     */
    public AbstractGrantedPermissionRepository(final Class<E> domainClass, final EntityManager entityManager) {
        super(domainClass, entityManager);
    }

    /**
     * @return Returns the specific {@link EntityPath} for {@link GrantedPermission} entity.
     */
    public QGrantedPermission grantedPermission() {
        return QGrantedPermission.grantedPermission;
    }

    /**
     * @return Returns the specific {@link EntityPath} for {@link PermissionTypeEntity} entity.
     */
    public QPermissionTypeEntity permissionType() {
        return QPermissionTypeEntity.permissionTypeEntity;
    }

    @Override
    public boolean hasPermissionEntry(@Nonnull final E grantedPermission) {
        final JPQLQuery<Long> query = createHasPermissionEntryCriteria(grantedPermission);
        return query.fetchOne() > 0;
    }

    /**
     * @param filter
     * @param additionalClause
     * @return
     */
    protected JPAQuery<PermittedGroup> createFindHighestPermissionPerGroupQuery(final String filter,
        final Predicate additionalClause) {
        // Join between t_*_permission table and t_permission_type to determine the weights for all permissions and
        // then
        // group by group to compute the highest permission for each group, ordered by group name. This query produces
        // an
        // inner join, augmenting each row with a weight. Such joins can _only_ be done in HQL.
        //
        // Previously this query was performed in two parts similar to findUsersAndPermissions()
        // but this is not required for groups as the group name is all we had to duplicate a lot of work
        // between the two queries
        return new JPAQuery<PermittedGroup>(entityManager).from(entity(), grantedPermission(), permissionType())
                .where(grantedPermission().permission.eq(permissionType().id)
                        .and(grantedPermission().user.isNull())
                        .and(StringUtils.isNotEmpty(filter) ? grantedPermission().group.containsIgnoreCase(filter)
                                : grantedPermission().group.isNotNull()),
                    additionalClause)
                .groupBy(grantedPermission().group)
                .orderBy(grantedPermission().group.asc())
                .select(Projections.constructor(PermittedGroup.class,
                    new Class<?>[] { String.class, int.class },
                    grantedPermission().group,
                    permissionType().weight.max().as("weight")));
    }

    /**
     * @param users
     * @param additionalClause
     * @return
     */
    protected JPAQuery<PermissionPerUser> createFindHighestPermissionPerUserQuery(final Page<UserEntity> users,
        final Predicate additionalClause) {
        // Use the users we loaded with the previous query to execute a second query, computing the highest
        // permission that has been associated with each user.
        //
        // However, to order the results we'd need a third join out to the t_user table to get the username. The
        // resulting join is unpleasant at best (especially since Hibernate wants to do that join twice), but works. The
        // real issue is that we need to group by user ID but that precludes ordering by username. Because usernames are
        // unique, we could theoretically group by that instead, but that precludes retrieving a User--we'd have to
        // return the username instead.
        //
        // There are likely some esoteric things we could do in HQL to try and work around this, but they'd likely end
        // up being either incredibly complicated, incredibly brittle or both. Instead, we pair the results in code.
        return new JPAQuery<PermissionPerUser>(entityManager).from(entity(), grantedPermission(), permissionType())
                .where(
                    grantedPermission().permission.eq(permissionType().id)
                            .and(grantedPermission().user.in(users.getContent())),
                    additionalClause)
                .groupBy(grantedPermission().user.id)
                .select(Projections.constructor(PermissionPerUser.class,
                    new Class<?>[] { Long.class, int.class },
                    grantedPermission().user.id,
                    permissionType().weight.max().as("weight")));
    }

    /**
     * Create a Criteria to find how many permission entries match the given granted permission.
     * <p>
     * Subclasses should override this method if they require more restrictions to be added to the Criteria
     *
     * @param grantedPermission
     *                          the granted permission to match
     * @return the search Criteria
     */
    protected JPQLQuery<Long> createHasPermissionEntryCriteria(final E grantedPermission) {
        return this.from(grantedPermission())
                .where(grantedPermission().permission.eq(grantedPermission.getPermission())
                        .and(grantedPermission.getGroup() == null ? grantedPermission().group.isNull()
                                : grantedPermission().group.eq(toLowerCase(grantedPermission.getGroup())))
                        .and(grantedPermission.getUser() == null ? grantedPermission().user.isNull()
                                : grantedPermission().user.eq(grantedPermission.getUser())))
                .select(grantedPermission().id.count());
    }

    /**
     * @param grantedPermission
     * @param additionalClause
     * @return
     */
    protected JPADeleteClause createRevokeQuery(final GrantedPermission grantedPermission,
        final Predicate additionalClause) {
        return this.deleteFrom(grantedPermission())
                .where(grantedPermission().permission.eq(grantedPermission.getPermission())
                        .and(grantedPermission.getGroup() == null ? grantedPermission().group.isNull()
                                : grantedPermission().group.eq(toLowerCase(grantedPermission.getGroup())))
                        .and(grantedPermission.getUser() == null ? grantedPermission().user.isNull()
                                : grantedPermission().user.eq(grantedPermission.getUser())),
                    additionalClause);
    }

    /**
     * @param pageable
     * @param criterion
     * @return
     */
    protected Page<String> findGroupsWithPermission(final Pageable pageable, final Predicate... criterion) {
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, QGroupEntity.groupEntity, criterion);
        return this.toPage(
            this.from(grantedPermission())
                    .where(predicates)
                    .where(grantedPermission().user.isNull().and(grantedPermission().group.isNotNull()))
                    .distinct()
                    .select(grantedPermission().group)
                    .orderBy(grantedPermission().group.asc()),
            pageable);

    }

    protected Page<String> findGroupsWithoutPermission(final String filter,
        final Pageable pageable,
        final Predicate... criterion) {
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, QGroupEntity.groupEntity, criterion);
        return this
                .toPage(
                    from(QGroupEntity.groupEntity).select(QGroupEntity.groupEntity.name)
                            .where(
                                QGroupEntity.groupEntity.name.notIn(JPAExpressions.selectFrom(grantedPermission())
                                        .where(grantedPermission().user.isNull()
                                                .and(grantedPermission().group.isNotNull()))
                                        .distinct()
                                        .select(grantedPermission().group)),
                                StringUtils.isNotEmpty(filter)
                                        ? QGroupEntity.groupEntity.name.containsIgnoreCase(filter) : null)
                            .where(predicates),
                    pageable);
    }

    /**
     * @param filter
     * @param pageable
     * @param criterion
     * @return
     */
    protected Page<UserEntity> findUsersWithPermission(final String filter,
        final Pageable pageable,
        final Predicate... criterion) {
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, QUserEntity.userEntity, criterion);
        // find the users matching the permissions
        return toPage(from(QUserEntity.userEntity)
                .where(
                    QUserEntity.userEntity.id.in(JPAExpressions.selectFrom(grantedPermission())
                            .where(grantedPermission().user.isNotNull().and(grantedPermission().group.isNull()))
                            .select(grantedPermission().user.id)
                            .distinct()),
                    StringUtils.isNotEmpty(filter) ? QUserEntity.userEntity.username.containsIgnoreCase(filter) : null)
                .where(predicates)
                .orderBy(QUserEntity.userEntity.username.asc()),
            pageable);
    }

    /**
     * @param filter
     * @param pageable
     * @param criterion
     * @return
     */
    protected Page<UserEntity> findUsersWithoutPermission(final String filter,
        final Pageable pageable,
        final Predicate... criterion) {
        final Predicate predicates = DslPagingHelper.createPredicates(pageable, QUserEntity.userEntity, criterion);
        // find the users matching the permissions
        return toPage(from(QUserEntity.userEntity)
                .where(
                    QUserEntity.userEntity.id.notIn(JPAExpressions.selectFrom(grantedPermission())
                            .where(grantedPermission().user.isNotNull().and(grantedPermission().group.isNull()))
                            .select(grantedPermission().user.id)
                            .distinct()),
                    StringUtils.isNotEmpty(filter) ? QUserEntity.userEntity.username.containsIgnoreCase(filter) : null)
                .where(predicates),
            pageable);
    }

    /**
     * @param page
     * @param query
     * @return
     */
    protected Page<IPermittedUser> pairUsersWithPermissions(final Page<UserEntity> page,
        final JPAQuery<PermissionPerUser> query) {
        // noinspection unchecked
        final List<PermissionPerUser> values = query.fetch();

        // Once we've loaded the user ID to max weight pairs, dump them in a map to simplify the next lookup
        final Map<Long, Integer> idToWeight = new HashMap<>(values.size());
        for (final PermissionPerUser permittedUser : values) {
            idToWeight.put(permittedUser.getUserId(), permittedUser.getWeight());
        }

        // Transform the page of UserEntity into a page of PermittedUsers by using the user's ID to look up the max
        // weight and then transform that to a Permission (since weights are guaranteed unique).
        return page.map(user -> {
            final Integer weight = idToWeight.get(user.getId());
            if (weight == null) {
                throw new IllegalStateException(
                        "No weight was found for the permission assigned to " + user.getUsername());
            }
            return new PermittedUser(user, weight);
        });
    }

    @Override
    public long revokeAll(@Nonnull final Long userId) {
        return this.deleteFrom(grantedPermission())
                .where(grantedPermission().user.id.eq(checkNotNull(userId, "userId")))
                .execute();
    }

    @Override
    public long revokeAll(@Nonnull final String groupName) {
        return this.deleteFrom(grantedPermission())
                .where(grantedPermission().group.eq(toLowerCase(checkNotNull(groupName, "groupName"))))
                .execute();
    }

    /**
     * This QueryDsl projection class is used in
     * {@link AbstractGrantedPermissionRepository#createFindHighestPermissionPerUserQuery(Page, Predicate)} method.
     *
     * @author Christophe Friederich
     * @since 2.0
     */
    protected static class PermissionPerUser {

        /** */
        private final long userId;

        /** */
        private final int weight;

        /**
         * @param userId
         *               unique identifier for an user.
         * @param weight
         *               the weight of permission.
         */
        public PermissionPerUser(final long userId, final int weight) {
            super();
            this.userId = userId;
            this.weight = weight;
        }

        /**
         * @return Returns unique identifier for an user
         */
        public long getUserId() {
            return userId;
        }

        /**
         * @return Returns the weight of permission.
         */
        public int getWeight() {
            return weight;
        }

    }

}
