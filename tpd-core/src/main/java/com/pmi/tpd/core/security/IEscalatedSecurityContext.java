package com.pmi.tpd.core.security;

import java.util.Set;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.util.IOperation;

/**
 * Custom security context that specifies as what user and with which additional permissions an {@link IOperation}
 * should be called.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IEscalatedSecurityContext {

    /**
     * Executes an Operation as the specified user and added permissions. The current security context is restored after
     * the operation completes.
     * <p>
     * Note: Nesting invocations merges all elevated permissions rather than replacing the previous values.
     *
     * @param operation
     *            the operation to be executed
     * @return the return value of the provided operation
     * @throws E
     *             when the provided operation throws an exception
     */
    <T, E extends Throwable> T call(@Nonnull IOperation<T, E> operation) throws E;

    /**
     * Escalates the security context as the specified user and added permissions for the duration of the current
     * request. If there is no request in scope an exception is thrown.
     *
     * @throws java.lang.IllegalStateException
     *             if there is {@link com.pmi.tpd.web.request.spi.IRequestContext#isActive() no request} in scope for
     *             the current thread
     */
    void applyToRequest();

    /**
     * Adds permissions to the set of elevated permissions. If a resource permission is provided, the permission is
     * granted to all resources.
     *
     * @param permission
     *            the permission to be temporarily granted
     * @return the escalated security context
     */
    @Nonnull
    IEscalatedSecurityContext withPermission(@Nonnull Permission permission);

    /**
     * Adds permissions on a particular resource to the set of elevated permissions.
     *
     * @param resource
     *            the resource
     * @param permission
     *            the permission to be temporarily granted on the provided resource
     * @return the escalated security context
     */
    @Nonnull
    IEscalatedSecurityContext withPermission(@Nonnull Object resource, @Nonnull Permission permission);

    /**
     * Adds permissions to the set of elevated permissions. If a resource permission is provided, the permission is
     * granted to all resources.
     *
     * @param permissions
     *            the permissions to be temporarily granted
     * @return the escalated security context
     */
    @Nonnull
    IEscalatedSecurityContext withPermissions(@Nonnull Set<Permission> permissions);
}
