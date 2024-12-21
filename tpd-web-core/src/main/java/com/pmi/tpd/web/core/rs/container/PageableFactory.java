package com.pmi.tpd.web.core.rs.container;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.data.domain.Pageable;

import com.pmi.tpd.web.core.rs.support.RestUtils;

/**
 * Injects the context {@link PageRequest} into a JAX-RS resource method that has start and limit query parameters.
 *
 * @since 2.0
 */
public class PageableFactory extends AbstractResourceFactory<Pageable> {

    /**
     * @param containerRequest
     *            the jax-rs container request.
     */
    @Inject
    public PageableFactory(final ContainerRequestContext containerRequest) {
        super(containerRequest);
    }

    @Override
    public Pageable doGetValue(final ContainerRequestContext containerRequest) {
        return RestUtils.makePageable(containerRequest);
    }
}