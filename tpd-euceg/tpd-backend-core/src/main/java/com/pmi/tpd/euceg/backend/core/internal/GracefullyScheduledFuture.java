package com.pmi.tpd.euceg.backend.core.internal;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author christophe friederich
 * @since 2.5
 */
public class GracefullyScheduledFuture {

    public static GracefullyScheduledFuture scheduleWithFixedDelay(final ScheduledExecutorService scheduler,
        final Runnable task,
        final Duration initialDelay,
        final Duration delay) {
        final CancellableCommand cancellableCommand = new CancellableCommand(task);
        final ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(cancellableCommand,
            initialDelay.toMillis(),
            delay.toMillis(),
            TimeUnit.MILLISECONDS);
        return new GracefullyScheduledFuture(future, cancellableCommand);
    }

    private GracefullyScheduledFuture(final ScheduledFuture<?> targetFuture, final CancellableCommand command) {
        this.targetFuture = targetFuture;
        this.runnable = command;
    }

    private final ScheduledFuture<?> targetFuture;

    private final CancellableCommand runnable;

    public void cancelAndBeSureOfTermination(final boolean mayInterruptIfRunning)
            throws InterruptedException, ExecutionException {
        try {
            targetFuture.cancel(mayInterruptIfRunning);
        } finally {
            runnable.cancel();
        }
    }

    private static class CancellableCommand implements Runnable {

        private static final int NOT_EXECUTING = 0;

        private static final int IN_PROGRESS = 1;

        private static final int CANCELLED_WITHOUT_OBSTRUCTION = 2;

        private static final int CANCELLED_IN_MIDDLE_OF_PROGRESS = 3;

        private final AtomicInteger state = new AtomicInteger(NOT_EXECUTING);

        private final AtomicReference<Thread> executionThread = new AtomicReference<>();

        private final CompletableFuture<Void> cancellationFuture = new CompletableFuture<>();

        private final Runnable target;

        private CancellableCommand(final Runnable target) {
            this.target = target;
        }

        public void cancel() throws ExecutionException, InterruptedException {
            if (executionThread.get() == Thread.currentThread()) {
                // cancel method was called from target by itself
                state.set(CANCELLED_IN_MIDDLE_OF_PROGRESS);
                return;
            }
            while (true) {
                if (state.get() == CANCELLED_WITHOUT_OBSTRUCTION) {
                    return;
                }
                if (state.get() == CANCELLED_IN_MIDDLE_OF_PROGRESS) {
                    cancellationFuture.get();
                    return;
                }
                if (state.compareAndSet(NOT_EXECUTING, CANCELLED_WITHOUT_OBSTRUCTION)) {
                    return;
                }
                if (state.compareAndSet(IN_PROGRESS, CANCELLED_IN_MIDDLE_OF_PROGRESS)) {
                    cancellationFuture.get();
                    return;
                }
            }
        }

        @Override
        public void run() {
            if (!state.compareAndSet(NOT_EXECUTING, IN_PROGRESS)) {
                notifyWaiters();
                return;
            }

            try {
                executionThread.set(Thread.currentThread());
                target.run();
            } finally {
                executionThread.set(null);
                if (!state.compareAndSet(IN_PROGRESS, NOT_EXECUTING)) {
                    notifyWaiters();
                }
            }
        }

        private void notifyWaiters() {
            if (state.get() == CANCELLED_WITHOUT_OBSTRUCTION) {
                // no need to notify anything
                return;
            }
            // someone waits for cancelling
            cancellationFuture.complete(null);
            return;
        }

    }
}