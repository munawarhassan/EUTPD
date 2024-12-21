package com.pmi.tpd.core.user.permission;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.permission.IPermissionService;

/**
 * A utility service for plugin developer to validate that the current user has a specific permission. This service uses
 * the {@link IPermissionService} to check for a permission and will throw {@link AuthorisationException} for any failed
 * permission check.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IPermissionValidationService {

    /**
     * validate that the user is logged in.
     *
     * @throws AuthorisationException
     *             if anonymous
     * @see com.pmi.tpd.core.security.IAuthenticationContext#isAuthenticated()0
     */
    void validateAuthenticated() throws AuthorisationException;

    /**
     * validate that the user is not logged in.
     *
     * @throws AuthorisationException
     *             if authenticated
     * @see com.pmi.tpd.core.security.IAuthenticationContext#isAuthenticated()0
     */
    void validateAnonymous() throws AuthorisationException;

    /**
     * validate that the current user has the specified global permission.
     *
     * @param permission
     *            the requested permission. Must not be {@code null} and {@link Permission#isGlobal()} must be true
     * @throws AuthorisationException
     *             if the current user is unauthorized
     * @see IPermissionService#hasGlobalPermission(Permission)
     */
    void validateForGlobal(@Nonnull Permission permission) throws AuthorisationException;

}
