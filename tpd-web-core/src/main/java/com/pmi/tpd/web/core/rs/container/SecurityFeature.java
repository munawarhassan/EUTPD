package com.pmi.tpd.web.core.rs.container;

import java.io.IOException;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.glassfish.jersey.model.AnnotatedMethod;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.security.ForbiddenException;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class SecurityFeature implements DynamicFeature {

    private final I18nService i18nService;

    @Inject
    public SecurityFeature(final I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext configuration) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        // DenyAll on the method take precedence over RolesAllowed and PermitAll
        if (am.isAnnotationPresent(DenyAll.class)) {
            configuration.register(new AuthenticationFilter());
            return;
        }

        // RolesAllowed on the method takes precedence over PermitAll
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new AuthenticationFilter(ra.value()));
            return;
        }

        // PermitAll takes precedence over RolesAllowed on the class
        if (am.isAnnotationPresent(PermitAll.class)) {
            // Do nothing.
            return;
        }

        // DenyAll can't be attached to classes

        // RolesAllowed on the class takes precedence over PermitAll
        ra = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new AuthenticationFilter(ra.value()));
        }
    }

    @Priority(Priorities.AUTHORIZATION)
    private class AuthenticationFilter implements ContainerRequestFilter {

        private final boolean denyAll;

        private final String[] rolesAllowed;

        AuthenticationFilter() {
            this.denyAll = true;
            this.rolesAllowed = null;
        }

        AuthenticationFilter(final String[] rolesAllowed) {
            this.denyAll = false;
            this.rolesAllowed = rolesAllowed != null ? rolesAllowed : new String[] {};
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            if (!denyAll) {
                for (final String role : rolesAllowed) {
                    if (requestContext.getSecurityContext().isUserInRole(role)) {
                        return;
                    }
                }
            }

            throw new ForbiddenException(i18nService.createKeyedMessage("app.rest.notAuthorised"));
        }
    }
}
