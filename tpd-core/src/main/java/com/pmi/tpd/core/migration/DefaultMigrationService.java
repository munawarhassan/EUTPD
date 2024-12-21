package com.pmi.tpd.core.migration;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.maintenance.IMaintenanceService;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.core.migration.task.DatabaseMigrationTask;
import com.pmi.tpd.core.migration.task.DatabaseSetupTask;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * A default implementation of the {@link IMigrationService} which uses the {@link IMaintenanceService
 * MaintenanceService} to start migration tasks.
 *
 * @see DatabaseMigrationTask
 * @see DatabaseSetupTask
 */
@PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
public class DefaultMigrationService implements IMigrationService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMigrationService.class);

    /** */
    private final IDatabaseManager databaseManager;

    /** */
    private final I18nService i18nService;

    /** */
    private final IMaintenanceService maintenanceService;

    /** */
    private final IMigrationTaskFactory taskFactory;

    /**
     * @param databaseManager
     * @param i18nService
     * @param maintenanceService
     * @param maintenanceTaskFactory
     */
    @Inject
    public DefaultMigrationService(final IDatabaseManager databaseManager, final I18nService i18nService,
            final IMaintenanceService maintenanceService, final IMigrationTaskFactory maintenanceTaskFactory) {
        this.databaseManager = databaseManager;
        this.i18nService = i18nService;
        this.maintenanceService = maintenanceService;
        this.taskFactory = maintenanceTaskFactory;
    }

    @Override
    public IMaintenanceService getMaintenanceService() {
        return maintenanceService;
    }

    /**
     * @see DatabaseMigrationTask
     */
    @Nonnull
    @Override
    public ITaskMaintenanceMonitor migrate(@Nonnull final IDataSourceConfiguration newConfiguration) {
        checkNotNull(newConfiguration, "newConfiguration");

        try {
            return maintenanceService.start(taskFactory.migrationTask(newConfiguration), MaintenanceType.MIGRATION);
        } catch (final IllegalStateException e) {
            LOGGER.error("An attempt to start a migration was blocked because maintenance is already in progress");
            throw new MigrationException(i18nService.createKeyedMessage("app.migration.already.running"));
        }
    }

    /**
     * @see DatabaseSetupTask
     */
    @Nonnull
    @Override
    public ITaskMaintenanceMonitor setup(@Nonnull final IDataSourceConfiguration newConfiguration) {
        checkNotNull(newConfiguration, "newConfiguration");

        try {
            return maintenanceService.start(taskFactory.setupTask(newConfiguration), MaintenanceType.MIGRATION);
        } catch (final IllegalStateException e) {
            LOGGER.error("An attempt to setup the database was blocked because maintenance is already in progress");
            throw new MigrationException(i18nService.createKeyedMessage("app.migration.already.running"));
        }
    }

    @Override
    public void validateConfiguration(@Nonnull final IDataSourceConfiguration configuration) {
        // Null check is handled inside the MigrationHelper
        databaseManager.validateConfiguration(configuration);
    }
}
