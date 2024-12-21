package com.pmi.tpd.web.core.request;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.CRC32;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.pmi.tpd.api.LoggingConstants;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Timer;
import com.pmi.tpd.api.util.TimerUtils;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.cluster.concurrent.IStatefulService;
import com.pmi.tpd.cluster.concurrent.ITransferableState;
import com.pmi.tpd.cluster.event.ClusterMembershipEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.event.RequestEndedEvent;
import com.pmi.tpd.web.core.request.event.RequestStartedEvent;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@Slf4j
public class DefaultRequestManager implements IRequestManager, IStatefulService {

    /** */
    private static final Logger ACCESS_LOG = LoggerFactory.getLogger(LoggingConstants.LOGGER_ACCESS);

    /** */
    private final IAuthenticationContext authenticationContext;

    // These two counters are only used while being paired with the local node id of the server
    // so don't need to be a cluster wide counter.

    /** */
    private final AtomicLong concurrentCounter;

    /** */
    private final AtomicLong requestCounter;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final ThreadLocal<DefaultRequestContext> currentRequestContext;

    /** */
    private final ThreadLocal<IRequestMetadata> requestMetadata;

    /** */
    private volatile boolean clustered;

    /** */
    private IClusterService clusterService;

    /** */
    private String nodeId;

    @Inject
    public DefaultRequestManager(final IAuthenticationContext authenticationContext,
            final IEventPublisher eventPublisher, final IClusterService clusterService) {
        this.authenticationContext = authenticationContext;
        this.eventPublisher = eventPublisher;
        this.setClusterService(clusterService);

        this.concurrentCounter = new AtomicLong(0);
        this.requestCounter = new AtomicLong(0);
        this.currentRequestContext = new ThreadLocal<>();
        this.requestMetadata = new ThreadLocal<>();
    }

    @PreDestroy
    public void destroy() {
        try {
            this.currentRequestContext.remove();
        } catch (final Throwable ex) {
            LOGGER.warn("Try remove threadlocal failed", ex);
        }
        try {
            this.requestMetadata.remove();
        } catch (final Throwable ex) {
            LOGGER.warn("Try remove threadlocal failed", ex);
        }
    }

    @Override
    @Nullable
    public <T, E extends Exception> T doAsRequest(@Nonnull final IRequestCallback<T, E> callback,
        @Nonnull final IRequestInfoProvider requestInfoProvider) throws E {
        if (currentRequestContext.get() == null) {
            final long startTime = System.nanoTime();
            final DefaultRequestContext requestContext = new DefaultRequestContext(authenticationContext,
                    requestInfoProvider, generateRequestId());
            try {
                concurrentCounter.incrementAndGet();
                currentRequestContext.set(requestContext);
                logStartRequest(requestContext);
                eventPublisher.publish(new RequestStartedEvent(this, requestContext));

                if (TimerUtils.isActive()) {
                    final String action = requestContext.getAction();
                    try (Timer ignored = TimerUtils.start(action)) {
                        return callback.withRequest(requestContext);
                    }
                }
                return callback.withRequest(requestContext);
            } finally {
                concurrentCounter.decrementAndGet();
                currentRequestContext.remove();
                try {
                    eventPublisher.publish(new RequestEndedEvent(this, requestContext));
                } finally {
                    try {
                        logEndRequest(requestContext, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
                    } finally {
                        MDC.clear();
                    }
                    requestContext.runCleanupCallbacks();
                }
            }
        } else {
            // currentRequest was already set, simply pass through
            return callback.withRequest(currentRequestContext.get());
        }
    }

    @Override
    @Nullable
    public IRequestContext getRequestContext() {
        return currentRequestContext.get();
    }

    @Nullable
    @Override
    public IRequestMetadata getRequestMetadata() {
        final IRequestMetadata md = requestMetadata.get();
        // RequestMetadata will only be set on threads which do not have a IRequestContext, so delegate if it is not set
        if (md == null) {
            return getRequestContext();
        }
        return md;
    }

    @EventListener
    public void onClusterMembershipChanged(final ClusterMembershipEvent event) {
        clustered = event.getCurrentNodes().size() > 1;
    }

    public void setClusterService(final IClusterService clusterService) {
        this.clusterService = clusterService;
        this.nodeId = calcLocalNodeId();
        this.clustered = clusterService.isClustered();
    }

    /**
     * Produces a highly unique (though not guaranteeably unique) request ID.
     * <p>
     * The request ID contains:
     * <ol>
     * <li>The cluster-sensitive ID of this instance which is composed of:
     * <ol>
     * <li>A reasonably (though not guaranteeably) unique identifier of this instance valid until it terminates.
     * Followed by:</li>
     * <li>'*' if currently clustered or '@' if not.</li>
     * </ol>
     * Example IDs: "*1k2z3ni" and "@9k7z3nn".</li>
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
    protected String generateRequestId() {
        final long count = requestCounter.incrementAndGet();

        return (clustered ? "*" : "@") + nodeId + "x" + new DateTime().getMinuteOfDay() + "x" + count + "x"
                + concurrentCounter.get();
    }

    private String calcLocalNodeId() {
        final CRC32 crc = new CRC32();
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        crc.update(sha1.digest(clusterService.getInformation().getLocalNode().getId().toString().getBytes()));
        return Long.toString(crc.getValue(), Character.MAX_RADIX).toUpperCase(Locale.US);
    }

    protected void setupMDC(final DefaultRequestContext requestContext) {
        // Bind properties to the MDC, allowing them to be included in logging
        MDC.put(LoggingConstants.MDC_REQUEST_ID, requestContext.getId());
        MDC.put(LoggingConstants.MDC_REMOTE_ADDRESS, requestContext.getRemoteAddress());
        MDC.put(LoggingConstants.MDC_REQUEST_DETAILS, requestContext.getDetails());
        // Best effort to set early, in case we already have session
        MDC.put(LoggingConstants.MDC_SESSION_ID, requestContext.getSessionId());
        MDC.put(LoggingConstants.MDC_PROTOCOL, requestContext.getProtocol());
        MDC.put(LoggingConstants.MDC_REQUEST_ACTION, requestContext.getAction());

        // best effort to set the current user if we already have it
        authenticationContext.getCurrentUser()
                .ifPresent(user -> MDC.put(LoggingConstants.MDC_USERNAME, user.getUsername()));

    }

    protected void logStartRequest(final DefaultRequestContext requestInfo) {
        checkStaleMDCUsername();
        setupMDC(requestInfo);

        MDC.put(LoggingConstants.MDC_ACCESSLOG_IN_OUT, "i");
        // the actual log message is not even included in the access log format, so log an empty string
        ACCESS_LOG.info("");
    }

    protected void logEndRequest(final DefaultRequestContext requestInfo, final long requestTime) {
        setupMDC(requestInfo);
        MDC.put(LoggingConstants.MDC_REQUEST_TIME, Long.toString(requestTime));
        MDC.put(LoggingConstants.MDC_ACCESSLOG_IN_OUT, "o");
        // the actual log message is not even included in the access log format, so log an empty string
        ACCESS_LOG.info("");
    }

    @Nonnull
    @Override
    public ITransferableState getState() {
        return new RequestMetadataState(getRequestMetadata());
    }

    /**
     * Checks that the username in the MDC is not set to a stale value (from a previous request). This should not be
     * necessary (the MDC should have been scrubbed of data at the end of the last request) but I have seen instances
     * where the MDC has contained a stale username and so if we don't clear it an anonymous or access-key authenticated
     * request gets associated with a user
     */
    private void checkStaleMDCUsername() {
        authenticationContext.getCurrentUser().ifPresent(user -> {
            final String username = MDC.get(LoggingConstants.MDC_USERNAME);
            if (username != null) {
                LOGGER.debug("No current user for the current request but MDC contains a request username: {}",
                    username);
                MDC.remove(LoggingConstants.MDC_USERNAME);
            }
        });
    }

    /**
     * Captures {@link RequestMetadata}, if the creating thread contained a request, and applies that request
     * information to the new thread.
     */
    private final class RequestMetadataState implements ITransferableState {

        private final IRequestMetadata metadata;

        /**
         * Constructs a new {@code RequestMetadataState}, capturing {@link RequestMetadata} if it is not {@code null}.
         *
         * @param md
         *           the request metadata held by the constructing thread, or {@code null} if the constructing thread
         *           did not have a copy of the request metadata
         */
        public RequestMetadataState(final IRequestMetadata md) {
            if (md != null) {
                this.metadata = new DefaultRequestMetadata(md);
            } else {
                this.metadata = null;
            }
        }

        /**
         * Binds the {@link RequestMetadataState} from the constructing thread, if set.
         */
        @Override
        public void apply() {
            requestMetadata.set(metadata);
        }

        /**
         * Remove the thread local from this thread.
         */
        @Override
        public void remove() {
            requestMetadata.remove();
        }
    }
}
