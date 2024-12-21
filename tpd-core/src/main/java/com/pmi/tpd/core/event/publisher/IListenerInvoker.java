package com.pmi.tpd.core.event.publisher;

import java.util.Set;

import javax.annotation.Nonnull;

/**
 * <p>
 * Implementation of this interface know how to invoke 'given types' of listeners so that they handle given events.
 * </p>
 * <p>
 * <strong>Note:</strong> Implementations <strong>MUST</strong> provide correct implementations of the
 * {@link Object#equals(Object)} and {@link Object#hashCode()} method.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IListenerInvoker {

    /**
     * The types of events supported by this invoker. I.e. {@link #invoke(Object)} can be safely called with any object
     * that is an instance of at least one of those types.
     *
     * @return the set of supported event types.
     */
    @Nonnull
    Set<Class<?>> getSupportedEventTypes();

    /**
     * Invokes the underlying listener for the given event.
     *
     * @param event
     *            the event to tell the listener about.
     * @throws java.lang.IllegalArgumentException
     *             if the event is not an instance of any of the types returned by {@link #getSupportedEventTypes()}
     */
    void invoke(@Nonnull final Object event);

    /**
     * Whether or not the underlying listener can handle asynchronous event.
     *
     * @return {@code true} if the underlying listener can handle asynchronous events, {@code false} otherwise
     */
    boolean supportAsynchronousEvents();
}
