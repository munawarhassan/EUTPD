package com.pmi.tpd.security.spring;

import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * Extending Spring's {@link AuthenticationTrustResolverImpl} to check correctly for anonymous access due to non-null
 * {@link UserAuthenticationToken authentication} when using
 * {@link com.pmi.tpd.core.security.ISecurityService#withPermission}.
 * <p>
 * Note that this isn't used in
 * {@link org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper}, which checks for
 * anonymous authentication before returning the non-null object. Because the principal is also checked for null this
 * shouldn't be a problem.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class ExtendedAuthenticationTrustResolver extends AuthenticationTrustResolverImpl {

    public ExtendedAuthenticationTrustResolver() {
        setRememberMeClass(RememberMeUserAuthenticationToken.class);
    }

    @Override
    public boolean isAnonymous(final Authentication authentication) {
        if (authentication instanceof UserAuthenticationToken) {
            return ((UserAuthenticationToken) authentication).getPrincipal() == null;
        }
        return super.isAnonymous(authentication);
    }
}
