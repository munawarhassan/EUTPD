package com.pmi.tpd.web.core.rs.endpoint;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;

/**
 * A strategy for the MVC layer on top of an {@link Endpoint}.
 */
public interface RestEndpoint {

    /**
     * Return the id of the endpoint.
     *
     * @return the endpoint id
     */
    String getId();

    /**
     * Return if the endpoint exposes sensitive information.
     *
     * @return if the endpoint is sensitive
     */
    boolean isSensitive();

    /**
     * Return the type of {@link Endpoint} exposed, or {@code null} if this {@link RestEndpoint} exposes information
     * that cannot be represented as a traditional {@link Endpoint}.
     *
     * @return the endpoint type
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Endpoint> getEndpointType();

    /**
     * @return
     */
    public Object invoke(final Request request, final SecurityContext context);

}
