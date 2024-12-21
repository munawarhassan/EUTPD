package com.pmi.tpd.scheduler.exec.support;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.pmi.tpd.api.exec.ICompletionCallback;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * Track the currently running {@link IRunnableTask task}, providing state-management functionality around its
 * execution, and other functionality needed.
 * <p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultTaskMonitor implements ITaskMonitor, Runnable {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTaskMonitor.class);

    /**
     * We don't rely on the ListenableFutureTask's callbacks for callbacks from external parties because we need to
     * precisely control when they are called ie within run() after the final state of the task is determined.
     */
    private final List<ICompletionCallback> callbacks;

    /** */
    @Nonnull
    private final String cancelToken;

    /** */
    private final ListenableFutureTask<Void> future;

    /** */
    private final I18nService i18nService;

    /** */
    @Nonnull
    private final String id;

    /** */
    private final CountDownLatch latch;

    /** */
    @Nonnull
    private final UUID nodeId;

    /** */
    private final String sessionId;

    /** */
    private final long startTime;

    /** */
    @Nonnull
    private final IRunnableTask task;

    /** */
    private final AtomicReference<State> state;

    /**
     * @param task
     * @param id
     * @param type
     * @param nodeId
     * @param sessionId
     * @param cancelToken
     * @param i18nService
     */
    public DefaultTaskMonitor(final @Nonnull IRunnableTask task, final @Nonnull String id, final @Nonnull UUID nodeId,
            final String sessionId, final @Nonnull String cancelToken, final I18nService i18nService) {
        this.cancelToken = Assert.checkNotNull(cancelToken, "cancelToken");
        this.i18nService = i18nService;
        this.id = id;
        this.nodeId = nodeId;
        this.sessionId = sessionId;
        this.task = Assert.checkNotNull(task, "task");

        callbacks = new CopyOnWriteArrayList<>();
        future = ListenableFutureTask.create(this, null);
        future.addListener(() -> {
            // Cancelling can come from MaintenanceTaskMonitor.cancel() or from a shutdown of the executor the
            // task was executing in
            if (future.isCancelled()) {
                DefaultTaskMonitor.this.task.cancel();
            }
        }, MoreExecutors.directExecutor());
        latch = new CountDownLatch(1);
        startTime = System.currentTimeMillis();
        state = new AtomicReference<>(State.CREATED);
    }

    @Override
    public void registerCallback(final ICompletionCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public void awaitCompletion() {
        try {
            Uninterruptibles.getUninterruptibly(future);
        } catch (final ExecutionException e) {
            final Throwable cause = Throwables.getRootCause(e);
            // Rethrow as an Error or RuntimeException
            Throwables.throwIfUnchecked(cause);
            // Otherwise wrap with a RuntimeException and throw
            Throwables.throwIfUnchecked(cause);
            throw new RuntimeException(cause);
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean cancel(@Nonnull final String token, final long timeout, @Nonnull final TimeUnit unit) {
        checkNotNull(token, "token");
        checkNotNull(unit, "unit");

        if (cancelToken.equals(token)) {
            // If we've already tried to cancel, there's no point in calling future.cancel(true) again. Instead, just
            // skip to waiting on the latch for the task to exit
            if (!future.isCancelled()) {
                // We don't cancel the task directly here because we need both explicit calls to cancel and
                // unsolicited ListenableFutureTask cancellation (from the ExecutorService) to be handled in the same
                // way. The migration task will throw a MaintenanceCancelled exception from run() in response to this
                // which will ultimately set the state to CANCELLED
                future.cancel(true);
            }

            try {
                if (state.compareAndSet(State.WAITING, State.CANCELED)) {
                    // If we get here, we've managed to cancel the task before it even entered run(). The
                    // ScheduledThreadPoolExecutor on which we are scheduled documents: "When a submitted task is
                    // cancelled before it is run, execution is suppressed." In this case latch.countDown() will
                    // never be called, so we don't want to wait for it.
                    LOGGER.warn("{} task was canceled before it started", getTaskName());
                    for (final ICompletionCallback callback : callbacks) {
                        try {
                            callback.onCancellation();
                        } catch (final Exception e) {
                            LOGGER.warn("Error while executing a callback on cancellation", e);
                        }
                    }
                } else {
                    latch.await(timeout, unit);
                }

                return state.get() == State.CANCELED;
            } catch (final InterruptedException e) {
                LOGGER.warn("{} task did not cancel within the {} {} timeout", getTaskName(), timeout, unit);
                return false;
            }
        } else {
            throw new IncorrectTokenException(i18nService.createKeyedMessage("app.scheduler.task.incorrecttoken"),
                    token);
        }
    }

    @Nonnull
    @Override
    public String getCancelToken() {
        return cancelToken;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    @Nonnull
    @Override
    public UUID getOwnerNodeId() {
        return nodeId;
    }

    @Override
    public String getOwnerSessionId() {
        return sessionId;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return task.getProgress();
    }

    @Nonnull
    @Override
    public Date getStartTime() {
        return new Date(startTime);
    }

    @Nonnull
    @Override
    public TaskState getState() {
        return state.get().getExternalState();
    }

    @Override
    public boolean isOwner(@Nonnull final IRequestContext requestContext) {
        checkNotNull(requestContext, "requestContext");

        // IRequestContext.getSessionId() is @Nullable, so this call won't create a session if one doesn't already
        // exist. If the session doesn't exist it couldn't possibly be the owning session for the maintenance, so
        // the functionality here is as-desired.
        return sessionId != null && sessionId.equals(requestContext.getSessionId());
    }

    @Override
    public void run() {
        // In the product, we only ever run() maintenance tasks that are in the WAITING state. Allowing CREATED tasks
        // to run() as well is currently only for unit tests.
        if (!(state.compareAndSet(State.WAITING, State.RUNNING) || state.compareAndSet(State.CREATED, State.RUNNING))) {
            LOGGER.warn("{} {}  task attempted to run (Canceled: {})",
                state.get(),
                getTaskName(),
                future.isCancelled());
            return;
        }

        try {
            task.run();

            // Even if cancellation was requested, if the task completes processing without throwing an exception
            // assume that means the cancel granularity in the processing did not detect cancellation before full
            // processing was completed and that maintenance was successful.
            LOGGER.debug("{} maintenance has completed successfully (Canceled: {})",
                getTaskName(),
                future.isCancelled());
            state.set(State.SUCCESSFUL);

            for (final ICompletionCallback callback : callbacks) {
                try {
                    callback.onSuccess();
                } catch (final Exception e) {
                    LOGGER.warn("Error while executing a callback on task success", e);
                }
            }
        } catch (final Throwable t) {
            final String name = t.getClass().getSimpleName();
            if (future.isCancelled()) {
                LOGGER.warn("{} task has been canceled (Cause: {}: {})", getTaskName(), name, t.getMessage(), t);
                state.set(State.CANCELED);
            } else {
                // Otherwise, any other exception thrown indicates maintenance failed
                LOGGER.warn("{} task has failed (Cause: {}: {})", getTaskName(), name, t.getMessage(), t);
                state.set(State.FAILED);
            }

            for (final ICompletionCallback callback : callbacks) {
                try {
                    if (state.get() == State.FAILED) {
                        callback.onFailure(t);
                    } else {
                        callback.onCancellation();
                    }
                } catch (final Exception e) {
                    LOGGER.warn("Error while executing a callback on task failure or cancellation", e);
                }
            }

            // Callable.call() can't throw Throwable, but we know the Throwable has to be either an Error or an
            // Exception, so pick the appropriate one, cast and rethrow
            if (t instanceof Error) {
                throw (Error) t;
            }

            // We are guaranteed t is a RuntimeException at this point
            throw (RuntimeException) t;
        } finally {
            // Whether processing succeeds or fails, once it's over countDown() the latch to free waiters
            latch.countDown();
        }
    }

    public void submitTo(final ExecutorService executorService) {
        state.set(State.WAITING);

        // After updating the task, submit it for processing
        executorService.submit(future);
    }

    protected String getTaskName() {
        return this.task.getName();
    }

    private enum State {

        CANCELED(TaskState.CANCELED),
        CREATED,
        FAILED(TaskState.FAILED),
        RUNNING,
        SUCCESSFUL(TaskState.SUCCESSFUL),
        WAITING;

        private final TaskState actionState;

        State() {
            this(TaskState.RUNNING);
        }

        State(final TaskState actionState) {
            this.actionState = actionState;
        }

        @Nonnull
        public TaskState getExternalState() {
            return actionState;
        }
    }
}
