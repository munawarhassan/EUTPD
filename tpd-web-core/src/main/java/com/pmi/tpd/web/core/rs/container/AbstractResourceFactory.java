package com.pmi.tpd.web.core.rs.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;

import org.glassfish.hk2.api.Factory;

/**
 * Simplifies exception handling for resource injectable implementations.
 *
 * @param <T>
 *            the type of resource.
 * @since 2.0
 */
public abstract class AbstractResourceFactory<T> implements Factory<T> {

    /** */
    static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    /** */
    private final Type type;

    /** */
    private final ContainerRequestContext containerRequest;

    /**
     * Default constructor.
     *
     * @param containerRequest
     *            the jax-rs container request.
     */
    @Inject
    public AbstractResourceFactory(final ContainerRequestContext containerRequest) {
        // Capture the type here so we can easily create a map in ResourceContextInjectableProvider
        type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.containerRequest = containerRequest;
    }

    @Override
    public T provide() {
        return doGetValue(containerRequest);
    }

    @Override
    public void dispose(final T instance) {

    }

    /**
     * @return Returns the type managed.
     */
    public Type getType() {
        return type;
    }

    /**
     * Implemented in derived classes to produce the value to be injected. Implementations may throw exceptions
     * normally; {@link #getValue(HttpContext)} will see to their handling.
     *
     * @param containerRequest
     *            the jax-rs container request.
     * @return the value to be injected, which may be {@code null} to not inject a value
     */
    protected abstract T doGetValue(ContainerRequestContext containerRequest);

}
