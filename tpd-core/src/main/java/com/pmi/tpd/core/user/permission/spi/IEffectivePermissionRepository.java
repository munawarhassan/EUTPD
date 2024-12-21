package com.pmi.tpd.core.user.permission.spi;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.core.model.user.GrantedPermission;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.user.permission.GroupPermissionCriteria;
import com.pmi.tpd.core.user.permission.UserPermissionCriteria;
import com.pmi.tpd.database.jpa.IDslAccessor;
import com.querydsl.core.types.Predicate;

/**
 * A repository to determine the <em>effective permissions</em> granted to a user or a group.
 * <p>
 * An effective permission is a permission that is either:
 * <ul>
 * <li>directly granted (as a permission associated with the user or as a permission associated with a group the user is
 * a member of),</li>
 * <li>or indirectly granted through a {@link Permission#getInheritedPermissions() inherited} permission (such as a
 * {@link Permission#SYS_ADMIN sysadmin} inheriting a {@link Permission#ADMIN admin} permission).</li>
 * </ul>
 *
 * @see GlobalPermissionDao
 * @see ProjectPermissionDao
 * @see RepositoryPermissionDao
 * @since 2.0
 */
public interface IEffectivePermissionRepository extends IDslAccessor<GrantedPermission, Long> {

    /**
     * Searches for {@link InternalGrantedPermission granted permissions} that are granted to one of the groups in
     * {@code groups}.
     *
     * @param group
     *            the group name
     * @param pageRequest
     *            specifies the page of groups to return
     * @return a page containing zero or more {@link InternalGrantedPermission}s, ordered by group name (ascending)
     */
    @Nonnull
    Page<GrantedPermission> findByGroup(@Nonnull String group, @Nonnull Pageable pageRequest);

    /**
     * Searches for {@link InternalGrantedPermission granted permissions} that are granted to the user with the provided
     * {@code userId}.
     *
     * @param userId
     *            the id of the user.
     * @param pageRequest
     *            a page request.
     * @return a page containing zero or more {@link InternalGrantedPermission}s.
     */
    @Nonnull
    Page<GrantedPermission> findByUserId(@Nonnull Long userId, @Nonnull Pageable pageRequest);

    /**
     * Searches for users that have a (direct or inherited) user permission.
     * <p>
     * Only <u>explicit</u> (i.e. not through a group) permissions are considered. A user will be returned if and only
     * if it has either:
     * <ul>
     * <li>the user permission {@code permission}, or</li>
     * <li>another user permission that implies the {@code permission}.</li>
     * </ul>
     * <p>
     * For example, searching for users that have the {@link Permission#ADMIN} permissions will return any user that
     * have either the {@link Permission#SYS_ADMIN} or {@link Permission#ADMIN} explicit permissions.
     *
     * @param permission
     *            permission the groups must have (either directly or through inheritance)
     * @param pageRequest
     *            specifies the page of users which should be returned
     * @param predicate
     *            the predicate that all returned entities must match
     * @return a page containing zero or more users, ordered by name
     */
    @Nonnull
    Page<UserEntity> findUsers(@Nonnull Permission permission,
        @Nonnull Pageable pageRequest,
        @Nonnull Predicate predicate);

    /**
     * Searches for groups that have a (direct or inherited) permission.
     * <p>
     * A group will be returned if and only if it has either:
     * <ul>
     * <li>the group permission {@code permission}, or</li>
     * <li>another group permission that implies the {@code permission}.</li>
     * </ul>
     * <p>
     * For example, searching for groups that have the {@link Permission#ADMIN} permissions will return any group that
     * have either the {@link Permission#SYS_ADMIN} or {@link Permission#ADMIN} permissions.
     *
     * @param permission
     *            permission the groups must have (either directly or through inheritance)
     * @param pageRequest
     *            specifies the page of groups which should be returned
     * @return a page containing zero or more groups, ordered by name
     */
    @Nonnull
    Page<String> findGroups(@Nonnull Permission permission, @Nonnull Pageable pageRequest);

    /**
     * Checks whether <em>any</em> of the groups has the permission.
     * <p>
     * This will return {@code true} if and only if any of the group:
     * <ul>
     * <li>has the {@code permission} or,</li>
     * <li>has another group permission implying {@code permission}</li>
     * </ul>
     *
     * @param criteria
     *            criteria specifying which permission to check
     * @return {@code true} if any of the groups inherit the permission
     */
    boolean isGrantedToGroup(@Nonnull GroupPermissionCriteria criteria);

    /**
     * Checks whether the user has the permission.
     * <p>
     * This will return {@code true} if and only if the user:
     * <ul>
     * <li>has the {@code permission} or,</li>
     * <li>has another user permission implying {@code permission}</li>
     * </ul>
     *
     * @param criteria
     *            criteria specifying which permission to check
     * @return {@code true} if the user has or inherits the permission
     */
    boolean isGrantedToUser(@Nonnull UserPermissionCriteria criteria);
}
