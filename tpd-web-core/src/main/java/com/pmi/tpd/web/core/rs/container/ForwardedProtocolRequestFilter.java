package com.pmi.tpd.web.core.rs.container;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

@PreMatching
@Priority(500)
@Singleton
public class ForwardedProtocolRequestFilter implements ContainerRequestFilter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext ctx) throws IOException {

        final String scheme = getValue(ctx.getHeaders(), "x-forwarded-proto");
        final String port = getValue(ctx.getHeaders(), "x-forwarded-port");
        if (scheme == null && port == null) {
            return;
        }

        final UriBuilder baseBuilder = ctx.getUriInfo().getBaseUriBuilder();
        final UriBuilder requestBuilder = ctx.getUriInfo().getRequestUriBuilder();
        if (scheme != null) {
            baseBuilder.scheme(scheme);
            requestBuilder.scheme(scheme);
            baseBuilder.port(443);
            requestBuilder.port(443);
        }

        if (port != null) {
            final int nPort = Integer.parseInt(port);
            baseBuilder.port(nPort);
            requestBuilder.port(nPort);
        }

        ctx.setRequestUri(baseBuilder.build(), requestBuilder.build());
    }

    private String getValue(final MultivaluedMap<String, String> headers, final String header) {
        final List<String> values = headers.get(header);
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
}