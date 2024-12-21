package com.pmi.tpd.web.core.rs.support;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.pmi.tpd.web.core.rs.error.Errors;

public class ResponseFactory {

    private ResponseFactory() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    private static Response.ResponseBuilder applyDefaults(final Response.ResponseBuilder builder) {
        return builder.cacheControl(CachePolicies.noCache())
                // Regardless of the cache policy specify the correct VARY headers
                .header(HttpHeaders.VARY, "X-USERNAME") // Chrome Dev Channel doesn't properly support Vary Cookie so
                                                        // this is here for good measure
                .header(HttpHeaders.VARY, "X-USERID") // Now that user renaming is possible we also need to vary on
                                                      // user id
                .header(HttpHeaders.VARY, "Cookie"); // This is here in case plugins don't send the X-AUSERNAME header
                                                     // in the request.
    }

    /**
     * Create a response builder for a bad request response with {@code error} message.
     *
     * @param error
     *              error message
     * @return response builder for bad request
     * @since 2.8
     */
    public static Response.ResponseBuilder badRequest(final String error) {
        return badRequest(null, error);
    }

    /**
     * Create a response builder for a bad request response with error {@code context} and {@code error} message.
     *
     * @param context
     *                error context
     * @param error
     *                error message
     * @return response builder for bad request
     * @since 2.8
     */
    public static Response.ResponseBuilder badRequest(final String context, final String error) {
        return error(Response.Status.BAD_REQUEST, context, error);
    }

    /**
     * @return Returns response with an OK status.
     */
    public static Response.ResponseBuilder ok() {
        return applyDefaults(Response.ok());
    }

    /**
     * @param entity
     *               entity to add
     * @return Returns response with an Ok status and that contains a representation.
     */
    public static Response.ResponseBuilder ok(final Object entity) {
        return applyDefaults(Response.ok(entity));
    }

    /**
     * @param entity
     *                     entity to add
     * @param cacheControl
     * @return
     */
    public static Response.ResponseBuilder ok(final Object entity, final CacheControl cacheControl) {
        return applyDefaults(Response.ok(entity)).cacheControl(cacheControl);
    }

    /**
     * @param location
     * @return
     */
    public static Response.ResponseBuilder created(@Nonnull final URI location) {
        return applyDefaults(Response.created(location));
    }

    /**
     * @return
     */
    public static Response.ResponseBuilder noContent() {
        return applyDefaults(Response.noContent());
    }

    /**
     * @return
     */
    public static Response.ResponseBuilder notFound() {
        return applyDefaults(status(Response.Status.NOT_FOUND));
    }

    /**
     * @param status
     * @param context
     * @param error
     * @return
     */
    public static Response.ResponseBuilder error(@Nonnull final Response.Status status,
        final String context,
        final String error) {
        return errors(status, new Errors(context, error));
    }

    /**
     * @param status
     * @param errors
     * @return
     */
    public static Response.ResponseBuilder errors(@Nonnull final Response.Status status, final Errors errors) {
        return status(status).entity(errors);
    }

    /**
     * @param status
     * @return
     */
    public static Response.ResponseBuilder status(@Nonnull final Response.Status status) {
        return applyDefaults(Response.status(status));
    }

    /**
     * @return
     */
    public static Response.ResponseBuilder serverError() {
        return applyDefaults(Response.serverError());
    }

    /**
     * @return
     */
    public static ResponseBuilder accepted() {
        return status(Response.Status.ACCEPTED);
    }

}