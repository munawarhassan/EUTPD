package com.pmi.tpd.scheduler.exec.cluster;

import org.springframework.aop.support.AopUtils;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * Base class for constructing tasks that unlatch resources.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractUnlatchTask<L extends ILatch> extends AbstractRunnableTask {

    /** */
    protected final I18nService i18nService;

    /** */
    private final ILatchableService<L> latchableService;

    /** */
    private volatile boolean unlatched;

    /**
     * @param i18nService
     * @param latchableService
     */
    protected AbstractUnlatchTask(final I18nService i18nService, final ILatchableService<L> latchableService) {
        this.i18nService = i18nService;
        this.latchableService = latchableService;
    }

    @Override
    public void run() {
        if (unlatched) {
            throw new IllegalStateException(getTypeName() + " already unlatched");
        }

        final L latch = latchableService.getCurrentLatch();
        if (latch == null) {
            throw new IllegalStateException(getTypeName() + " not latched");
        }
        unlatch(latch);

        unlatched = true;
    }

    /**
     * @param latch
     */
    protected void unlatch(final L latch) {
        latch.unlatch();
    }

    /**
     * @return
     */
    protected boolean isUnlatched() {
        return unlatched;
    }

    private String getTypeName() {
        return AopUtils.getTargetClass(latchableService).getSimpleName();
    }
}
