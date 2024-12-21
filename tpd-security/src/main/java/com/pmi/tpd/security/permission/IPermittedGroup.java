package com.pmi.tpd.security.permission;

import javax.annotation.Nonnull;

/**
 * Defines an association between a group and a {@link Permission} which has
 * been granted to it.
 *
 * @see PermittedUser
 */
public interface IPermittedGroup {

  /**
   * Retrieves the group to which the permission has been granted.
   *
   * @return the permitted group
   */
  @Nonnull
  String getGroup();

  /**
   * Retrieves the permission which has been granted to the group.
   *
   * @return the granted permission
   */
  @Nonnull
  Permission getPermission();
}
