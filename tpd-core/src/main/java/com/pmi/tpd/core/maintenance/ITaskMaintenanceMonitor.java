package com.pmi.tpd.core.maintenance;

import com.pmi.tpd.scheduler.exec.ITaskMonitor;

/**
 * Tracks the progress and status of a maintenance task and allows it to be canceled.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ITaskMaintenanceMonitor extends ITaskMonitor, IRunnableMaintenanceTaskStatus {

}
