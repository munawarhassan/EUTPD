package com.pmi.tpd.core.maintenance;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.scheduler.exec.cluster.AbstractLatchAndDrainTask;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class LatchAndDrainDatabaseStep extends AbstractLatchAndDrainTask<IDatabaseLatch> {

    /**
     * @param i18nService
     * @param databaseManager
     * @param latchMode
     */
    public LatchAndDrainDatabaseStep(final I18nService i18nService, final IDatabaseManager databaseManager,
            final LatchMode latchMode) {
        super(i18nService, databaseManager, latchMode);
    }

    @Override
    protected String getMessage() {
        return i18nService.getMessage("app.migration.closingConnections");
    }

    @Override
    protected String getResourceName() {
        return "DataSource";
    }

    @Override
    protected ServiceException newDrainFailedException() {
        throw new MigrationException(i18nService.createKeyedMessage("app.migration.drain.failed"));
    }
}
