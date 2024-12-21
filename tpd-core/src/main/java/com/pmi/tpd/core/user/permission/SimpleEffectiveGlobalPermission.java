package com.pmi.tpd.core.user.permission;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.IEffectiveGlobalPermission;
import com.pmi.tpd.security.permission.IEffectivePermissionVisitor;

/**
 * @author Christophe Friederich
 */
public class SimpleEffectiveGlobalPermission extends SimpleEffectivePermissionBase
        implements IEffectiveGlobalPermission {

    /**
     * @param permission
     */
    public SimpleEffectiveGlobalPermission(@Nonnull final Permission permission) {
        super(permission);
    }

    @Override
    public <T> T accept(@Nonnull final IEffectivePermissionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
