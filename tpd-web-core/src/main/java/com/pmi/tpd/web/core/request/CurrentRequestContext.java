package com.pmi.tpd.web.core.request;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Component that provides easy access to the {@link IRequestManager#getRequestContext() current request context}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class CurrentRequestContext implements IRequestContext, ApplicationListener<ContextRefreshedEvent> {

    /** */
    private IRequestManager requestManager;

    @Override
    public void addCleanupCallback(@Nonnull final Runnable callback) {
        current().addCleanupCallback(checkNotNull(callback, "callback"));
    }

    @Override
    public void addLabel(@Nonnull final String label) {
        current().addLabel(label);
    }

    @Override
    public Optional<IAuthenticationContext> getAuthenticationContext() {
        return current().getAuthenticationContext();
    }

    @Override
    @Nonnull
    public String getAction() {
        return current().getAction();
    }

    @Override
    @Nullable
    public String getDetails() {
        return current().getDetails();
    }

    @Override
    @Nonnull
    public String getId() {
        return current().getId();
    }

    @Override
    @Nonnull
    public String getProtocol() {
        return current().getProtocol();
    }

    @Override
    @Nonnull
    public Object getRawRequest() {
        return current().getRawRequest();
    }

    @Override
    @Nonnull
    public Object getRawResponse() {
        return current().getRawResponse();
    }

    @Override
    @Nullable
    public String getRemoteAddress() {
        return current().getRemoteAddress();
    }

    @Override
    @Nullable
    public String getSessionId() {
        return current().getSessionId();
    }

    @Override
    public boolean hasSessionId() {
        return current().hasSessionId();
    }

    @Override
    public boolean isActive() {
        return requestManager != null && requestManager.getRequestContext() != null;
    }

    @Override
    public boolean isSecure() {
        return current().isSecure();
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        requestManager = event.getApplicationContext().getBean(IRequestManager.class);
    }

    private IRequestContext current() {
        final IRequestContext context = requestManager.getRequestContext();
        checkState(context != null, "There is no current IRequestContext");
        return context;
    }
}
