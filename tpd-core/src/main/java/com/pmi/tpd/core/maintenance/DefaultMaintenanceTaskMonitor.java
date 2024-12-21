package com.pmi.tpd.core.maintenance;

import java.util.UUID;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.scheduler.exec.support.DefaultTaskMonitor;

/**
 * Used by the {@link DefaultMaintenanceService maintenance service} to track the currently running
 * {@link MaintenanceTask task}, providing state-management functionality around its execution, and other functionality
 * needed by the maintenance service.
 * <p>
 * Note: This is not a consumable task, and is not intended to be used outside the maintenance service. It is a separate
 * class to reduce the complexity of the {@link DefaultMaintenanceService}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultMaintenanceTaskMonitor extends DefaultTaskMonitor implements ITaskMaintenanceMonitor, Runnable {

    /** */
    private final MaintenanceType type;

    /**
     * @param task
     * @param id
     * @param type
     * @param nodeId
     * @param sessionId
     * @param cancelToken
     * @param i18nService
     */
    public DefaultMaintenanceTaskMonitor(final IRunnableTask task, final String id, final MaintenanceType type,
            final UUID nodeId, final String sessionId, final String cancelToken, final I18nService i18nService) {
        super(task, id, nodeId, sessionId, cancelToken, i18nService);
        this.type = type;

    }

    @Override
    @Nonnull
    public MaintenanceType getType() {
        return type;
    }

    @Override
    protected String getTaskName() {
        return getType().name();
    }
}
