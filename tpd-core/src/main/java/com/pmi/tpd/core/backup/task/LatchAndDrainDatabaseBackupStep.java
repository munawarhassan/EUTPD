package com.pmi.tpd.core.backup.task;

import javax.inject.Inject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.maintenance.LatchAndDrainDatabaseStep;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class LatchAndDrainDatabaseBackupStep extends LatchAndDrainDatabaseStep implements InitializingBean {

    /** */
    @Value("${database.backup.drain.timeout}")
    private int drainTimeoutSeconds;

    /** */
    @Value("${database.backup.drain.force.timeout}")
    private int forceDrainTimeoutSeconds;

    /**
     * @param i18nService
     * @param databaseManager
     * @param latchMode
     */
    @Inject
    public LatchAndDrainDatabaseBackupStep(final I18nService i18nService, final IDatabaseManager databaseManager,
            final LatchMode latchMode) {
        super(i18nService, databaseManager, latchMode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setDrainTimeoutSeconds(drainTimeoutSeconds);
        setForceDrainTimeoutSeconds(forceDrainTimeoutSeconds);
    }
}
