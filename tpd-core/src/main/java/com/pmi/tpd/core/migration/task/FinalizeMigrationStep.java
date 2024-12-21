package com.pmi.tpd.core.migration.task;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.migration.IMigrationState;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.IDatabaseConfigurationService;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class FinalizeMigrationStep extends AbstractRunnableTask {

    /** */
    private final IDatabaseConfigurationService configurationService;

    /** */
    private final I18nService i18nService;

    /** */
    private volatile int progress;

    /** */
    private final IMigrationState state;

    /**
     * @param state
     * @param configurationService
     * @param i18nService
     */
    @Inject
    public FinalizeMigrationStep(final IMigrationState state, final IDatabaseConfigurationService configurationService,
            final I18nService i18nService) {

        this.configurationService = configurationService;
        this.i18nService = i18nService;
        this.state = state;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return new ProgressImpl(i18nService.getMessage("app.migration.finalizing"), progress);
    }

    @Override
    public void run() {
        final IDataSourceConfiguration configuration = state.getTargetDatabase().getConfiguration();

        final String message = "Migrated to database at " + configuration.getUrl();

        // persist the new datasource configuration
        configurationService.saveDataSourceConfiguration(configuration, Optional.of(message));

        // and release the resources associated with state
        state.getSourceDatabase().close();

        progress = 100;
    }
}
