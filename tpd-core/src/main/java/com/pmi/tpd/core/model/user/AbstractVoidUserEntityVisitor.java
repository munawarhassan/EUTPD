package com.pmi.tpd.core.model.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.IUserVisitor;
import com.pmi.tpd.api.user.User;

/**
 * <p>
 * Abstract AbstractVoidUserEntityVisitor class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class AbstractVoidUserEntityVisitor implements IUserVisitor<Void> {

  public Void visit(@Nonnull final UserEntity user) {
    doVisit(user);
    return null;
  }

  public Void visit(@Nonnull final User user) {
    doVisit(user);
    return null;
  }

  @Override
  public Void visit(@Nonnull final IUser user) {
    doVisit(user);
    return null;
  }

  /**
   * <p>
   * doVisit.
   * </p>
   *
   * @param user
   *             a {@link UserEntity} object.
   */
  protected void doVisit(@Nonnull final UserEntity user) {

  }

  /**
   * <p>
   * doVisit.
   * </p>
   *
   * @param user
   *             a {@link User} object.
   */
  protected void doVisit(@Nonnull final User user) {

  }

  /**
   * <p>
   * doVisit.
   * </p>
   *
   * @param user
   *             a {@link IUser} object.
   */
  protected void doVisit(@Nonnull final IUser user) {

  }

}
