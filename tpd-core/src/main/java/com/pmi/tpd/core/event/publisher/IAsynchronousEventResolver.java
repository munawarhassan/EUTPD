package com.pmi.tpd.core.event.publisher;

import javax.annotation.Nonnull;

/**
 * An interface to resolve whether an event can be handled asynchronously or not.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
interface IAsynchronousEventResolver {

    /**
     * Tells whether the event can be handled asynchronously or not.
     *
     * @param event
     *            the event to check
     * @return {@code true} if the event can be handled asynchronously, {@code false} otherwise.
     * @throws java.lang.IllegalArgumentException
     *             if the {@code event} is {@code null}
     */
    boolean isAsynchronousEvent(@Nonnull final Object event);
}
