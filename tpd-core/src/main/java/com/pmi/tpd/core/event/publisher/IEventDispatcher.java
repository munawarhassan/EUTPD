package com.pmi.tpd.core.event.publisher;

import javax.annotation.Nonnull;

/**
 * Dispatches an event to its listener (through the invoker). Implementations can choose for example whether to dispatch
 * events asynchronously.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IEventDispatcher {

    /**
     * Dispatches the event using the invoker.
     *
     * @param invoker
     *            the invoker to use to dispatch the event
     * @param event
     *            the event to dispatch
     * @throws java.lang.IllegalArgumentException
     *             if either the {@code invoker} or the {@code event} is {@code null}
     */
    void dispatch(@Nonnull final IListenerInvoker invoker, @Nonnull Object event);

    /**
     *
     */
    void shutdown();
}
