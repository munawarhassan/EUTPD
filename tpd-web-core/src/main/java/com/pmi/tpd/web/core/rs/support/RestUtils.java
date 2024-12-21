package com.pmi.tpd.web.core.rs.support;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Function;
import com.pmi.tpd.api.paging.PageRequest;
import com.pmi.tpd.api.paging.PageUtils;

/**
 * Helper functions and constants for REST.
 */
public abstract class RestUtils {

    /** */
    private static final Pattern CSID_PATTERN = Pattern.compile("[0-9a-f]{40}", Pattern.CASE_INSENSITIVE);

    /** */
    public static final String APPLICATION_JSON_UTF8 = MediaType.APPLICATION_JSON + ";charset=UTF-8";

    /** */
    public static final String DEFAULT_LIMIT = "25";

    /** */
    public static final String DEFAULT_START = "0";

    /** */
    public static final int DEFAULT_RECENT_REPOS_LIMIT = 15;

    /** */
    public static final String DEFAULT_ENTITY_VERSION = "-1";

    /** */
    public static final String CONTENT_TYPE = "content-type";

    /** */
    public static final String CHARSET_PREFIX = "charset=";

    /** */
    // Note: In order to use DEFAULT_LIMIT in annotations it must be a constant. That means it can't be the String value
    // of an integer. So while this is wasteful, this is the easiest way to make the two linked.
    private static final int LIMIT = Integer.parseInt(DEFAULT_LIMIT);

    /** */
    private static final int START = Integer.parseInt(DEFAULT_START);

    /**
     * Extracts paging-related query parameters from the provided {@code UriInfo} and returns a {@link PageRequest}
     * describing them. If paging parameters are not found, <i>default parameters are assumed</i>.
     * <p>
     * This method will never return {@code null}.
     *
     * @param request
     *            the request to parse query parameters for
     * @return a description of the page being requested
     * @see #DEFAULT_LIMIT
     * @see #DEFAULT_START
     */
    public static Pageable makePageable(final ContainerRequestContext request) {
        return makePageRequest(request, LIMIT);
    }

    /**
     * Extracts paging-related query parameters from the provided {@code UriInfo} and returns a {@link Pageable}
     * describing them. If paging parameters are not found, <i>default parameters are assumed</i>. the provided
     * {@code defaultLimit} will be used if no explicit limit is found as a query parameter.
     * <p>
     * This method will never return {@code null}.
     *
     * @param request
     *            the request to parse query parameters for
     * @param defaultLimit
     *            the limit to apply if no explicit limit has been specified
     * @return a description of the page being requested
     */
    public static Pageable makePageRequest(final ContainerRequestContext request, final int defaultLimit) {
        final MultivaluedMap<String, String> parameters = request.getUriInfo().getQueryParameters();

        final int page = parameters.containsKey("page") ? Integer.parseInt(parameters.getFirst("page")) : START;
        final int size = parameters.containsKey("size") ? Integer.parseInt(parameters.getFirst("size")) : LIMIT;
        final String sort = parameters.containsKey("sort") ? parameters.getFirst("sort") : null;
        final String filter = parameters.containsKey("filter") ? parameters.getFirst("filter") : null;
        final String query = parameters.containsKey("query") ? parameters.getFirst("query") : null;

        return PageUtils.newRequest(page, size, sort, filter, query);
    }

    public static boolean isImmutableObjectId(final String objectId) {
        return objectId != null && CSID_PATTERN.matcher(objectId).matches();
    }

    public static boolean isImmutableBetween(final String untilId, final String sinceId) {
        return isImmutableObjectId(untilId) && (sinceId == null || isImmutableObjectId(sinceId));
    }

    /**
     * Throws a {@link NotFoundException} if the value is null, otherwise return the value.
     */
    public static <T> T notFoundIfNull(final T t) throws NotFoundException {
        if (t == null) {
            throw new NotFoundException();
        }
        return t;
    }

    /**
     * If {@code from} is non-null, return the result of {@link Function#apply}. Otherwise return {@code null}.
     */
    public static <F, T> T applyOrNull(final F from, final Function<F, T> function) {
        if (from == null) {
            return null;
        }
        return function.apply(from);
    }

    /**
     * Gets the charset of a request.
     *
     * @param request
     *            request to introspect
     * @param defaultCharset
     *            default charset to use if none is found or if the charset is not supported
     * @return the request's charset or {@code defaultCharset} if none is found or if the charset is not supported
     */
    public static Charset getCharset(final ContainerRequestContext request, final Charset defaultCharset) {
        final String contentType = StringUtils.lowerCase(request.getHeaderString(CONTENT_TYPE));
        if (contentType != null) {
            final int p = contentType.indexOf(CHARSET_PREFIX);
            if (p != -1) {
                return getCharset(contentType.substring(p + CHARSET_PREFIX.length()), defaultCharset);
            }
        }

        return defaultCharset;
    }

    private static Charset getCharset(final String charset, final Charset defaultCharset) {
        try {
            return Charset.forName(charset);
        } catch (final IllegalCharsetNameException e) {
            return defaultCharset;
        } catch (final UnsupportedCharsetException e) {
            return defaultCharset;
        }
    }

    /**
     * Conditionally handle gets where the request may include <code>If-Last-Modified</code> headers.
     *
     * @param request
     *            current request
     * @param lastUpdate
     *            time when this entity was last updated
     * @param okFunction
     *            will be called with response builder if this is a fresh request; should most likely be calling
     *            {@link Response.ResponseBuilder#entity(Object)}.
     * @return response with the appropriate <code>Last-Modified</code> header
     */
    public static Response doConditionalGet(final Request request,
        final Date lastUpdate,
        final Function<Response.ResponseBuilder, Response.ResponseBuilder> okFunction) {
        final Response.ResponseBuilder rb = request.evaluatePreconditions(lastUpdate);
        if (rb != null) {
            return rb.build();
        } else {
            return okFunction.apply(Response.ok().lastModified(lastUpdate)).build();
        }
    }
}
