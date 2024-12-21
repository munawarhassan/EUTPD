package com.pmi.tpd.database.liquibase;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.database.liquibase.backup.processor.IChangeSetProcessor;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * Apply a Liquibase changelog onto a database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DatabaseUpdater implements IChangeSetProcessor {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUpdater.class);

    /** */
    private final Iterator<LiquibaseChangeSetMetaData> outline;

    /** */
    private final ILiquibaseAccessor dao;

    /** */
    private final ILiquibaseRestoreMonitor monitor;

    /** */
    private final int totalNonEmptyChangesetCount;

    /** */
    private int changeSetCount;

    /** */
    private boolean skipChangeset;

    public DatabaseUpdater(@Nonnull final ILiquibaseAccessor dao, @Nonnull final ILiquibaseRestoreMonitor monitor,
            @Nonnull final ChangeLogOutline outline) {
        this.dao = checkNotNull(dao, "liquibaseDao");
        this.monitor = checkNotNull(monitor, "liquibaseRestoreMonitor");
        this.outline = checkNotNull(outline, "outline").iterator();
        this.totalNonEmptyChangesetCount = outline.nonEmptyChangeSetCount();
    }

    @Override
    public void onChangesetBegin(final ChangeSet changeSet) {
        final LiquibaseChangeSetMetaData changeSetSummary = Iterators.getNext(outline, null);
        if (changeSetSummary != null && changeSetSummary.getChangeCount() > 0) {
            skipChangeset = false;

            LOGGER.debug("Executing changeset {} of {}, containing {} changes with id {}",
                changeSetCount,
                totalNonEmptyChangesetCount,
                changeSetSummary.getChangeCount(),
                changeSet.getId());
            changeSetCount++;
            monitor.onBeginChangeset(changeSetSummary, changeSetCount, totalNonEmptyChangesetCount);
            dao.beginChangeSet();
        } else {
            skipChangeset = true;
        }
    }

    @Override
    public void onChangesetContent(final DeleteDataChange change) {
        dao.deleteAllRows(change.getTableName());
        monitor.onAppliedChange();
    }

    @Override
    public void onChangesetContent(final InsertDataChange change) {
        dao.insert(change);
        monitor.onAppliedChange();
    }

    @Override
    public void onChangesetComplete(final ChangeSet changeSet) {
        if (!skipChangeset) {
            dao.endChangeSet();
            monitor.onFinishedChangeset();
        }
    }

}
