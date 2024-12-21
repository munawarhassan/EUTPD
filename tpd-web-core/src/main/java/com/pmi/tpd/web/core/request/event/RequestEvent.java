package com.pmi.tpd.web.core.request.event;

import java.util.EventObject;

import javax.servlet.http.HttpServletRequest;

import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * A base class for constructing events related to requests (i.e. HTTP and SSH requests).
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class RequestEvent extends EventObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final boolean http;

    /** */
    private final IRequestContext requestContext;

    /**
     * @param source
     * @param requestContext
     */
    protected RequestEvent(final Object source, final IRequestContext requestContext) {
        super(source);

        this.requestContext = requestContext;

        http = requestContext.getRawRequest() instanceof HttpServletRequest;
    }

    /**
     * Retrieves the {@code IRequestContext} for the event.
     *
     * @return the request
     */
    public IRequestContext getRequestContext() {
        return requestContext;
    }

    /**
     * Retrieves a flag indicating whether the event's request, available from the {@link #getRequestContext() request
     * context}, is an {@code HttpServletRequest}.
     *
     * @return {@code true} if the request is an {@code HttpServletRequest}; otherwise, {@code false}
     */
    public boolean isHttp() {
        return http;
    }
}
