package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * only need to extend the {@link UnhandledExceptionMapper} class.
 */
public class UnrecognizedPropertyExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

    @Override
    public Response toResponse(final UnrecognizedPropertyException exception) {
        final ErrorMessage error = new ErrorMessage(exception.getPropertyName(), exception.getLocalizedMessage());
        return ResponseFactory.status(Response.Status.BAD_REQUEST)
                .entity(new Errors(error))
                .type(RestUtils.APPLICATION_JSON_UTF8)
                .build();
    }
}
