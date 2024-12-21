package com.pmi.tpd.web.core.request.event;

import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Dispatched to indicate a request has started.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class RequestStartedEvent extends RequestEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code RequestEndedEvent}, providing the event's source and the IRequestContext for the request
     * that was started.
     *
     * @param source
     *            the event source
     * @param requestContext
     *            the IRequestContext for the started request
     */
    public RequestStartedEvent(final Object source, final IRequestContext requestContext) {
        super(source, requestContext);
    }
}
