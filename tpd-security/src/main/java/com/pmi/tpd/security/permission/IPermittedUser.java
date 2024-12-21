package com.pmi.tpd.security.permission;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;

/**
 * Defines an association between a {@link IUser} and a {@link Permission} which
 * has been granted to them.
 *
 * @see IPermittedGroup
 */
public interface IPermittedUser {

  /**
   * Retrieves the permission which has been granted to the user.
   *
   * @return the granted permission
   */
  @Nonnull
  Permission getPermission();

  /**
   * Retrieves the user to which the permission has been granted.
   * <p>
   * Note, the user may not be {@link IUser#isActivated() active}
   *
   * @return the permitted user
   */
  @Nonnull
  IUser getUser();
}
