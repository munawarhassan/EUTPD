package com.pmi.tpd.core.event.advisor;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.IEventContainer;

/**
 * Allows applications using event application to provide custom {@link IEventContainer} implementations for use as part
 * of their configuration.
 * <p/>
 * For example, multi-tenant applications need a way to group events in the container per-tenant, allowing tenants to
 * see only those application and request events which relate to their view of the system. In such a scenario, the
 * application may register a custom {@code IContainerFactory} which returns a tenant-aware container. This keeps any
 * dependency on or knowledge of multi-tenancy libraries.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IContainerFactory {

    /**
     * Creates a {@link IEventContainer} for use storing events.
     * <p/>
     * This method will be called <i>exactly once</i> during event initialisation, and the returned event container will
     * be used to service all requests on all threads. As a result, the returned implementation is required to be
     * thread-safe. For multi-tenant systems, handling the differentiation between events for each tenant must happen
     * below the interface, on a single instance of the container.
     *
     * @return an event container
     */
    @Nonnull
    IEventContainer create();
}
