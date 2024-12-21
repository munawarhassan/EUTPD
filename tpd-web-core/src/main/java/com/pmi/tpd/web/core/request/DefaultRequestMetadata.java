package com.pmi.tpd.web.core.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultRequestMetadata implements IRequestMetadata {

    /** */
    private final String action;

    /** */
    private final String details;

    /** */
    private final String protocol;

    /** */
    private final String remoteAddress;

    /** */
    private final String sessionId;

    /** */
    private final boolean secure;

    /**
     * @param source
     */
    public DefaultRequestMetadata(final IRequestMetadata source) {
        action = source.getAction();
        details = source.getDetails();
        protocol = source.getProtocol();
        remoteAddress = source.getRemoteAddress();
        sessionId = source.getSessionId();
        secure = source.isSecure();
    }

    @Nonnull
    @Override
    public String getAction() {
        return action;
    }

    @Nullable
    @Override
    public String getDetails() {
        return details;
    }

    @Nonnull
    @Override
    public String getProtocol() {
        return protocol;
    }

    @Nullable
    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Nullable
    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean hasSessionId() {
        return sessionId != null;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }
}
