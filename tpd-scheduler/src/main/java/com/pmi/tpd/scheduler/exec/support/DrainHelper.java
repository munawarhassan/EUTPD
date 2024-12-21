package com.pmi.tpd.scheduler.exec.support;

import java.util.concurrent.TimeUnit;

import com.pmi.tpd.cluster.latch.ILatch;

/**
 * Assists in draining {@link ILatch latches} for maintenance tasks in a standardised way.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DrainHelper {

    /**
     * Standard behaviour for draining {@link ILatch latches}. An initial drain is performed and if this fails to
     * succeed in the specified timeout a further forceful drain is performed (but only if the force-drain timeout for
     * this is non-negative).
     *
     * @param latch
     *            the latch to drain
     * @param drainTimeoutSeconds
     *            the non-negative timeout to wait for the latch to drain of its own accord
     * @param forceDrainTimeoutSeconds
     *            the timeout to wait for the latch to drain before forcibly draining
     * @return if draining succeeded
     */
    public static boolean drain(final ILatch latch,
        final long drainTimeoutSeconds,
        final long forceDrainTimeoutSeconds) {
        // 1. Drain
        boolean drained = latch.drain(drainTimeoutSeconds, TimeUnit.SECONDS);
        // 2. Force drain if force drain timeout >= 0
        if (!drained && forceDrainTimeoutSeconds >= 0) {
            drained = latch.forceDrain(forceDrainTimeoutSeconds, TimeUnit.SECONDS);
        }
        return drained;
    }

    private DrainHelper() {
    }
}
