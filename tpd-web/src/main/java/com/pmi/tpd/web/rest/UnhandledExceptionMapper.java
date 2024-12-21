package com.pmi.tpd.web.rest;

import java.util.Map;

import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.IRequestMetadata;
import com.pmi.tpd.web.core.rs.error.AccessDeniedExceptionMapper;
import com.pmi.tpd.web.core.rs.error.ErrorMessage;
import com.pmi.tpd.web.core.rs.error.Errors;
import com.pmi.tpd.web.core.rs.error.IUnhandledExceptionMapperHelper;
import com.pmi.tpd.web.core.rs.error.NoSuchEntityExceptionMapper;
import com.pmi.tpd.web.core.rs.error.ResourceException;
import com.pmi.tpd.web.core.rs.error.ResourceExceptionMapper;
import com.pmi.tpd.web.core.rs.error.UnrecognizedPropertyExceptionMapper;
import com.pmi.tpd.web.core.rs.error.ValidationExceptionMapper;
import com.pmi.tpd.web.core.rs.error.WebApplicationExceptionMapper;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * A aggregate class which when extended provides default exception mapping for the plugin.
 * <p>
 * All bundled plugins with REST resources should extend this class.
 */
public class UnhandledExceptionMapper implements ExceptionMapper<Exception> {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(UnhandledExceptionMapper.class);

    /** */
    private final Map<Class<?>, ExceptionMapper<?>> mappers;

    /** */
    private final IRequestManager requestManager;

    /**
     * @param helper
     */
    @Inject
    public UnhandledExceptionMapper(final IUnhandledExceptionMapperHelper helper) {
        final ImmutableMap.Builder<Class<?>, ExceptionMapper<?>> builder = createDefaultMappers();
        addMapper(builder,
            NoSuchEntityException.class,
            new NoSuchEntityExceptionMapper(helper.getAuthenticationContext(), helper.getI18nService()));
        addMapper(builder, AccessDeniedException.class, new AccessDeniedExceptionMapper(helper.getI18nService()));
        mappers = builder.build();

        requestManager = helper.getRequestManager();
    }

    private static ImmutableMap.Builder<Class<?>, ExceptionMapper<?>> createDefaultMappers() {
        final ImmutableMap.Builder<Class<?>, ExceptionMapper<?>> builder = ImmutableMap.builder();
        // Note this could alternatively be done via reflection but avoided for simplicity
        addMapper(builder, ServiceException.class, new ServiceExceptionMapper());
        addMapper(builder, WebApplicationException.class, new WebApplicationExceptionMapper());
        addMapper(builder, ResourceException.class, new ResourceExceptionMapper());
        addMapper(builder, UnrecognizedPropertyException.class, new UnrecognizedPropertyExceptionMapper());
        addMapper(builder, JsonParseException.class, new JsonParseExceptionMapper());
        addMapper(builder, JsonMappingException.class, new JsonMappingExceptionMapper());
        addMapper(builder, ValidationException.class, new ValidationExceptionMapper());
        return builder;
    }

    // A helper method to add compile check on exception mappers
    private static <T extends Exception> void addMapper(
        final ImmutableMap.Builder<Class<?>, ExceptionMapper<?>> builder,
        final Class<T> exceptionClass,
        final ExceptionMapper<T> exceptionMapper) {
        builder.put(exceptionClass, exceptionMapper);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Response toResponse(final Exception exception) {

        Class<?> exceptionClass = exception.getClass();
        // Walk the class hierarchy until you reach Exception (exclusively)
        while (!exceptionClass.equals(Exception.class)) {
            final ExceptionMapper mapper = mappers.get(exceptionClass);
            if (mapper != null) {
                // noinspection unchecked
                final Response response = mapper.toResponse(exception);
                // Allow the mappers to 'fall through' to the next mapper
                if (response != null) {
                    return response;
                }
            }
            exceptionClass = exceptionClass.getSuperclass();
        }

        // Using getRequestMetadata() instead of getRequestContext() because metadata also works on background
        // threads via the TransferableState mechanism (though _theoretically_ it shouldn't matter here)
        final IRequestMetadata metadata = requestManager.getRequestMetadata();
        if (metadata == null) {
            LOGGER.error("Unhandled exception while processing REST request, but no request is active", exception);
        } else {
            LOGGER.error("Unhandled exception while processing REST request: {}", metadata.getAction(), exception);
        }

        return ResponseFactory.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new Errors(new ErrorMessage(null,
                        "An error occurred while processing the request. Check the server logs for more information.",
                        exception)))
                .type(RestUtils.APPLICATION_JSON_UTF8)
                .build();
    }
}
