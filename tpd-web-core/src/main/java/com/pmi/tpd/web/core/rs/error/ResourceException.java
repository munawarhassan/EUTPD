package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.core.Response;

import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public abstract class ResourceException extends RuntimeException {

    private static final long serialVersionUID = 8345013342748820431L;

    /** */
    protected final Response.ResponseBuilder builder;

    /**
     * @param builder
     */
    protected ResourceException(final Response.ResponseBuilder builder) {
        this.builder = builder.type(RestUtils.APPLICATION_JSON_UTF8);
    }

    /**
     * @return
     */
    public Response getResponse() {
        return builder.build();
    }
}
