package com.pmi.tpd.web.core.rs.error;

import javax.validation.ValidationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * only need to extend the {@link UnhandledExceptionMapper} class.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(final ValidationException exception) {
        return ResponseFactory.status(Response.Status.BAD_REQUEST)
                .entity(new Errors(new ErrorMessage(null, exception)))
                .type(RestUtils.APPLICATION_JSON_UTF8)
                .build();
    }
}