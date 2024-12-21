package com.pmi.tpd.web.rest;

import static com.google.common.base.Preconditions.checkArgument;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.crypto.KeystoreException;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.core.elasticsearch.IndexingException;
import com.pmi.tpd.core.exception.RequestCanceledException;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.exception.UsernameAlreadyExistsException;
import com.pmi.tpd.keystore.KeystoreWrongPasswordException;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.ForbiddenException;
import com.pmi.tpd.web.core.rs.annotation.JsonSurrogate;
import com.pmi.tpd.web.core.rs.error.ErrorMessage;
import com.pmi.tpd.web.core.rs.error.Errors;
import com.pmi.tpd.web.core.rs.renderer.AggregateBuilder;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;
import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * Maps exceptions from the {@link ServiceException} class hierarchy to JSON REST responses, choosing a status code for
 * the response based on the exception type.
 * <p>
 * only need to extend the {@link UnhandledExceptionMapper} class.
 * </p>
 */
class ServiceExceptionMapper implements ExceptionMapper<ServiceException> {

    /** */
    public static final Map<Class<? extends ServiceException>, Response.Status> STATUS_MAP;

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExceptionMapper.class);

    static {
        STATUS_MAP = ImmutableMap.<Class<? extends ServiceException>, Response.Status> builder()
                .put(ArgumentValidationException.class, BAD_REQUEST)
                .put(IndexingException.class, BAD_REQUEST)
                // .put(AuthenticationRequiredException.class, OK)
                .put(AuthorisationException.class, UNAUTHORIZED)
                .put(ForbiddenException.class, FORBIDDEN)
                // .put(IllegalEntityStateException.class, CONFLICT)
                .put(IntegrityException.class, CONFLICT)
                .put(NoSuchEntityException.class, NOT_FOUND)
                .put(RequestCanceledException.class, BAD_REQUEST)
                .put(UsernameAlreadyExistsException.class, CONFLICT)
                .put(UserEmailAlreadyExistsException.class, CONFLICT)
                .put(NoMailHostConfigurationException.class, Status.SERVICE_UNAVAILABLE)
                .put(KeystoreException.class, BAD_REQUEST)
                .put(KeystoreWrongPasswordException.class, BAD_REQUEST)
                .build();
    }

    /** */
    private final AggregateBuilder exceptionMessageBuilder;

    public ServiceExceptionMapper() {
        exceptionMessageBuilder = new AggregateBuilder();

        registerSurrogate(ErrorMessage.class);

    }

    @Override
    public Response toResponse(final ServiceException e) {

        // We can guarantee the result of buildFor(...) is both non-null and an instance of ErrorMessage
        // because ServiceException is mapped by ErrorMessage as a fallback and the other surrogates are
        // sub-classes of ErrorMessage, as enforced by registerSurrogate above
        final Errors errors = new Errors((ErrorMessage) exceptionMessageBuilder.buildFor(e));
        final Response response = createResponseBuilder(e).entity(errors).type(RestUtils.APPLICATION_JSON_UTF8).build();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Mapping ServiceException to REST response " + response.getStatus(), e);
        }
        return response;
    }

    private void registerSurrogate(final Class<?> surrogateClass) {
        checkArgument(surrogateClass.isAnnotationPresent(JsonSurrogate.class),
            surrogateClass.getName() + " is not annotated as a surrogate");
        checkArgument(ErrorMessage.class.isAssignableFrom(surrogateClass),
            surrogateClass.getName() + " must derive from ErrorMessage");
        checkArgument(
            ServiceException.class.isAssignableFrom(surrogateClass.getAnnotation(JsonSurrogate.class).value()),
            surrogateClass.getAnnotation(JsonSurrogate.class).value().getName() + " must derive from ServiceException");

        exceptionMessageBuilder.registerSurrogate(surrogateClass);
    }

    /**
     * Creates a {@code ResponseBuilder} from the provided {@code ServiceException}.
     * <p>
     * The status code for the returned builder is looked up in {@link #STATUS_MAP}. Lookups begin with the exact class
     * of the provided exception and then work their way up its inheritance hierarchy to {@code ServiceException}. If a
     * match is found at any level, it is used. If no match is found from the exception hierarchy, a default server
     * error status (500) is used.
     *
     * @param e
     *          the exception to create a builder for
     * @return a builder with a status code appropriate for the provided exception
     */
    private Response.ResponseBuilder createResponseBuilder(final ServiceException e) {
        // Walk up the exception's class hierarchy until it we traverse past ServiceException (at which point the class
        // could no longer possibly be in the map). The most specific mapping is preferred, but a more generic match at
        // any point in the exception's hierarchy will be accepted
        Class<?> clazz = e.getClass();
        while (ServiceException.class.isAssignableFrom(clazz)) {
            final Response.Status status = STATUS_MAP.get(clazz);
            if (status != null) {
                return ResponseFactory.status(status);
            }

            clazz = clazz.getSuperclass();
        }

        // If we make it here, there is no mapping for the provided exception, or any of its ServiceException-derived
        // superclasses, so return a default server error status.
        return ResponseFactory.serverError();
    }
}
