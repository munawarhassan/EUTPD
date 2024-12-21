package com.pmi.tpd.core.user.permission.impl;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.model.user.PermissionTypeEntity;
import com.pmi.tpd.core.model.user.QGlobalPermissionEntity;
import com.pmi.tpd.core.model.user.QGrantedPermission;
import com.pmi.tpd.core.model.user.QPermissionTypeEntity;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.permission.GroupPermissionCriteria;
import com.pmi.tpd.core.user.permission.PermissionCriteria;
import com.pmi.tpd.core.user.permission.UserPermissionCriteria;
import com.pmi.tpd.core.user.permission.spi.IEffectivePermissionRepository;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;
import com.pmi.tpd.database.support.IdentifierUtils;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;

/**
 * @author Christophe Friederich
 */
@Repository
public class JpaEffectivePermissionRepository extends DefaultJpaRepository<GrantedPermission, Long>
        implements IEffectivePermissionRepository {

    /**
     * @param entityManager
     *            the JPA the entity manager for the persistence unit.
     */
    public JpaEffectivePermissionRepository(final EntityManager entityManager) {
        super(GrantedPermission.class, entityManager);
    }

    @Override
    public QGrantedPermission entity() {
        return QGrantedPermission.grantedPermission;
    }

    /**
     * @return Returns the specific {@link EntityPath} for {@link PermissionTypeEntity} entity.
     */
    public QPermissionTypeEntity permissionType() {
        return QPermissionTypeEntity.permissionTypeEntity;
    }

    /**
     * @return Returns the specific {@link EntityPath} for {@link GlobalPermissionEntity} entity.
     */
    public QGlobalPermissionEntity globalPermission() {
        return QGlobalPermissionEntity.globalPermissionEntity;
    }

    @Nonnull
    @Override
    public Page<GrantedPermission> findByGroup(@Nonnull final String group, @Nonnull final Pageable pageable) {
        return this.toPage(
            new JPAQuery<GrantedPermission>(entityManager).from(entity(), permissionType())
                    .where(entity().user.isNull()
                            .and(entity().permission.eq(permissionType().id))
                            .and(entity().group.eq(IdentifierUtils.toLowerCase(group))))
                    .orderBy(entity().id.asc()),
            pageable);
    }

    @Nonnull
    @Override
    public Page<GrantedPermission> findByUserId(@Nonnull final Long userId, @Nonnull final Pageable pageable) {
        return this.toPage(
            new JPAQuery<GrantedPermission>(entityManager).from(entity(), permissionType())
                    .where(entity().group.isNull()
                            .and(entity().permission.eq(permissionType().id))
                            .and(entity().user.id.eq(userId)))
                    .orderBy(entity().id.asc()),
            pageable);
    }

    @Override
    @Nonnull
    public Page<String> findGroups(@Nonnull final Permission permission, @Nonnull final Pageable pageable) {
        return this.toPage(from(globalPermission())
                .where(globalPermission().permission.in(permission.getInheritingPermissions())
                        .and(globalPermission().user.isNull())
                        .and(globalPermission().group.isNotNull()))
                .orderBy(globalPermission().group.asc())
                .select(globalPermission().group)
                .distinct(),
            pageable);
    }

    @Override
    @Nonnull
    public Page<UserEntity> findUsers(@Nonnull final Permission permission,
        @Nonnull final Pageable pageable,
        @Nonnull final Predicate predicate) {
        return this.toPage(
            new JPAQuery<UserEntity>(entityManager).from(QUserEntity.userEntity, globalPermission())
                    .where(predicate,
                        globalPermission().user.eq(QUserEntity.userEntity)
                                .and(globalPermission().permission.in(permission.getInheritingPermissions()))
                                .and(globalPermission().user.isNotNull())
                                .and(globalPermission().group.isNull()))
                    .orderBy(QUserEntity.userEntity.username.asc())
                    .distinct(),
            pageable);
    }

    @Override
    public boolean isGrantedToGroup(@Nonnull final GroupPermissionCriteria criteria) {
        final Permission permission = criteria.getPermission();
        final Set<String> groups = Sets
                .newHashSet(Iterables.transform(criteria.getGroups(), perm -> IdentifierUtils.toLowerCase(perm)));
        return from()
                .where(entity().permission.in(permission.getInheritingPermissions())
                        .and(entity().user.isNull())
                        .and(entity().group.isNull().or(entity().group.in(groups))))
                .fetchCount() > 0;
    }

    @Override
    public boolean isGrantedToUser(@Nonnull final UserPermissionCriteria criteria) {
        final Permission permission = criteria.getPermission();
        final Long userId = criteria.getUserId();
        return this.from()
                .where(entity().permission.in(permission.getInheritingPermissions())
                        .and(entity().user.isNull().or(entity().user.id.eq(userId)))
                        .and(entity().group.isNull()))
                .fetchCount() > 0;
    }

    @SuppressWarnings("unused")
    private void includeResourceIds(final PermissionCriteria criteria, final DetachedCriteria permissionCriteria) {
        // final Permission permission = criteria.getPermission();
        // final Integer projectId = criteria.getProjectId();
        // final Integer repositoryId = criteria.getRepositoryId();
        //
        // if (permission.isGlobal()) {
        // // matches only global permissions
        // // (note: this clause is not necessary since non-global permissions would be eliminated
        // // by the 'in' clause on the perm_id column anyways. However, it leads to > 50% CPU/IO cost reduction.)
        // permissionCriteria.add(Restrictions.isNull("project")).add(Restrictions.isNull("repository"));
        // } else if (permission.isResource(Project.class) && !Permission.PROJECT_VIEW.equals(permission)) {
        // // filter out repository permissions
        // // (like above, this clause is not necessary since repository permissions would be eliminated by the 'in'
        // // clause on the perm_id column. However, it leads to ~ 20% CPU/IO cost reduction.)
        // permissionCriteria.add(Restrictions.isNull("repository"));
        // // the project id will be null for queries checking if the user (or group) has any project permission
        // // (for example, queries originating from PermissionService.hasAnyUserPermission())
        // if (projectId != null) {
        // permissionCriteria.add(Restrictions.or(
        // // match global permissions
        // Restrictions.isNull("project"),
        // // match project permissions
        // Restrictions.eq("project.id", projectId)));
        // }
        // } else if (permission.isResource(Repository.class) || Permission.PROJECT_VIEW.equals(permission)) {
        // if (projectId != null || repositoryId != null) {
        // final Disjunction disjunction = Restrictions.disjunction();
        // // match global permissions
        // disjunction.add(Restrictions.and(Restrictions.isNull("project"), Restrictions.isNull("repository")));
        // // match project permissions
        // if (projectId != null) { // assume there are usages for retrieving the repo permissions while ignoring
        // // inheritance from project permissions
        // disjunction.add(Restrictions.eq("project.id", projectId));
        // }
        // // match repository permissions
        // if (repositoryId != null) {
        // disjunction.add(Restrictions.eq("repository.id", repositoryId));
        // } else if (Permission.PROJECT_VIEW.equals(permission)) {
        // // Perform a left outer join to the repository table but ONLY when
        // // we need to
        // permissionCriteria.createAlias("repository", "joinRepository", JoinType.LEFT_OUTER_JOIN);
        //
        // disjunction.add(Restrictions.eq("joinRepository.project.id", projectId));
        // }
        // permissionCriteria.add(disjunction);
        // }
        // } else {
        // throw new IllegalArgumentException("Unsupported permission: " + permission);
        // }
    }

}
