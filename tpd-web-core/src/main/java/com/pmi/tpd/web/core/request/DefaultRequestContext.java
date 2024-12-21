package com.pmi.tpd.web.core.request;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Default implementation of the {@link IRequestContext} interface.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
class DefaultRequestContext implements IRequestContext {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestContext.class);

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final List<Runnable> cleanupCallbacks;

    /** */
    private final IRequestInfoProvider delegate;

    /** */
    private final String id;

    /** */
    private final SortedSet<String> labels;

    /** */
    private volatile boolean active;

    DefaultRequestContext(@Nullable final IAuthenticationContext authenticationContext,
            @Nonnull final IRequestInfoProvider delegate, final String id) {
        this.authenticationContext = authenticationContext;
        this.delegate = delegate;
        this.id = id;

        active = true;
        cleanupCallbacks = Lists.newArrayList();
        labels = Sets.newTreeSet();
    }

    @Override
    public void addCleanupCallback(@Nonnull final Runnable callback) {
        cleanupCallbacks.add(callback);
    }

    @Override
    public void addLabel(@Nonnull final String label) {
        labels.add(label);
        MDC.put(ApplicationConstants.Logging.MDC_REQUEST_LABELS, StringUtils.join(labels, ", ").replace("|", "\\|"));
    }

    @Nonnull
    @Override
    public String getAction() {
        return delegate.getAction();
    }

    @Override
    public Optional<IAuthenticationContext> getAuthenticationContext() {
        return ofNullable(authenticationContext);
    }

    @Override
    public String getDetails() {
        return delegate.getDetails();
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    @Override
    public String getProtocol() {
        return delegate.getProtocol();
    }

    @Nonnull
    @Override
    public Object getRawRequest() {
        return delegate.getRawRequest();
    }

    @Nonnull
    @Override
    public Object getRawResponse() {
        return delegate.getRawResponse();
    }

    @Override
    public String getRemoteAddress() {
        return delegate.getRemoteAddress();
    }

    @Override
    public String getSessionId() {
        return delegate.getSessionId();
    }

    @Override
    public boolean hasSessionId() {
        return delegate.hasSessionId();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isSecure() {
        return delegate.isSecure();
    }

    public void runCleanupCallbacks() {
        for (final Runnable callback : cleanupCallbacks) {
            try {
                callback.run();
            } catch (final Exception e) {
                LOGGER.warn("Request cleanup callback failed", e);
            }
        }
        active = false;
    }
}
