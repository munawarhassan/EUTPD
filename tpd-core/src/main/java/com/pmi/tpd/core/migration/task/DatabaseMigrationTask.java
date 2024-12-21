package com.pmi.tpd.core.migration.task;

import javax.inject.Inject;

import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.maintenance.MaintenanceType;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.core.migration.CanceledMigrationException;
import com.pmi.tpd.core.migration.IMigrationState;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.core.migration.event.MigrationCanceledEvent;
import com.pmi.tpd.core.migration.event.MigrationFailedEvent;
import com.pmi.tpd.core.migration.event.MigrationStartedEvent;
import com.pmi.tpd.core.migration.event.MigrationSucceededEvent;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * Note this class should not be made available for component scan. It requires targetDatabase to be supplied from a
 * child context with the qualifier "{@value #QUALIFIER_TARGET_DATABASE}".
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DatabaseMigrationTask extends BaseMigrationTask {

    /** */
    private final I18nService i18nService;

    /**
     * @param databaseManager
     * @param eventPublisher
     * @param factory
     * @param i18nService
     * @param targetDatabase
     */
    @Inject
    public DatabaseMigrationTask(final IDatabaseManager databaseManager, final IEventPublisher eventPublisher,
            final IMigrationTaskFactory factory, final I18nService i18nService,
            final IEventAdvisorService<?> eventAdvisorService, final IDatabaseHandle targetDatabase) {
        super(databaseManager, eventPublisher, factory, i18nService, eventAdvisorService, targetDatabase);

        this.i18nService = i18nService;
    }

    @Override
    public void run() {
        MigrationException exception = null;
        try {
            eventPublisher.publish(new MigrationStartedEvent(this));

            super.run();
        } catch (final RuntimeException e) {
            exception = convertToMigrationException(e);

            throw exception;
        } finally {
            publishMigrationEvent(exception);
        }
    }

    @Override
    protected IRunnableTask createMigrationTask(final IMigrationTaskFactory factory,
        final IMigrationState state,
        final IEventAdvisorService<?> eventAdvisorService) {
        final MaintenanceApplicationEvent event = new MaintenanceApplicationEvent(
                eventAdvisorService.getEventType("database-unavailable").orElseThrow(),
                Product.getName() + " is unavailable while its data is migrated to a new database",
                eventAdvisorService.getEventLevel(IEventAdvisorService.LEVEL_MAINTENANCE).orElseThrow(),
                MaintenanceType.MIGRATION);

        // The whole migration needs to execute in maintenance mode - wrap it in the MaintenanceModePhase
        return factory.maintenanceModePhaseBuilder()
                .event(event)
                .add(super.createMigrationTask(factory, state, eventAdvisorService), 100)
                .build();
    }

    protected void publishMigrationEvent(final MigrationException exception) {
        if (exception instanceof CanceledMigrationException) {
            eventPublisher.publish(new MigrationCanceledEvent(this));
        } else if (exception != null) {
            eventPublisher.publish(new MigrationFailedEvent(this));
        } else {
            eventPublisher.publish(new MigrationSucceededEvent(this));
        }
    }

    private MigrationException convertToMigrationException(final RuntimeException e) {
        if (e instanceof MigrationException) {
            return (MigrationException) e;
        }
        return new MigrationException(i18nService.createKeyedMessage("app.migration.failed", Product.getName()), e);
    }
}
