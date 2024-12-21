package com.pmi.tpd.security.permission;

/**
 * @since 2.0
 */
public class AbstractEffectivePermissionVisitor<T> implements IEffectivePermissionVisitor<T> {

    @Override
    public T visit(final IEffectiveGlobalPermission permission) {
        return null;
    }

    @Override
    public T visit(final IEffectiveProductPermission permission) {
        return null;
    }

    @Override
    public T visit(final IEffectiveSubmissionPermission permission) {
        return null;
    }

}
