package com.pmi.tpd.core.security;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.IOperation;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.user.permission.CompositePermissionGraph;
import com.pmi.tpd.core.user.permission.DefaultPermissionGraph;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.security.permission.IPermissionGraph;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.spring.UserAuthenticationToken;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
final class DefaultEscalatedSecurityContext implements IEscalatedSecurityContext {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEscalatedSecurityContext.class);

    /** */
    private final DefaultPermissionGraph elevatedPermissions;

    /** */
    private final boolean impersonateUser;

    /** */
    private final String reason;

    /** */
    private final IRequestManager requestManager;

    /** */
    private final IUser user;

    private DefaultEscalatedSecurityContext(final Builder builder) {
        elevatedPermissions = builder.permissionsBuilder.build();
        impersonateUser = builder.impersonateUser;
        reason = builder.reason;
        requestManager = builder.requestManager;
        user = builder.user;
    }

    @Override
    public <T, E extends Throwable> T call(@Nonnull final IOperation<T, E> operation) throws E {
        checkNotNull(operation, "operation cannot be null");

        // store the current token so we can restore it later on
        final Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        final Authentication runAsToken = createRunAsToken();

        SecurityContextHolder.getContext().setAuthentication(runAsToken);
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("doWithPermission: running as {}", runAsToken);
            }
            return operation.perform();
        } finally {
            // restore the original token
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    @Override
    public void applyToRequest() {
        final IRequestContext requestContext = requestManager.getRequestContext();
        if (requestContext == null || !requestContext.isActive()) {
            throw new IllegalStateException("No request is active");
        }

        // The runAsToken will be cleared at the end of the request
        SecurityContextHolder.getContext().setAuthentication(createRunAsToken());
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext withPermission(@Nonnull final Permission permission) {
        return new Builder(this).withPermission(checkNotNull(permission, "permission")).build();
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext withPermission(@Nonnull final Object resource,
        @Nonnull final Permission permission) {
        return new Builder(this).withPermission(resource, permission).build();
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext withPermissions(@Nonnull final Set<Permission> permissions) {
        return new Builder(this).withPermissions(permissions).build();
    }

    private UserAuthenticationToken createRunAsToken() {
        final Authentication currentToken = SecurityContextHolder.getContext().getAuthentication();

        // create the custom token
        IPermissionGraph currentElevatedPermissions = null;
        IUser currentUser = null;
        if (currentToken instanceof UserAuthenticationToken) {
            final UserAuthenticationToken token = (UserAuthenticationToken) currentToken;
            currentElevatedPermissions = token.getElevatedPermissions();
            currentUser = token.getPrincipal();
        }

        return UserAuthenticationToken.builder()
                .user(impersonateUser ? user : currentUser)
                .elevatedPermissions(
                    CompositePermissionGraph.maybeCompose(currentElevatedPermissions, elevatedPermissions))
                .build();
    }

    /**
     * @author Christophe Friederich
     * @since 2.0
     */
    static class Builder {

        /** */
        private final IRequestManager requestManager;

        /** */
        private final String reason;

        /** */
        private final DefaultPermissionGraph.Builder permissionsBuilder;

        /** */
        private boolean impersonateUser;

        /** */
        private IUser user;

        Builder(@Nonnull final String reason, @Nonnull final IRequestManager requestManager) {
            this.requestManager = checkNotNull(requestManager, "requestManager");
            this.reason = checkNotNull(reason, "reason");

            permissionsBuilder = new DefaultPermissionGraph.Builder();
        }

        Builder(@Nonnull final DefaultEscalatedSecurityContext escalatedSecurityContext) {
            checkNotNull(escalatedSecurityContext, "escalatedSecurityContext");

            impersonateUser = escalatedSecurityContext.impersonateUser;
            permissionsBuilder = new DefaultPermissionGraph.Builder()
                    .addAll(escalatedSecurityContext.elevatedPermissions);
            reason = escalatedSecurityContext.reason;
            requestManager = escalatedSecurityContext.requestManager;
            user = escalatedSecurityContext.user;
        }

        @Nonnull
        Builder anonymously() {
            impersonateUser = true;
            user = null;
            return this;
        }

        @Nonnull
        DefaultEscalatedSecurityContext build() {
            return new DefaultEscalatedSecurityContext(this);
        }

        @Nonnull
        Builder impersonating(@Nonnull final IUser user) {
            impersonateUser = true;
            this.user = checkNotNull(user, "user");
            return this;
        }

        @Nonnull
        Builder withPermission(@Nonnull final Permission permission) {
            permissionsBuilder.add(checkNotNull(permission, "permission"), null);
            return this;
        }

        @Nonnull
        Builder withPermission(@Nonnull final Object resource, @Nonnull final Permission permission) {
            Integer resourceId;
            if (checkNotNull(resource, "resource") instanceof SubmissionEntity) {
                resourceId = ((ISubmissionEntity) resource).getId().intValue();
                isTrue(permission.isResource(SubmissionEntity.class), "Submission permission required");
            } else {
                throw new IllegalArgumentException(
                        "Only Submission resource is supported. Got " + resource.getClass().getCanonicalName());
            }
            permissionsBuilder.add(permission, resourceId);
            return this;
        }

        @Nonnull
        Builder withPermissions(@Nonnull final Iterable<Permission> permissions) {
            int index = 0;
            for (final Permission permission : checkNotNull(permissions, "permissions")) {
                permissionsBuilder.add(checkNotNull(permission, "permissions[{}]", index++), null);
            }
            return this;
        }
    }

}