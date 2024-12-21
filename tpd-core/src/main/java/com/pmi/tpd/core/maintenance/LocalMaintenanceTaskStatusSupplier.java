package com.pmi.tpd.core.maintenance;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link IMaintenanceTaskStatusSupplier} which holds the status in a local atomic reference.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LocalMaintenanceTaskStatusSupplier implements IMaintenanceTaskStatusSupplier {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMaintenanceTaskStatusSupplier.class);

    /** */
    private final AtomicReference<IRunnableMaintenanceTaskStatus> statusRef = new AtomicReference<>();

    @Override
    public IRunnableMaintenanceTaskStatus get() {
        return statusRef.get();
    }

    @Override
    public void set(@Nonnull final IRunnableMaintenanceTaskStatus status) {
        checkNotNull(status, "status");
        IRunnableMaintenanceTaskStatus existing;
        do {
            existing = statusRef.get();
            // Only update the status if the task IDs match or the new status is for a more recent maintenance
            // task. This helps to handle when a split brain occurs and a node cannot keep up to date with
            // the latest status
            if (existing != null && !existing.getId().equals(status.getId())
                    && existing.getStartTime().after(status.getStartTime())) {
                LOGGER.warn("Ignoring maintenance task status {} as existing status {} is newer", status, existing);
                break;
            }
        } while (!statusRef.compareAndSet(existing, status));
    }

}
