package com.pmi.tpd.core.security;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.IOperation;

/**
 * <p>
 * ISecurityService interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface ISecurityService {

    /**
     * Creates a custom security context that is not authenticated that can be used to perform operations. Permissions
     * that have been granted to the current user will no longer apply. Note that any
     * {@link #withPermission(Permission, String) escalated permissions} that are associated with the current security
     * context will still apply.
     *
     * @param reason
     *            a description of the reason for creating the custom security context. This description is used for
     *            logging.
     * @return the custom security context
     * @since 2.0
     */
    @Nonnull
    IEscalatedSecurityContext anonymously(@Nonnull String reason);

    /**
     * Creates a custom security context that is authenticated as {@code user} that can be used to perform operations.
     * Note that any {@link #withPermission(Permission, String) escalated permissions} that are associated with the
     * current security context will still apply.
     *
     * @param user
     *            the user to temporarily execute as
     * @param reason
     *            a description of the reason for creating the custom security context. This description is used for
     *            logging.
     * @return the custom security context
     * @since 2.0
     */
    @Nonnull
    IEscalatedSecurityContext impersonating(@Nonnull IUser user, @Nonnull String reason);

    /**
     * Creates a custom security context with elevated permissions that can be used to perform operations. The custom
     * context is still authenticated as the current user (if there is one). Note that any escalated permissions
     * associated with the current security context will still apply.
     *
     * @param permission
     *            the permission to temporarily grant. This can be either a global or a resource permission. If a
     *            resource permission is provided, the permission is granted to all resources of that type.
     * @param reason
     *            a description of the reason for creating the custom security context. This description is used for
     *            logging.
     * @return the custom security context
     * @since 2.0
     */
    @Nonnull
    IEscalatedSecurityContext withPermission(@Nonnull Permission permission, @Nonnull String reason);

    /**
     * Creates a custom security context with elevated permissions that can be used to perform operations. The custom
     * context is still authenticated as the current user (if there is one). Note that any escalated permissions that
     * are associated with the current security context will still apply.
     *
     * @param permission
     *            the permission to temporarily grant. This has to be a resource permission that is valid for the
     *            provided resource.
     * @param resource
     *            the resource to temporarily grant the permission on
     * @param reason
     *            a description of the reason for creating the custom security context. This description is used for
     *            logging.
     * @return the custom security context
     * @since 2.0
     */
    @Nonnull
    IEscalatedSecurityContext withPermission(@Nonnull Permission permission,
        @Nonnull Object resource,
        @Nonnull String reason);

    /**
     * Creates a custom security context with elevated permissions that can be used to perform operations. The custom
     * context is still authenticated as the current user (if there is one). Note that any
     * {@link #withPermission(Permission, String) escalated permissions} that are associated with the current security
     * context will still apply.
     *
     * @param permissions
     *            the permissions to temporarily grant. This can be a mix of global and resource permissions. If a
     *            resource permission is provided, the permission is granted to all resources of that type.
     * @param reason
     *            a description of the reason for creating the custom security context. This description is used for
     *            logging.
     * @return the custom security context
     * @since 2.0
     */
    @Nonnull
    IEscalatedSecurityContext withPermissions(@Nonnull Set<Permission> permissions, @Nonnull String reason);

    /**
     * Execute an Operation anonymously (not as any user)
     *
     * @param reason
     *            reason for running as a different user. Helpful for logging/debugging
     * @param operation
     *            the Operation to perform.
     * @return the return value of Operation.perform()
     * @throws E
     *             when thrown by Operation.perform()
     * @since 2.0
     */
    <T, E extends Throwable> T doAnonymously(@Nonnull String reason, @Nonnull IOperation<T, E> operation) throws E;

    /**
     * Execute an Operation as a particular user.
     *
     * @param reason
     *            reason for running as a different user. Helpful for logging/debugging
     * @param userName
     *            a String naming the user to impersonate.
     * @param operation
     *            the Operation to perform.
     * @return the return value of Operation.perform()
     * @throws E
     *             when thrown by Operation.perform()
     * @throws PreAuthenticationFailedException
     *             when authenticating as {@code userName} failed
     * @since 2.0
     */
    <T, E extends Throwable> T doAsUser(@Nonnull String reason,
        @Nonnull String userName,
        @Nonnull IOperation<T, E> operation) throws E;

    /**
     * Executes an Operation as the current user, but with elevated permission.
     * <p>
     * Note: nested invocations results in all elevated permissions taking effect rather than replacing the previous
     * elevated permissions
     *
     * @param reason
     *            the reason for the elevation of permissions. Helpful for logging/debugging
     * @param permission
     *            the permission to temporarily grant to the current user.
     * @param operation
     *            the operation to perform
     * @return the return value of Operation.perform()
     * @throws E
     *             when thrown by Operation.perform()
     * @since 2.0
     */
    <T, E extends Throwable> T doWithPermission(@Nonnull String reason,
        @Nonnull Permission permission,
        @Nonnull IOperation<T, E> operation) throws E;

    /**
     * Executes an Operation as the current user, but with elevated permission.
     * <p>
     * Note: nested invocations results in all elevated permissions taking effect rather than replacing the previous
     * elevated permissions
     *
     * @param reason
     *            the reason for the elevation of permissions. Helpful for logging/debugging
     * @param permissions
     *            the permissions to temporarily grant to the current user.
     * @param operation
     *            the operation to perform
     * @return the return value of Operation.perform()
     * @throws E
     *             when thrown by Operation.perform()
     * @since 2.0
     */
    <T, E extends Throwable> T doWithPermissions(@Nonnull String reason,
        @Nonnull Collection<Permission> permissions,
        @Nonnull IOperation<T, E> operation) throws E;

}
