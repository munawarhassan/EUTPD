package com.pmi.tpd.security.spring;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.Permission;

/**
 * Spring security Authentication token that holds the user for the logged in
 * user, and a collection of global
 * {@link Permission}s that have been granted to the current user session. This
 * mechanism is used to temporally run with
 * elevated permissions.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class UserAuthenticationToken extends UsernamePasswordAuthenticationToken implements IPermissionGraph {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final IPermissionGraph elevatedPermissions;

  /** */
  private final IUser user;

  /** */
  private IPermissionGraph permissions;

  protected UserAuthenticationToken(final Builder builder) {
    super(builder.user, null, builder.userDetails != null ? builder.userDetails.getAuthorities()
        : Collections.<GrantedAuthority>emptySet());
    this.user = builder.user;
    this.elevatedPermissions = builder.elevatedPermissions;
    this.permissions = builder.permissions;
  }

  /**
   * @param user
   * @return
   */
  public static UserAuthenticationToken forUser(final IUser user) {
    return new Builder().user(user).build();
  }

  /**
   * @param user
   * @param userDetails
   * @return
   */
  public static UserAuthenticationToken forUser(final IUser user, final UserDetails userDetails) {
    return new Builder().user(user).userDetails(userDetails).build();
  }

  /**
   * @param user
   * @return
   */
  @Nonnull
  public UserAuthenticationToken copyWithUser(@Nullable final IUser user) {
    return new UserAuthenticationToken(new Builder(this).user(user));
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  /**
   * @return
   */
  public IPermissionGraph getElevatedPermissions() {
    return elevatedPermissions;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return user == null ? "<anonymous>" : user.getName();
  }

  /**
   * @return
   */
  public IPermissionGraph getPermissions() {
    return permissions;
  }

  @Override
  public IUser getPrincipal() {
    return user;
  }

  @Override
  public boolean isGranted(final Permission permission, @Nullable final Object resource) {
    return isGranted(elevatedPermissions, permission, resource) || isGranted(permissions, permission, resource);
  }

  /**
   * @param permissions
   */
  public void setPermissions(final IPermissionGraph permissions) {
    this.permissions = permissions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("user", user)
        .add("elevatedPermissions", elevatedPermissions)
        .toString();
  }

  private boolean isGranted(final IPermissionGraph graph, final Permission permission, final Object resource) {
    return graph != null && permission != null && graph.isGranted(permission, resource);
  }

  /**
   * @return
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * @author Christophe Friederich
   */
  public static class Builder {

    /** */
    private IPermissionGraph permissions;

    /** */
    private IPermissionGraph elevatedPermissions;

    /** */
    private IUser user;

    /** */
    private UserDetails userDetails;

    private Builder() {

    }

    /**
     * @param token
     */
    public Builder(final UserAuthenticationToken token) {
      elevatedPermissions = token.elevatedPermissions;
      permissions = token.permissions;
      user = token.user;
      userDetails = token.getDetails() instanceof UserDetails ? (UserDetails) token.getDetails() : null;
    }

    /**
     * @return
     */
    public UserAuthenticationToken build() {
      final UserAuthenticationToken token = new UserAuthenticationToken(this);
      token.setDetails(userDetails);
      return token;
    }

    /**
     * @param value
     * @return
     */
    public Builder elevatedPermissions(final IPermissionGraph value) {
      elevatedPermissions = value;
      return this;
    }

    /**
     * @param value
     * @return
     */
    public Builder permissions(final IPermissionGraph value) {
      permissions = value;
      return this;
    }

    /**
     * @param value
     * @return
     */
    public Builder user(final IUser value) {
      user = value;
      return this;
    }

    /**
     * @param value
     * @return
     */
    public Builder userDetails(final UserDetails value) {
      userDetails = value;
      return this;
    }
  }

}
