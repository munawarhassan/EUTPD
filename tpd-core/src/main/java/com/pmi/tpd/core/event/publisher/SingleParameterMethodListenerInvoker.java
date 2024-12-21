package com.pmi.tpd.core.event.publisher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.Sets;
import com.pmi.tpd.api.util.Assert;

/**
 * A listener invoker that knows how to call a given single parameter method on a given object.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
final class SingleParameterMethodListenerInvoker implements IListenerInvoker {

    /** */
    private final Method method;

    /** */
    private final Object listener;

    /**
     * <p>
     * Constructor for SingleParameterMethodListenerInvoker.
     * </p>
     *
     * @param listener
     *            a {@link java.lang.Object} object.
     * @param method
     *            a {@link java.lang.reflect.Method} object.
     */
    SingleParameterMethodListenerInvoker(@Nonnull final Object listener, @Nonnull final Method method) {
        this.listener = Assert.notNull(listener);
        this.method = Assert.notNull(method);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Set<Class<?>> getSupportedEventTypes() {
        return Sets.newHashSet(method.getParameterTypes());
    }

    /** {@inheritDoc} */
    @Override
    public void invoke(@Nonnull final Object event) {
        try {
            method.invoke(listener, event);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            if (e.getCause() == null) {
                throw new RuntimeException(e);
            } else if (e.getCause().getMessage() == null) {
                throw new RuntimeException(e.getCause());
            } else {
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportAsynchronousEvents() {
        return true;
    }
}
