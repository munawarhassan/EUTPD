package com.pmi.tpd.web.core.request.spi;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestInfoProvider;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * Provides information about a request, such as the requestId, sessionId, remoteAddress and more.
 * {@code IRequestContext} is currently used as an abstraction for HTTP and SSH requests, but can easily be used for
 * other protocols as well.
 *
 * @see IRequestManager
 * @since 1.3
 */
public interface IRequestContext extends IRequestInfoProvider {

    /**
     * Registers a cleanup task that is executed when the current request ends. Note that the callback will
     * <em>only</em> be called for the current request.
     *
     * @param callback
     *                 callback that should be called when the request ends
     */
    void addCleanupCallback(@Nonnull Runnable callback);

    /**
     * Adds a label to the request context that will be included in the access logs. Use this purely for diagnostic /
     * analysis purposes.
     *
     * @param label
     *              the label to add
     */
    void addLabel(@Nonnull String label);

    /**
     * Convenience method to access the authentication context for the current request.
     *
     * @return the {@code IAuthenticationContext} for the current request, or {@code null} if there is no authenticated
     *         user.
     */
    @Nonnull
    Optional<IAuthenticationContext> getAuthenticationContext();

    /**
     * Returns a highly unique (though not guaranteeably unique) request ID.
     * <p>
     * The request ID contains:
     * <ol>
     * <li>The minute of the current day</li>
     * <li>The number of requests, including the current one, which have been serviced by the application since it was
     * started</li>
     * <li>The number of requests which were being processed concurrently at the time the ID was generated</li>
     * </ol>
     * These fields are separated by an "x". The hour of the day portion resets each night, and the concurrency count
     * rises and falls with the load on the server. The request count is monotonically increasing until the {@code long}
     * for the counter wraps (which at 1,000 requests per second will take 252 million years).
     * <p>
     * It is worth noting that the uniqueness period required for request IDs is 1 day, which is the period at which log
     * files are rotated. The goal of this ID is not to be universally unique so much as it is to allow support to
     * easily trace the logging related to a single request, within the log files.
     *
     * @return the generated request ID
     */
    @Nonnull
    String getId();

    /**
     * @return {@code true} if the request is in progress. {@code false} if there is no current request or the request
     *         has finished
     */
    boolean isActive();
}
