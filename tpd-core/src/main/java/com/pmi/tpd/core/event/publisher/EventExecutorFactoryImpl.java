package com.pmi.tpd.core.event.publisher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.config.IEventThreadPoolConfiguration;

/**
 * Factory based {@link com.pmi.tpd.core.event.publisher.IEventExecutorFactory} allowing create a
 * {@link java.util.concurrent.Executor} according to configuration.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventExecutorFactoryImpl implements IEventExecutorFactory {

    /** executor. */
    private final ThreadPoolExecutor executor;

    /**
     * Create new instance.
     *
     * @param configuration
     *            thread pool configuration used
     */
    public EventExecutorFactoryImpl(final IEventThreadPoolConfiguration configuration) {
        Assert.notNull(configuration);
        this.executor = new ThreadPoolExecutor(configuration.getCorePoolSize(), configuration.getMaximumPoolSize(),
                configuration.getKeepAliveTime(), configuration.getTimeUnit(), new SynchronousQueue<Runnable>(),
                new EventThreadFactory());
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ExecutorService getExecutor() {
        return executor;
    }

    @PreDestroy
    @Override
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
