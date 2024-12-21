package com.pmi.tpd.web.rest.util;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.google.common.base.Strings;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;
import com.pmi.tpd.web.core.rs.support.CachePolicies;
import com.pmi.tpd.web.core.rs.support.ResponseFactory;

public abstract class AvatarSupport extends ResourceSupport {

    protected AvatarSupport(final I18nService i18nService) {
        super(i18nService);
    }

    /**
     * Streams the provided {@link ICacheableAvatarSupplier avatar}, automatically handling "If-Modified-Since",
     * "Cache-Control" and "Last-Modified" headers to encourage browsers to cache avatars.
     *
     * @param supplier
     *            a supplier providing access to the avatar data to stream
     * @param request
     *            the HTTP request to retrieve the avatar
     * @param response
     *            the HTTP response to which the avatar should be streamed
     * @throws IOException
     *             if the avatar cannot be read from the provided supplier, or written to the provided response
     */
    protected Response streamAvatar(final ICacheableAvatarSupplier supplier, final ContainerRequestContext request)
            throws IOException {
        final ResponseBuilder response = ResponseFactory.ok();
        if (!Strings.isNullOrEmpty(request.getUriInfo().getPathParameters().getFirst("v"))) {
            // if we have a cache-busting URL, the client can cache for a long time
            CachePolicies.cacheFor(response, 365, TimeUnit.DAYS);
        } else {
            // support Last-Modified/If-Modified-Since headers, when the URL contains no version parameter
            final long timestamp = supplier.getTimestamp();
            if (timestamp != ICacheableAvatarSupplier.TIMESTAMP_UNKNOWN) {
                final Date date = new Date(timestamp);
                final ResponseBuilder resp = request.getRequest().evaluatePreconditions(date);
                if (resp != null) {
                    return resp.build();
                }
                response.lastModified(date);
            }
        }
        response.type(MediaType.valueOf(supplier.getContentType()));
        response.entity(supplier.open());

        return response.build();
    }
}