package com.pmi.tpd.security.permission;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.user.IUser;

/**
 * Used to set a permission to multiple users and/or groups.
 *
 * @see IPermissionAdminService#setPermission(SetPermissionRequest)
 * @author Christophe Friederich
 * @since 2.0
 */
public final class SetPermissionRequest {

  /** */
  private final Permission permission;

  /** */
  private final Set<IUser> users;

  /** */
  private final Set<String> groups;

  private SetPermissionRequest(@Nonnull final Permission permission, @Nonnull final Set<IUser> users,
      @Nonnull final Set<String> groups) {
    checkNotNull(permission, "permission");
    isTrue(!(users.isEmpty() && groups.isEmpty()), "either a user or a group must be specified");
    this.permission = permission;
    this.groups = groups;
    this.users = users;

  }

  /**
   * @return
   */
  @Nonnull
  public Permission getPermission() {
    return permission;
  }

  /**
   * @return
   */
  @Nonnull
  public Set<IUser> getUsers() {
    return users;
  }

  /**
   * @return
   */
  @Nonnull
  public Set<String> getGroups() {
    return groups;
  }

  /**
   * {@inheritDoc
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(permission, users, groups);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final SetPermissionRequest other = (SetPermissionRequest) obj;
    return Objects.equal(this.permission, other.permission) && Objects.equal(this.users, other.users)
        && Objects.equal(this.groups, other.groups);
  }

  /**
   * @author Christophe Friederich
   */
  public static class Builder {

    /** */
    private Permission permission;

    /** */
    private Set<IUser> users;

    /** */
    private Set<String> groups;

    /**
     *
     */
    public Builder() {
    }

    /**
     * @param permission
     * @return
     */
    public Builder globalPermission(@Nonnull final Permission permission) {
      isTrue(permission.isGlobal(), "not a global permission");
      this.permission = permission;
      return this;
    }

    /**
     * @param user
     * @return
     */
    public Builder user(@Nonnull final IUser user) {
      // use a Precondition rather than relying on the NPE thrown by
      // ImmutableSet.of(null)
      this.users = ImmutableSet.of(checkNotNull(user, "user"));
      return this;
    }

    /**
     * @param users
     * @return
     */
    public Builder users(@Nonnull final Iterable<IUser> users) {
      // use a Precondition rather than relying on the NPE thrown by
      // ImmutableSet.copyOf({null})
      isTrue(Iterables.all(users, Predicates.notNull()), "can not have null users");
      this.users = ImmutableSet.copyOf(users);
      return this;
    }

    /**
     * @param group
     * @return
     */
    public Builder group(@Nonnull final String group) {
      isTrue(NOT_EMPTY.apply(group), "can not have null or empty group");
      this.groups = ImmutableSet.of(group);
      return this;
    }

    /**
     * @param groups
     * @return
     */
    public Builder groups(@Nonnull final Iterable<String> groups) {
      isTrue(Iterables.all(groups, NOT_EMPTY), "can not have null or empty groups");
      this.groups = ImmutableSet.copyOf(groups);
      return this;
    }

    /**
     * @return
     */
    public SetPermissionRequest build() {
      final Set<IUser> users = this.users == null ? ImmutableSet.<IUser>of() : this.users;
      final Set<String> groups = this.groups == null ? ImmutableSet.<String>of() : this.groups;
      return new SetPermissionRequest(permission, users, groups);
    }
  }

  /** */
  private static final Predicate<String> NOT_EMPTY = s -> !Strings.isNullOrEmpty(s);
}
