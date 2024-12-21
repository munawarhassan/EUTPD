package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * only need to extend the {@link UnhandledExceptionMapper} class.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(final WebApplicationException e) {
        final Response response = e.getResponse();
        return ResponseFactory.status(response.getStatusInfo().toEnum())
                .entity(new Errors(new ErrorMessage(null, e)))
                .type(RestUtils.APPLICATION_JSON_UTF8)
                .build();
    }
}