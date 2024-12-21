package com.pmi.tpd.api.user;

import java.security.Principal;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.model.AbstractEntityBuilder;
import com.pmi.tpd.api.model.IIdentityEntity;

/**
 * <p>
 * IUser interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUser extends IIdentityEntity<Long>, Principal, IPerson {

  /**
   * <p>
   * accept.
   * </p>
   *
   * @param visitor
   *                a {@link com.pmi.tpd.core.user.IUserVisitor} object.
   * @param <T>
   *                a T object.
   * @return a T object.
   */
  <T> T accept(@Nonnull IUserVisitor<T> visitor);

  /**
   * <p>
   * getSlug.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getSlug();

  /**
   * <p>
   * getEmail.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Override
  String getEmail();

  /**
   * <p>
   * Gets the display name (first name + last name) of this user.
   * </p>
   * <p>
   * <b>Note:</b>if the first name or last name are null or empty, returns the
   * login name.
   * </p>
   *
   * @return Returns a {@link java.lang.String} representing the full name of this
   *         user.
   */
  String getDisplayName();

  /**
   * <p>
   * getLogin.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getUsername();

  /**
   * <p>
   * getPassword.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  String getPassword();

  /**
   * <p>
   * isActivated.
   * </p>
   *
   * @return a boolean.
   */
  boolean isActivated();

  /**
   * @return Returns the user directory associated to.
   */
  UserDirectory getDirectory();

  /**
   * @author Christophe Friederich
   * @param <B>
   */
  public abstract class UserBuilder<B extends AbstractEntityBuilder<Long, IUser, B>>
      extends AbstractEntityBuilder<Long, IUser, B> {

    /** */
    protected String slug;

    /** */
    protected String email;

    /** */
    protected String displayName;

    /** */
    protected String username;

    /** */
    protected String password;

    /** */
    protected boolean activated;

    /** */
    protected UserDirectory directory;

    /**
     *
     */
    public UserBuilder() {
    }

    /**
     *
     */
    public UserBuilder(@Nonnull final IUser user) {
      super(user);
      this.email = user.getEmail();
      this.displayName = user.getDisplayName();
      this.slug = user.getSlug();
      this.activated = user.isActivated();
      this.username = user.getUsername();
      this.password = user.getPassword();
      this.directory = user.getDirectory() == null ? UserDirectory.defaultDirectory() : user.getDirectory();
    }

    public B activated(final boolean activated) {
      this.activated = activated;
      return self();
    }

    public B email(final String email) {
      this.email = email;
      return self();
    }

    public B displayName(final String displayName) {
      this.displayName = displayName;
      return self();
    }

    public B username(final String username) {
      this.username = username;
      return self();
    }

    public B slug(final String slug) {
      this.slug = slug;
      return self();
    }

    public B password(final String password) {
      this.password = password;
      return self();
    }

    public B directory(final UserDirectory directory) {
      this.directory = directory;
      return self();
    }

  }
}
