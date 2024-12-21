package com.pmi.tpd.core.event.publisher;

import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.lifecycle.IShutdown;

/**
 * <p>
 * A factory to create executors for asynchronous event handling
 * </p>
 * .
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IEventExecutorFactory extends IShutdown {

    /**
     * Gets the current {@link java.util.concurrent.ExecutorService} associate to.
     *
     * @return Returns the current {@link java.util.concurrent.ExecutorService} associate to.
     */
    @Nonnull
    ExecutorService getExecutor();

}
