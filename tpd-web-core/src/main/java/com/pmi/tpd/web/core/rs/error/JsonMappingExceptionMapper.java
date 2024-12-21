package com.pmi.tpd.web.core.rs.error;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * Implementation of {@link ExceptionMapper} to send down a "400 Bad Request" in the event mapping JSON is received.
 */
class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    @Override
    public Response toResponse(final JsonMappingException exception) {
        return ResponseFactory.status(Response.Status.BAD_REQUEST)
                .entity(new Errors(new ErrorMessage(null, exception)))
                .type(RestUtils.APPLICATION_JSON_UTF8)
                .build();
    }
}
