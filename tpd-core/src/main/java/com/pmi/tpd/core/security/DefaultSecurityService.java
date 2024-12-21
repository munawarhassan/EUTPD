package com.pmi.tpd.core.security;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.event.RequestEndedEvent;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.IOperation;
import com.pmi.tpd.core.user.IUserService;

/**
 * <p>
 * DefaultSecurityService class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Named("DefaultSecurityService")
@Transactional(readOnly = true)
public class DefaultSecurityService implements ISecurityService {

    /** */
    private final I18nService i18nService;

    /** */
    private final IRequestManager requestManager;

    /** */
    private final IUserService userService;

    /**
     * <p>
     * Constructor for DefaultSecurityService.
     * </p>
     *
     * @param requestManager
     * @param userService
     * @param i18nService
     */
    @Inject
    public DefaultSecurityService(@Nonnull final I18nService i18nService, @Nonnull final IRequestManager requestManager,
            @Nonnull final IUserService userService) {
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.requestManager = checkNotNull(requestManager, "requestManager");
        this.userService = checkNotNull(userService, "userService");
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext anonymously(@Nonnull final String reason) {
        return contextBuilder(reason).anonymously().build();
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext

            impersonating(@Nonnull final IUser user, @Nonnull final String reason) {
        return contextBuilder(reason).impersonating(user).build();
    }

    @Override
    public <T, E extends Throwable> T doAnonymously(@Nonnull final String reason,
        @Nonnull final IOperation<T, E> operation) throws E {
        return anonymously(reason).call(operation);
    }

    @Override
    public <T, E extends Throwable> T doAsUser(@Nonnull final String reason,
        @Nonnull final String username,
        @Nonnull final IOperation<T, E> operation) throws E {
        final IUser runAsUser = userService.getUserByName(checkNotNull(username, "username"));
        if (runAsUser == null) {
            final KeyedMessage message = i18nService.createKeyedMessage("app.service.user.preauthfail", username);
            throw new PreAuthenticationFailedException(message);
        }
        return impersonating(runAsUser, reason).call(operation);
    }

    @Override
    public <T, E extends Throwable> T doWithPermission(@Nonnull final String reason,
        @Nonnull final Permission permission,
        @Nonnull final IOperation<T, E> operation) throws E {
        return withPermission(permission, reason).call(operation);
    }

    @Override
    public <T, E extends Throwable> T doWithPermissions(@Nonnull final String reason,
        @Nonnull final Collection<Permission> permissions,
        @Nonnull final IOperation<T, E> operation) throws E {
        return withPermissions(EnumSet.copyOf(permissions), reason).call(operation);
    }

    /**
     * Ensure the {@code SecurityContextHolder} is cleared when the request ends.
     *
     * @param event
     *            ignored
     */
    @EventListener
    public void onRequestEnded(final RequestEndedEvent event) {
        SecurityContextHolder.clearContext();
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext withPermission(@Nonnull final Permission permission,
        @Nonnull final String reason) {
        return contextBuilder(reason).withPermission(permission).build();
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext withPermission(@Nonnull final Permission permission,
        @Nonnull final Object resource,
        @Nonnull final String reason) {
        return contextBuilder(reason).withPermission(resource, permission).build();
    }

    @Nonnull
    @Override
    public IEscalatedSecurityContext withPermissions(@Nonnull final Set<Permission> permissions,
        @Nonnull final String reason) {
        return contextBuilder(reason).withPermissions(permissions).build();
    }

    private DefaultEscalatedSecurityContext.Builder contextBuilder(final String reason) {
        return new DefaultEscalatedSecurityContext.Builder(reason, requestManager);
    }
}
