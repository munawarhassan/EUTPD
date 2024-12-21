package com.pmi.tpd.core.user.permission;

import javax.annotation.Nonnull;

import org.springframework.security.access.prepost.PreAuthorize;

import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.annotation.AnonymousRequired;
import com.pmi.tpd.security.permission.Permission;

/**
 * Implementation of {@link IPermissionValidationService} which uses Spring Security annotations and exception rewrite
 * advice to throw {@link AuthorisationException}.
 */

public class PermissionValidationServiceImpl implements IPermissionValidationService {

    @Override
    @PreAuthorize("isAuthenticated()")
    public void validateAuthenticated() throws AuthorisationException {
        // Noop. Annotation driven
    }

    @Override
    @PreAuthorize("isAnonymous()")
    @AnonymousRequired
    public void validateAnonymous() throws AuthorisationException {
        // Noop. Annotation driven
    }

    @Override
    @PreAuthorize("hasGlobalPermission(#permission)")
    public void validateForGlobal(@Nonnull final Permission permission) throws AuthorisationException {
        // Noop. Annotation driven
    }

}
