package com.pmi.tpd.core.migration.task;

import javax.inject.Inject;

import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.migration.IMigrationTaskFactory;
import com.pmi.tpd.database.spi.IDatabaseHandle;
import com.pmi.tpd.database.spi.IDatabaseManager;

/**
 * Note this class should not be made available for component scan. It requires targetDatabase to be supplied from a
 * child context with the qualifier "{@value #QUALIFIER_TARGET_DATABASE}".
 *
 * @author Christophe Friederich
 * @since 1.5
 */
public class DatabaseSetupTask extends BaseMigrationTask {

    @Inject
    public DatabaseSetupTask(final IDatabaseManager databaseManager, final IEventPublisher eventPublisher,
            final IMigrationTaskFactory factory, final I18nService i18nService,
            final IEventAdvisorService<?> eventAdvisorService, final IDatabaseHandle targetDatabase) {
        super(databaseManager, eventPublisher, factory, i18nService, eventAdvisorService, targetDatabase);

    }
}
