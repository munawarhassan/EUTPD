package com.pmi.tpd.core.event.publisher;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * This dispatcher will dispatch event asynchronously if:
 * <ul>
 * <li>the event 'is' asynchronous, as resolved by the
 * {@link com.pmi.tpd.core.event.publisher.IAsynchronousEventResolver} and</li>
 * <li>the invoker {@link com.pmi.tpd.core.event.publisher.IListenerInvoker#supportAsynchronousEvents() supports
 * asynchronous events}</li>
 * </ul>
 * .
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class AsynchronousAbleEventDispatcher implements IEventDispatcher {

    /**
     * An executor that execute commands synchronously.
     */
    private static final Executor SYNCHRONOUS_EXECUTOR = new Executor() {

        @Override
        public void execute(final Runnable command) {
            command.run();
        }
    };

    /** An asynchronous executor. */
    private final ExecutorService asynchronousExecutor;

    /** a event resolver. */
    private final IAsynchronousEventResolver asynchronousEventResolver;

    /**
     * The only public constructor, uses an
     * {@link com.pmi.tpd.core.event.publisher.AnnotationAsynchronousEventResolver}.
     *
     * @param executorFactory
     *            the executor to use for asynchronous event listener invocations
     */
    public AsynchronousAbleEventDispatcher(@Nonnull final IEventExecutorFactory executorFactory) {
        this(executorFactory, new AnnotationAsynchronousEventResolver());
    }

    AsynchronousAbleEventDispatcher(@Nonnull final IEventExecutorFactory executorFactory,
            @Nonnull final IAsynchronousEventResolver asynchronousEventResolver) {
        this.asynchronousEventResolver = Assert.notNull(asynchronousEventResolver);
        this.asynchronousExecutor = Assert.notNull(Assert.notNull(executorFactory).getExecutor());
    }

    @Override
    public void shutdown() {
        asynchronousExecutor.shutdownNow();
    }

    /** {@inheritDoc} */
    @Override
    public void dispatch(@Nonnull final IListenerInvoker invoker, @Nonnull final Object event) {
        getExecutor(Assert.notNull(invoker), Assert.notNull(event)).execute(new Runnable() {

            @Override
            public void run() {
                invoker.invoke(event);
            }
        });
    }

    private Executor getExecutor(@Nonnull final IListenerInvoker invoker, @Nonnull final Object event) {
        return asynchronousEventResolver.isAsynchronousEvent(event) //
                && invoker.supportAsynchronousEvents() ? asynchronousExecutor : SYNCHRONOUS_EXECUTOR;
    }
}
