package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.pmi.tpd.security.permission.Permission;

public abstract class SimpleEffectivePermissionBase {

  protected final Permission permission;

  public SimpleEffectivePermissionBase(@Nonnull final Permission permission) {
    this.permission = checkNotNull(permission, "permission");
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && getClass().equals(obj.getClass())) {
      return Objects.equal(permission, ((SimpleEffectivePermissionBase) obj).permission);
    }

    return false;
  }

  @Nonnull
  public Permission getPermission() {
    return permission;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).addValue(permission).toString();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(permission);
  }
}
