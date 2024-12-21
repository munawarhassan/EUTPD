package com.pmi.tpd.core.elasticsearch.impl;

import javax.annotation.Nonnull;

import org.springframework.context.ApplicationContext;

import com.pmi.tpd.core.elasticsearch.task.DatabaseIndexingStep;
import com.pmi.tpd.core.elasticsearch.task.IIndexerTaskFactory;
import com.pmi.tpd.core.elasticsearch.task.IndexingTask;
import com.pmi.tpd.core.maintenance.MaintenanceModePhase;
import com.pmi.tpd.scheduler.exec.support.SpringTaskFactory;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public class IndexerTaskFactory extends SpringTaskFactory implements IIndexerTaskFactory {

    public IndexerTaskFactory(final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Nonnull
    @Override
    public MaintenanceModePhase.Builder maintenanceModePhaseBuilder() {
        return createInstance(MaintenanceModePhase.Builder.class);
    }

    @Override
    public DatabaseIndexingStep databaseIndexingStep() {
        return createInstance(DatabaseIndexingStep.class);
    }

    @Override
    public IndexingTask indexingTask() {
        return createInstance(IndexingTask.class);
    }

}
