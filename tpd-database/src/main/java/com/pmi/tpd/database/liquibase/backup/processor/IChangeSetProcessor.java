package com.pmi.tpd.database.liquibase.backup.processor;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * Process Liquibase {@link ChangeSet} after they are read from a backup.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IChangeSetProcessor {

    /**
     * Callback when a new changeset is read from the changelog, before any of its
     * {@link liquibase.change.AbstractChange content} is read.
     *
     * @see #onChangesetComplete(liquibase.changelog.ChangeSet)
     */
    void onChangesetBegin(ChangeSet changeSet);

    /**
     * Callback when a {@link InsertDataChange insert} is read.
     * <p>
     * This is called between {@link #onChangesetBegin(liquibase.changelog.ChangeSet)} and
     * {@link #onChangesetComplete(liquibase.changelog.ChangeSet)}.
     */
    void onChangesetContent(InsertDataChange change);

    /**
     * Callback when a {@link DeleteDataChange delete} is read.
     * <p>
     * This is called between {@link #onChangesetBegin(liquibase.changelog.ChangeSet)} and
     * {@link #onChangesetComplete(liquibase.changelog.ChangeSet)}.
     */
    void onChangesetContent(DeleteDataChange change);

    /**
     * Callback after all the {@link liquibase.change.AbstractChange content} of a changeset was read from the
     * changelog.
     *
     * @see #onChangesetBegin(liquibase.changelog.ChangeSet)
     */
    void onChangesetComplete(ChangeSet changeSet);

}
