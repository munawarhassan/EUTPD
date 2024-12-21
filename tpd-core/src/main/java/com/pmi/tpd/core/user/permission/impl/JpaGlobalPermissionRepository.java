package com.pmi.tpd.core.user.permission.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.QGlobalPermissionEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.security.permission.IPermittedGroup;
import com.pmi.tpd.security.permission.IPermittedUser;
import com.querydsl.jpa.impl.JPAQuery;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@Repository
public class JpaGlobalPermissionRepository extends AbstractGrantedPermissionRepository<GlobalPermissionEntity>
        implements IGlobalPermissionRepository {

    /**
     * @param entityManager
     *            the JPA the entity manager for the persistence unit.
     */
    @Autowired
    public JpaGlobalPermissionRepository(final EntityManager entityManager) {
        super(GlobalPermissionEntity.class, entityManager);
    }

    @Override
    public QGlobalPermissionEntity entity() {
        return QGlobalPermissionEntity.globalPermissionEntity;
    }

    @Override
    @Nonnull
    public Page<IPermittedUser> findHighestPermissionPerUser(@Nullable final String filter,
        @Nonnull final Pageable pageable) {
        // First, find a page of users that have repository permissions.
        // This will do two things for us:
        // 1. Apply pagination
        // 2. Sort the results
        final Page<UserEntity> page = findUsersWithPermission(filter, pageable);
        if (page.isLast() && page.getSize() == 0) {
            return PageUtils.createEmptyPage(pageable);
        }

        // Secondly we query for the userIds and highest permissions
        final JPAQuery<PermissionPerUser> query = createFindHighestPermissionPerUserQuery(page, null);

        // Lastly we pair the results of the 2 queries
        return pairUsersWithPermissions(page, query);
    }

    @Override
    @Nonnull
    public Page<IUser> findUsersWithPermission(@Nonnull final Pageable pageable) {
        return PageUtils.asPageOf(IUser.class, super.findUsersWithPermission(null, pageable));
    }

    @Override
    @Nonnull
    public Page<IUser> findUsersWithoutPermission(final Pageable pageable) {
        return PageUtils.asPageOf(IUser.class, super.findUsersWithoutPermission(null, pageable));
    }

    @Override
    @Nonnull
    public Page<IPermittedGroup> findHighestPermissionPerGroup(@Nullable final String filter,
        @Nonnull final Pageable pageable) {
        return PageUtils.asPageOf(IPermittedGroup.class,
            this.toPage(createFindHighestPermissionPerGroupQuery(filter, null), pageable));
    }

    @Override
    @Nonnull
    public Page<String> findGroupsWithPermission(@Nonnull final Pageable pageRequest) {
        return super.findGroupsWithPermission(pageRequest);
    }

    @Override
    @Nonnull
    public Page<String> findGroupsWithoutPermission(@Nonnull final Pageable pageRequest) {
        return super.findGroupsWithoutPermission(null, pageRequest);
    }

    @Transactional
    @Override
    public long revoke(@Nonnull final GlobalPermissionEntity globalPermission) {
        return createRevokeQuery(globalPermission, null).execute();
    }

}
