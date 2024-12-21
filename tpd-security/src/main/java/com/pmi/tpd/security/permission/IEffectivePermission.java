package com.pmi.tpd.security.permission;

import javax.annotation.Nonnull;

/**
 * An effective permission is a permission that is either:
 * <ul>
 * <li>directly granted (as a permission associated with the user or as a
 * permission associated with a group the user is
 * a member of),</li>
 * <li>or indirectly granted through a
 * {@link Permission#getInheritedPermissions() inherited} permission (such as a
 * {@link Permission#SYS_ADMIN sysadmin} inheriting a {@link Permission#ADMIN
 * admin} permission).</li>
 * </ul>
 *
 * @since 2.0
 */
public interface IEffectivePermission {

  /**
   * @return the {@link Permission permission}
   */
  @Nonnull
  Permission getPermission();

  /**
   * Accepts the provided {@link IEffectivePermissionVisitor visitor} and invokes
   * the appropriate {@code visit}
   * overloaded method.
   *
   * @param visitor
   *                the visitor
   * @return the result of the invoked {@code visit} method
   */
  <T> T accept(@Nonnull IEffectivePermissionVisitor<T> visitor);
}
