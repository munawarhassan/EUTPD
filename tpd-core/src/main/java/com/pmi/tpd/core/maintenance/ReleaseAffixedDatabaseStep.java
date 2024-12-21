package com.pmi.tpd.core.maintenance;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

import com.google.common.collect.ImmutableList;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.core.migration.MigrationException;
import com.pmi.tpd.database.spi.IDatabaseAffixed;
import com.pmi.tpd.scheduler.exec.AbstractRunnableTask;

/**
 * Replaces listening for {@link JohnsonMaintenanceEvent}s with a deterministic callback to trigger components that are
 * affixed to the current database to release it.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ReleaseAffixedDatabaseStep extends AbstractRunnableTask {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseAffixedDatabaseStep.class);

    /** */
    private final List<IDatabaseAffixed> affixes;

    /** */
    private final I18nService i18nService;

    /** */
    private volatile boolean released;

    @Inject
    public ReleaseAffixedDatabaseStep(final I18nService i18nService, final List<IDatabaseAffixed> affixes) {
        this.affixes = ImmutableList.copyOf(affixes);
        this.i18nService = i18nService;
    }

    @Nonnull
    @Override
    public IProgress getProgress() {
        return new ProgressImpl(i18nService.getMessage("app.migration.releasingdatabase"), released ? 100 : 0);
    }

    @Override
    public void run() {
        for (final IDatabaseAffixed affix : affixes) {
            // DatabaseAffixed.release() implementations _shouldn't_ do any heavy work, but to try and
            // be more responsive to cancellation check for it each iteration
            if (isCanceled()) {
                return;
            }

            try {
                affix.release();
            } catch (final Exception e) {
                final Class<?> componentClass = AopUtils.getTargetClass(affix);
                LOGGER.error("{} failed to release the current database", componentClass.getName(), e);

                throw new MigrationException(i18nService.createKeyedMessage("app.migration.releasedatabasefailed"));
            }
        }

        released = true;
    }
}
