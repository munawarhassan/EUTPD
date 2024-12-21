package com.pmi.tpd.security.permission;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * Reads the permissions of users and groups. IMPORTANT: This should not be
 * restricted by permissions, and is not
 * intended for querying access levels of users, but rather checking access of
 * the current user at runtime
 *
 * @see IPermissionAdminService
 */
public interface IPermissionService {

  /**
   * @return {@code true} if the given user identified by {@code username} has the
   *         requested {@link Permission}. Will
   *         return true if the user is {@link IUser#isActivated() active} and one
   *         of the following conditions is met:
   *         <ul>
   *         <li>permission is granted directly for the given user</li>
   *         <li>permission is granted to a group the given user is a member
   *         of</li>
   *         </ul>
   * @param username
   *                   the user in question
   * @param permission
   *                   the requested permission. Must be global.
   */
  boolean hasGlobalPermission(@Nullable String username, @Nonnull Permission permission);

  /**
   * @return {@code true} if the given {@link IUser} has the requested
   *         {@link Permission}. Will return true if the
   *         user is {@link IUser#isActivated() active} and one of the following
   *         conditions is met:
   *         <ul>
   *         <li>permission is granted directly for the given user</li>
   *         <li>permission is granted to a group the given user is a member
   *         of</li>
   *         </ul>
   * @param user
   *                   the user in question
   * @param permission
   *                   the requested permission. Must be global.
   */
  boolean hasGlobalPermission(@Nullable IUser user, @Nonnull Permission permission);

  /**
   * @return true if the <em>current authentication session</em> has the requested
   *         {@link Permission}
   * @param permission
   *                   the requested permission. Must be global.
   * @see IPermissionService#hasGlobalPermission(IUser, Permission)
   */
  boolean hasGlobalPermission(@Nonnull Permission permission);

  /**
   * @return true if the given {@link IUser} has the requested {@link Permission}
   *         for <em>any</em> project /
   *         repository
   * @param user
   *                   the user in question
   * @param permission
   *                   the requested permission. Must be non-global.
   */
  boolean hasAnyUserPermission(@Nonnull IUser user, @Nonnull Permission permission);

  /**
   * @return true if the <em>current user</em> has the requested
   *         {@link Permission} for <em>any</em> project /
   *         repository
   * @param permission
   *                   the requested permission. Must be non-global.
   */
  boolean hasAnyUserPermission(@Nonnull Permission permission);

  /**
   * @param token
   *                   the token in question
   * @param permission
   *                   the requested permission. Must be non-global.
   * @return true if the <em>current user</em> has the requested
   *         {@link Permission} for <em>any</em> project /
   *         repository
   */
  boolean hasAnyUserPermission(@Nullable UserAuthenticationToken token, @Nonnull Permission permission);

  /**
   * @return true if the current user has the given global permission through its
   *         membership of a group
   * @param permission
   *                       The permission required.
   * @param excludedGroups
   *                       A Set of groups to be excluded from consideration.
   */
  boolean hasGlobalPermissionThroughGroupMembership(@Nonnull Permission permission,
      @Nonnull Set<String> excludedGroups);

  /**
   * @return true if the current user has the given global permission directly
   *         granted (i.e. not through their group
   *         membership)
   * @param permission
   *                   the permission required.
   */
  boolean hasDirectGlobalUserPermission(@Nonnull Permission permission);

  /**
   * @return true if the given group has the given permission.
   * @param permission
   *                   the permission required.
   * @param group
   *                   the group to be checked.
   */
  boolean hasGlobalGroupPermission(@Nonnull Permission permission, @Nonnull String group);

  /**
   * Get the users which are granted a permission.
   *
   * @param permission
   *                   the permission in question
   * @param request
   *                   a page request
   * @return the page of users who have been granted a permission or an inheriting
   *         permission
   */
  @Nonnull
  Page<IUser> getGrantedUsers(@Nonnull Permission permission, @Nonnull Pageable request);

  /**
   * Get the groups which are granted a permission.
   *
   * @param permission
   *                   the permission in question
   * @param request
   *                   a page request
   * @return the page of group names which have been granted a permission or an
   *         inheriting permission
   */
  @Nonnull
  Page<String> getGrantedGroups(@Nonnull Permission permission, @Nonnull Pageable request);

  /**
   * Get the usernames of the users with the given permission (whether directly or
   * though permission inheritance) This
   * includes users granted the permission directly and those who have the
   * permission through their group membership.
   *
   * @param permission
   *                   the permission for which the users are being fetched. It
   *                   must be {@link Permission#isGlobal() global}
   * @return The lowercase usernames of all the users who currently have the given
   *         permission
   */
  @Nonnull
  Set<String> getUsersWithPermission(@Nonnull Permission permission);

  /**
   * Get the highest global permission for a user.
   *
   * @param user
   *             the user
   * @return the highest global permission for the user or null if they have no
   *         permissions
   */
  @Nullable
  Permission getHighestGlobalPermission(@Nullable IUser user);

  /**
   * Get the highest global permission for a user.
   *
   * @param username
   *                 the user name
   * @return the highest global permission for the user or null if they have no
   *         permissions
   */
  @Nullable
  Permission getHighestGlobalPermission(@Nullable String username);

  /**
   * Get the highest global permission for a group.
   *
   * @param groupName
   *                  the group name
   * @return the highest global permission for the group or null if the group has
   *         no permissions
   */
  @Nullable
  Permission getHighestGlobalGroupPermission(@Nullable String groupName);

  /**
   * The complete set of {@link IEffectivePermission effective permissions} for
   * the supplied user. Only the minimal
   * set of effective permissions is returned by the iterator such that any
   * effective permission that can be
   * {@link Permission#getImplyingPermissions() inferred} from other permissions
   * is not returned.
   *
   * @param user
   *             the user
   * @return a minimal sequence of effective permissions
   */
  @Nonnull
  Iterable<IEffectivePermission> getEffectivePermissions(@Nonnull IUser user);

  /**
   * @param token
   *                   the token in question
   * @param permission
   *                   the requested permission. Must be global.
   * @return true if the given token has the requested {@link Permission}. Will
   *         return true if one of the following
   *         conditions is met:
   *         <ul>
   *         <li>permission is granted for the given user</li>
   *         <li>permission is granted for all logged in users</li>
   *         <li>permission is granted for anonymous</li>
   *         </ul>
   * @see PermissionService#hasGlobalPermission(IUser, Permission)
   */
  boolean hasGlobalPermission(UserAuthenticationToken token, Permission permission);

}
