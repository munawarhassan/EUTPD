package com.pmi.tpd.scheduler.exec.cluster;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;
import com.pmi.tpd.scheduler.exec.support.DrainHelper;

/**
 * A base class for constructing tasks which latch and drain {@link LatchableService latchable services}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractLatchAndDrainTask<L extends ILatch> extends AbstractRunnableTask {

    /** */
    protected final I18nService i18nService;

    /** */
    private long drainTimeoutSeconds;

    /** */
    private long forceDrainTimeoutSeconds;

    /** */
    private final ILatchableService<L> latchableService;

    /** */
    @Nonnull
    private final LatchMode latchMode;

    /** */
    private final Logger log;

    /** */
    private volatile boolean drained;

    /** */
    private volatile Thread drainingThread;

    public AbstractLatchAndDrainTask(final I18nService i18nService, final ILatchableService<L> latchableService,
            @Nonnull final LatchMode latchMode) {
        this.i18nService = i18nService;
        this.latchableService = latchableService;
        this.latchMode = Assert.checkNotNull(latchMode, "latchMode");

        log = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void cancel() {
        super.cancel();

        final Thread thread = drainingThread;
        if (!drained && thread != null && thread.getState() == Thread.State.TIMED_WAITING) {
            thread.interrupt();
        }
    }

    public long getDrainTimeoutSeconds() {
        return drainTimeoutSeconds;
    }

    public void setDrainTimeoutSeconds(final long drainTimeoutSeconds) {
        this.drainTimeoutSeconds = drainTimeoutSeconds;
    }

    public long getForceDrainTimeoutSeconds() {
        return forceDrainTimeoutSeconds;
    }

    public void setForceDrainTimeoutSeconds(final long forceDrainTimeoutSeconds) {
        this.forceDrainTimeoutSeconds = forceDrainTimeoutSeconds;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return new ProgressImpl(getMessage(), drained ? 100 : 0);
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Latching {}", getResourceName());
        }
        final ILatch latch = latchableService.acquireLatch(latchMode);

        // record the drainingThread so the draining can be aborted in cancel
        if (log.isDebugEnabled()) {
            log.debug("Draining {}, timeoutSeconds: {}, forceDrainTimeoutSeconds: {}",
                getResourceName(),
                drainTimeoutSeconds,
                forceDrainTimeoutSeconds);
        }
        drainingThread = Thread.currentThread();
        drained = DrainHelper.drain(latch, drainTimeoutSeconds, forceDrainTimeoutSeconds);
        drainingThread = null;

        if (drained) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully drained the {}", getResourceName());
            }
        } else {
            if (isCanceled()) {
                // Canceling the maintenance task may have interrupted draining, in which case this thread's interrupt
                // flag may be set. This clears that flag because we consider the interruption "handled", and we don't
                // want the next blocking operation to unexpectedly fail (If there are no more blocking operations for
                // task processing, leaving the interrupt flag set means this thread will be destroyed by the executor
                // when it goes to poll for the next task)
                Thread.interrupted();
            }

            log.warn("The {} could not be drained. Aborting...", getResourceName());
            throw newDrainFailedException();
        }
    }

    /**
     * @return
     */
    @Nonnull
    protected abstract String getMessage();

    /**
     * @return
     */
    protected abstract String getResourceName();

    /**
     * @return
     */
    protected abstract ServiceException newDrainFailedException();
}
