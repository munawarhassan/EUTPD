package com.pmi.tpd.core.event.publisher;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * A thread factory that will name the threads <strong>App::[thread_name]</strong>.
 * </p>
 * <p>
 * If you need your own {@link java.util.concurrent.ThreadFactory ThreadFactory} we recommend delegating the Thread
 * creation to this implementation.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class EventThreadFactory implements ThreadFactory {

    /** */
    private final ThreadFactory delegateThreadFactory;

    /**
     * Create new instance using a default thread factory.
     *
     * @see Executors#defaultThreadFactory()
     */
    public EventThreadFactory() {
        this(Executors.defaultThreadFactory());
    }

    /**
     * Create new instance with specific thread factory.
     *
     * @param delegateThreadFactory
     *            a thread factory;
     * @throws java.lang.IllegalArgumentException
     *             if the {@code delegateThreadFactory} is {@code null}
     */
    public EventThreadFactory(@Nonnull final ThreadFactory delegateThreadFactory) {
        this.delegateThreadFactory = Assert.notNull(delegateThreadFactory);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Thread newThread(@Nonnull final Runnable r) {
        final Thread thread = delegateThreadFactory.newThread(r);
        thread.setName("App::" + thread.getName());
        return thread;
    }
}
