package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * only need to extend the {@link UnhandledExceptionMapper} class.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class ResourceExceptionMapper implements ExceptionMapper<ResourceException> {

    @Override
    public Response toResponse(final ResourceException exception) {
        return exception.getResponse();
    }
}