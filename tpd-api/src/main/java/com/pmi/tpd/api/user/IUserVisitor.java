package com.pmi.tpd.api.user;

import javax.annotation.Nonnull;

/**
 * <p>
 * IUserVisitor interface.
 * </p>
 *
 * @param <T>
 *            a returned object type.
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUserVisitor<T> {

  /**
   * @param user
   *             a {@link User} object.
   * @return a T object.
   */
  T visit(@Nonnull IUser user);
}
