package com.pmi.tpd.api.user;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;

import lombok.EqualsAndHashCode;

/**
 * <p>
 * User class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode
public class User implements IUser, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final Long id;

  /** */
  private final String username;

  /** */
  private final String slug;

  /** */
  private final String password;

  /** */
  private final boolean activated;

  /** */
  private final String email;

  /** */
  private final String displayName;

  /** */
  private final UserDirectory directory;

  /**
   * Create new instance and initialise with
   * {@link com.pmi.tpd.core.model.user.UserEntity.Builder}.
   *
   * @param builder
   *                contains value to initialise a new
   *                {@link com.pmi.tpd.core.model.user.UserEntity UserEntity}
   *                instance.
   */
  public User(@Nonnull final Builder builder) {
    this.id = builder.id();
    this.slug = builder.slug;
    this.email = builder.email;
    this.displayName = builder.displayName;
    this.activated = builder.activated;
    this.username = builder.username;
    this.password = builder.password;
    this.directory = builder.directory;
  }

  /** {@inheritDoc} */
  @Override
  public <T> T accept(final IUserVisitor<T> visitor) {
    return visitor.visit(this);
  }

  /** {@inheritDoc} */
  @Override
  public String getEmail() {
    return email;
  }

  /** {@inheritDoc} */
  @Override
  public String getDisplayName() {
    return displayName;
  }

  /** {@inheritDoc} */
  @Override
  public Long getId() {
    return id;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return getUsername();
  }

  /** {@inheritDoc} */
  @Override
  public String getSlug() {
    return slug;
  }

  /** {@inheritDoc} */
  @Override
  public String getUsername() {
    return username;
  }

  /** {@inheritDoc} */
  @Override
  public String getPassword() {
    return password;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isActivated() {
    return activated;
  }

  /** {@inheritDoc} */
  @Override
  public UserDirectory getDirectory() {
    return directory;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("displayName", displayName)
        .add("username", username)
        .add("email", email)
        .add("activated", activated)
        .add("directory", directory)
        .toString();
  }

  /**
   * Create new instance of {@link Builder}.
   *
   * @return Returns new instance of {@link Builder}.
   * @since 1.3
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * @param user
   *             a user to use
   * @return Return new instance of {@link Builder} for the specific {@code user}.
   */
  public static Builder builder(final IUser user) {
    return new Builder(user);
  }

  /**
   * @return Return new instance of {@link Builder} for this specific instance.
   */
  public Builder copy() {
    return new Builder(this);
  }

  /**
   * @author Christophe Friederich
   */
  public static class Builder extends UserBuilder<Builder> {

    /**
     *
     */
    private Builder() {
      super();
    }

    /**
     * @param user
     *             a user to use.
     */
    public Builder(final IUser user) {
      super(user);
    }

    /**
     * @param user
     *             a user to use.
     */
    public Builder(final User user) {
      super(user);
    }

    @Override
    public Builder id(final Long value) {
      return super.id(value);
    }

    @Override
    public IUser build() {
      return new User(this);
    }

    @Override
    protected Builder self() {
      return this;
    }

  }

}
