package com.pmi.tpd.core.elasticsearch.task;

import com.pmi.tpd.core.maintenance.ITaskMaintenanceFactory;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface IIndexerTaskFactory extends ITaskMaintenanceFactory {

    IndexingTask indexingTask();

    DatabaseIndexingStep databaseIndexingStep();
}
