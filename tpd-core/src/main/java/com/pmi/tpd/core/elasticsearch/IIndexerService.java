package com.pmi.tpd.core.elasticsearch;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface IIndexerService {

    /**
     * Starts the indexing processing.
     * 
     * @return the task handle which may be used to register listeners or wait for the task to complete
     * @throws IndexingException
     *                           if another task is already in progress, or if the method is called outside the context
     *                           of a user request
     */
    ITaskMaintenanceMonitor performIndex();

    /**
     * @param monitor
     */
    void indexDatabase(@Nonnull final ITaskMonitorProgress monitor);
}
