package com.pmi.tpd.web.core.rs.endpoint;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

/**
 * Adapter class to expose {@link Endpoint}s as {@link RestEndpoint}s.
 */
public class EndpointRestAdapter implements RestEndpoint {

    private final Endpoint<?> delegate;

    /**
     * Create a new {@link EndpointRestAdapter}.
     *
     * @param delegate
     *            the underlying {@link Endpoint} to adapt.
     */
    public EndpointRestAdapter(final Endpoint<?> delegate) {
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
    }

    @Override
    public Object invoke(final Request request, final SecurityContext context) {
        if (!this.delegate.isEnabled()) {
            // Shouldn't happen
            return Response.status(Status.NOT_FOUND)
                    .entity(ImmutableMap.of("message", "This endpoint is disabled"))
                    .build();
        }
        return this.delegate.invoke();
    }

    public Endpoint<?> getDelegate() {
        return this.delegate;
    }

    @Override
    public String getId() {
        return this.delegate.getId();
    }

    @Override
    public boolean isSensitive() {
        return this.delegate.isSensitive();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends Endpoint> getEndpointType() {
        return this.delegate.getClass();
    }

}
