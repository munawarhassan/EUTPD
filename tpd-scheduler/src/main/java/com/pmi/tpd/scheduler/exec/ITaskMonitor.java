package com.pmi.tpd.scheduler.exec;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exec.ICompletionCallback;

/**
 * Tracks the progress and status of a maintenance task and allows it to be canceled.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ITaskMonitor extends IRunnableTaskStatus {

    /**
     * Blocks the current thread until the task is complete.
     */
    void awaitCompletion();

    /**
     * Cancels the in-progress task, waiting the configured timeout for processing to complete.
     *
     * @param token
     *                 the token to authorise canceling the in-progress task
     * @param timeout
     *                 the duration to wait for the task to complete after being canceled
     * @param timeUnit
     *                 the measurement unit for the {@code timeout} duration
     * @return {@code true} if the in-progress task detected the cancellation request and aborted; otherwise,
     *         {@code false} to indicate that the task completed normally without detecting the cancellation request
     *         <i>or</i> the task still has not completed
     */
    boolean cancel(@Nonnull String token, long timeout, @Nonnull TimeUnit timeUnit);

    /**
     * Registers a {@link ICompletionCallback callback} to be executed upon task completion. If the task has already
     * completed the callback will be executed immediately.
     *
     * @param callback
     *                 the callback
     */
    void registerCallback(ICompletionCallback callback);
}
