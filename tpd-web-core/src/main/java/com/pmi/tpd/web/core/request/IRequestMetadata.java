package com.pmi.tpd.web.core.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for providing immutable request meta data to {@link IRequestManager}. These details are used to set up
 * auditing, logging and profiling.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRequestMetadata {

    /**
     * @return the URL or command which was requested, omitting any scheme, host, port and context path information.
     *         Query parameters for the request should also not be returned, as they may contain sensitive data
     */
    @Nonnull
    String getAction();

    /**
     * @return any further details associated with the request that are relevant, or {@code null} if not relevant
     */
    @Nullable
    String getDetails();

    /**
     * @return the protocol of the request
     */
    @Nonnull
    String getProtocol();

    /**
     * @return a comma-delimited string containing 1 or more IP addresses for the provided request, or {@code null} if
     *         this information is not available
     */
    @Nullable
    String getRemoteAddress();

    /**
     * @return a unique identifier for the session associated with the provided request, or {@code null} if there is no
     *         session available
     */
    @Nullable
    String getSessionId();

    /**
     * @return {@code true} iff a session is available
     */
    boolean hasSessionId();

    /**
     * @return {@code true} if the request is being made over a secure protocol; otherwise, {@code false}
     */
    boolean isSecure();

}
