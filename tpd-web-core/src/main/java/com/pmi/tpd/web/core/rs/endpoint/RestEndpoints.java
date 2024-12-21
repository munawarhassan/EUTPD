package com.pmi.tpd.web.core.rs.endpoint;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.pmi.tpd.api.util.Assert;

/**
 * A registry for all {@link RestEndpoint} beans, and a factory for a set of generic ones wrapping existing
 * {@link Endpoint} instances that are not already exposed as Rest endpoints.
 */
public class RestEndpoints {

    private final Set<RestEndpoint> endpoints;

    /**
     *
     */
    public RestEndpoints() {
        this.endpoints = Sets.newHashSet();
    }

    /**
     *
     */
    public RestEndpoints(@Nonnull final Set<Endpoint<?>> endpoints) {
        this.endpoints = Sets.newHashSet();
        for (final Endpoint<?> endpoint : endpoints) {
            register(endpoint);
        }
    }

    public void register(@Nonnull final Endpoint<?> endpoint) {
        Assert.notNull(endpoint);
        if (isGenericEndpoint(endpoint.getClass())) {
            this.endpoints.add(new EndpointRestAdapter(endpoint));
        } else {
            this.endpoints.add((RestEndpoint) endpoint);
        }
    }

    public RestEndpoint findEndpoint(final String id) {
        for (final RestEndpoint restEndpoint : endpoints) {
            if (id.equals(restEndpoint.getId())) {
                return restEndpoint;
            }
        }
        return null;
    }

    public Set<? extends RestEndpoint> getEndpoints() {
        return this.endpoints;
    }

    private boolean isGenericEndpoint(final Class<?> type) {
        return !RestEndpoint.class.isAssignableFrom(type);
    }

}
