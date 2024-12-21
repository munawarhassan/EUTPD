package com.pmi.tpd.web.core.request;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Sets up and tears down all logic for processing requests, such as logging, access logging, profiling and publishing
 * {@link com.pmi.tpd.web.request.event.RequestStartedEvent} and
 * {@link com.pmi.tpd.web.request.event.RequestEndedEvent}.
 *
 * @since 1.3
 */
public interface IRequestManager {

    /**
     * Sets up and tears down all logic for processing requests.
     *
     * @param callback
     *                            called after the {@link com.pmi.tpd.web.request.event.RequestStartedEvent} has been
     *                            published.
     * @param requestInfoProvider
     *                            provides request details such as sessionId, remoteAddress that are used to set up
     *                            logging.
     * @return the return value of the callback
     * @throws E
     *           when {@link IRequestCallback#withRequest(IRequestContext)} throws an exception.
     */
    @Nullable
    <T, E extends Exception> T doAsRequest(@Nonnull IRequestCallback<T, E> callback,
        @Nonnull IRequestInfoProvider requestInfoProvider) throws E;

    /**
     * @return the context of the current request. Returns {@code null} if there is no current request, for instance
     *         when running a task in a background thread.
     */
    @Nullable
    IRequestContext getRequestContext();

    /**
     * @return the metadata of the current request. Returns {@code null} if there is no current request.
     */
    @Nullable
    IRequestMetadata getRequestMetadata();
}
