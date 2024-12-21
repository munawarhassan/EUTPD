package com.pmi.tpd.core.migration.task;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Throwables;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.core.migration.CanceledMigrationException;
import com.pmi.tpd.core.migration.IMigrationState;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.core.migration.SimpleMigrationState;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseLatch;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.scheduler.exec.CompositeRunableTask;
import com.pmi.tpd.scheduler.exec.support.DrainHelper;

/**
 * @author Christophe Friederich
 * @since
 */
public abstract class BaseMigrationTask implements IRunnableTask {

    /** */
    public static final String QUALIFIER_TARGET_DATABASE = "targetDatabase";

    /** */
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseMigrationTask.class);

    /** */
    protected final I18nService i18nService;

    /** */
    protected final IMigrationState state;

    /** */
    protected volatile boolean canceled;

    /** */
    private final IDatabaseManager databaseManager;

    /** */
    protected IEventPublisher eventPublisher;

    /** */
    private final IRunnableTask delegateTask;

    /** */
    @Value("${database.migration.drain.timeout:90}")
    private int drainTimeoutSeconds;

    /** */

    @Value("${database.migration.drain.force.timeout:30}")
    private int forceDrainTimeoutSeconds;

    protected BaseMigrationTask(final IDatabaseManager databaseManager, final IEventPublisher eventPublisher,
            final IMigrationTaskFactory factory, final I18nService i18nService,
            final IEventAdvisorService<?> eventAdvisorService, final IDatabaseHandle targetDatabase) {
        this.i18nService = i18nService;
        this.databaseManager = databaseManager;
        this.eventPublisher = eventPublisher;

        // create a migrationState bean and register it in the beanFactory
        state = new SimpleMigrationState(databaseManager.getHandle(), targetDatabase);

        // create the delegate task
        delegateTask = createMigrationTask(factory, state, eventAdvisorService);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getDrainTimeoutSeconds() {
        return drainTimeoutSeconds;
    }

    public int getForceDrainTimeoutSeconds() {
        return forceDrainTimeoutSeconds;
    }

    public void setDrainTimeoutSeconds(final int drainTimeoutSeconds) {
        this.drainTimeoutSeconds = drainTimeoutSeconds;
    }

    public void setForceDrainTimeoutSeconds(final int forceDrainTimeoutSeconds) {
        this.forceDrainTimeoutSeconds = forceDrainTimeoutSeconds;
    }

    @Override
    public void cancel() {
        canceled = true;
        delegateTask.cancel();
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return delegateTask.getProgress();
    }

    @Override
    public void run() {
        try {
            delegateTask.run();

            if (canceled) {
                throw new CanceledMigrationException(i18nService.createKeyedMessage("app.migration.canceled"));
            }
        } catch (final Throwable t) {
            // The migration failed or was canceled. Revert the configuration/data source/session factory and then
            // propagate the exception.
            if (canceled) {
                LOGGER.info("Reverting database configuration after migration was canceled");
            } else {
                LOGGER.error("Reverting database configuration after a failed migration attempt", t);
            }
            revert();

            Throwables.throwIfUnchecked(t);
            throw new RuntimeException(t);
        }
    }

    protected IRunnableTask createMigrationTask(final IMigrationTaskFactory factory,
        final IMigrationState state,
        final IEventAdvisorService<?> eventAdvisorService) {
        return new CompositeRunableTask.Builder()
                .add(
                    factory.backupPhaseBuilder(state)
                            .add(factory.latchAndDrainDatabaseMigrationStep(LatchMode.LOCAL), 5) // 5
                            .add(factory.releaseAffixedDatabaseStep(), 2) // 7
                            .add(factory.databaseBackupStep(state), 90) // 97
                            .add(factory.configurationBackupStep(state), 3) // 100 backup
                            .build(),
                    82)
                .add(
                    factory.restorePhaseBuilder(state)
                            .add(factory.unpackBackupFilesStep(state), 0)
                            .add(factory.databaseRestoreStep(state), 100)
                            .add(factory.unlatchDatabaseStep(LatchMode.LOCAL, state.getTargetDatabase()), 0)
                            .build(),
                    18) // 100 overall
                .add(factory.finalizeMigrationStep(state), 0)
                .build();
    }

    /**
     * Reverts the {@link com.pmi.tpd.database.IDataSourceConfiguration DataSourceConfiguration}, {@code DataSource} and
     * {@code SessionFactoryImplementor}, restoring them to their pre-migration values. If the swappables were
     * {@link DatabaseManager#isLatched()} latched}, the latch is released to allow any blocked callers to continue
     * their processing on the original database.
     */
    private void revert() {
        IDatabaseLatch latch = databaseManager.getCurrentLatch();
        if (latch == null) {
            // the database may have been swapped over and unlatched already.
            latch = databaseManager.acquireLatch(LatchMode.LOCAL);
            if (!DrainHelper.drain(latch, drainTimeoutSeconds, forceDrainTimeoutSeconds)) {
                LOGGER.warn("Could not drain all database connections while reverting migration. Switching back to the "
                        + "original database anyway.");
            }
        }

        // This will restore the swappables to reference the source data source and session factory, and also
        // release the latch to ensure any calls that were parked on it are allowed to continue.
        latch.unlatchTo(state.getSourceDatabase());

        // Close the new data source, because the system will never be updated to use it. Note that, at this time,
        // the new database is an unknown state and may require system administrator interaction before it can be
        // used as a migration target again.
        state.getTargetDatabase().close();
    }
}
