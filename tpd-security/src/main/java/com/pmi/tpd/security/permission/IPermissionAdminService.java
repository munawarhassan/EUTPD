package com.pmi.tpd.security.permission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.user.IUser;

/**
 * Updates and queries the permissions of users and groups.
 *
 * @see IPermissionService
 * @since 2.0
 */
public interface IPermissionAdminService {

  /**
   * Check if a {@link UserType#NORMAL normal} user can remove themselves from the
   * given group.
   *
   * @param username
   *                 the user to check
   * @param group
   *                 the group from which the user is to be removed.
   */
  void canRemoveUserFromGroup(@Nonnull String username, @Nonnull String group);

  /**
   * Check if the current user can add users to the given group.
   *
   * @param group
   *              the group to which users will be added.
   */
  void canAddUserToGroup(@Nonnull String group);

  /**
   * Check if the current user can delete a group without affecting their
   * permissions.
   *
   * @param group
   *              the group to be deleted.
   */
  void canDeleteGroup(@Nonnull String group);

  /**
   * Check if the user can be deleted.
   *
   * @param user
   *             the user being deleted
   */
  void canDeleteUser(@Nonnull IUser user);

  /**
   * Check if the user can be deleted.
   *
   * @param username
   *                 the user name being deleted
   */
  void canDeleteUser(String username);

  /**
   * Check if the user can be activated/deactivated.
   *
   * @param user
   *                 the user being activated/deactivated.
   * @param activate
   *                 indicate if activation or deactivation.
   */
  void canActivateUser(@Nonnull IUser user, boolean activate);

  /**
   * Retrieves a page of {@link PermittedGroup groups} and their highest global
   * permission. Groups which do not have
   * any global permissions <i>explicitly</i> assigned to them will not be
   * returned.
   *
   * @param filter
   *                    if non-null non-empty then the groups returned must
   *                    include this text
   * @param pageRequest
   *                    bounds the page of groups to be returned
   * @return a page containing zero or more groups which have been explicitly
   *         granted a global permission, and their
   *         highest granted permission
   */
  @Nonnull
  Page<IPermittedGroup> findGroupsWithGlobalPermission(@Nullable String filter, @Nonnull Pageable pageRequest);

  /**
   * Retrieves a page of groups who have not been explicitly granted any global
   * permission.
   *
   * @param pageRequest
   *                    bounds the page of groups to be returned
   * @return a page of groupnames.
   */
  @Nonnull
  Page<String> findGroupsWithoutGlobalPermission(@Nonnull Pageable pageRequest);

  /**
   * Retrieves a page of {@link PermittedUser users} and their highest global
   * permission. Users which do not have any
   * global permissions <i>explicitly</i> assigned to them will not be returned.
   *
   * @param filter
   *                    if non-null non-empty then the usernames returned must
   *                    include this text
   * @param pageRequest
   *                    bounds the page of users to be returned
   * @return a page containing zero or more users who have been explicitly granted
   *         a global permission, and their
   *         highest granted permission
   */
  @Nonnull
  Page<IPermittedUser> findUsersWithGlobalPermission(@Nullable String filter, @Nonnull Pageable pageRequest);

  /**
   * Retrieves a page of {@link IUser#isActivated() active} users who have not
   * been explicitly granted any global
   * permission.
   *
   * @param pageRequest
   *                    bounds the page of users to be returned
   * @return a page of {@link IUser} instances.
   */
  @Nonnull
  Page<IUser> findUsersWithoutGlobalPermission(@Nonnull Pageable pageRequest);

  /**
   * Assigns a permission to multiple users and/or groups.
   * <p>
   * Note that:
   * <ul>
   * <li>granting a global permission to users or groups will remove their other
   * <em>global</em> permissions,</li>
   * <li>granting a project permission to users or groups will remove their other
   * <em>project</em> permissions on the
   * same project,</li>
   * <li>granting a repository permission to users or groups will remove their
   * other <em>repository</em> permissions
   * on the same repository.</li>
   * </ul>
   *
   * @param request
   *                request specifying which permission to grant to which users
   *                and/or groups
   * @see #grantAllProjectPermission(Permission, Project) to grant default project
   *      permissions
   */
  void setPermission(@Nonnull SetPermissionRequest request);

  /**
   * Revoke all global permissions for the given group.
   *
   * @param group
   *              name of the group
   */
  void revokeAllGlobalPermissions(@Nonnull String group);

  /**
   * Revoke all global permissions for the given user.
   *
   * @param user
   *             user to revoke permissions from
   */
  void revokeAllGlobalPermissions(@Nonnull IUser user);

  /**
   * Revoke all permissions from a user.
   *
   * @param name
   *             name of the normal user
   */
  void revokeAllUserPermissions(@Nonnull String name);

  /**
   * Revoke all permissions from a user.
   *
   * @param user
   *             the user
   */
  void revokeAllUserPermissions(@Nonnull IUser user);

  /**
   * Revoke all permissions granted to a group.
   *
   * @param name
   *             name of the group
   */
  void revokeAllGroupPermissions(@Nonnull String name);

}
