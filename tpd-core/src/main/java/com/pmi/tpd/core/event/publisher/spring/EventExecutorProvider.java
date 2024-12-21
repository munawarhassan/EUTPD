package com.pmi.tpd.core.event.publisher.spring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import com.pmi.tpd.core.event.publisher.IEventExecutorFactory;

/**
 * <p>
 * EventExecutorProvider class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventExecutorProvider implements IEventExecutorFactory {

    /** concurrent executor service. */
    private ExecutorService executorService;

    /**
     * <p>
     * Constructor for EventExecutorProvider.
     * </p>
     */
    public EventExecutorProvider() {
        this(null);
    }

    /**
     * Create new instance.
     *
     * @param executorService
     *            a concurrent executor service.
     */
    public EventExecutorProvider(@Nullable final ExecutorService executorService) {
        this.executorService = executorService;
        init();
    }

    /**
     * <p>
     * init.
     * </p>
     */
    @PostConstruct
    public void init() {
        if (this.executorService == null) {
            this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public ExecutorService getExecutor() {
        return this.executorService;
    }

    @Override
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

}
