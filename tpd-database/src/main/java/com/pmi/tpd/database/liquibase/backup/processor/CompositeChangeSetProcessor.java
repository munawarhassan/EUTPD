package com.pmi.tpd.database.liquibase.backup.processor;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * A composite of {@link IChangeSetProcessor}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class CompositeChangeSetProcessor implements IChangeSetProcessor {

  /** */
  private final Collection<IChangeSetProcessor> processors;

  /**
   * @param processors
   */
  public CompositeChangeSetProcessor(@Nonnull final IChangeSetProcessor... processors) {
    this.processors = ImmutableList.copyOf(checkNotNull(processors, "processors"));
  }

  @Override
  public void onChangesetBegin(final ChangeSet changeSet) {
    for (final IChangeSetProcessor processor : processors) {
      processor.onChangesetBegin(changeSet);
    }
  }

  @Override
  public void onChangesetContent(final InsertDataChange change) {
    for (final IChangeSetProcessor processor : processors) {
      processor.onChangesetContent(change);
    }
  }

  @Override
  public void onChangesetContent(final DeleteDataChange change) {
    for (final IChangeSetProcessor processor : processors) {
      processor.onChangesetContent(change);
    }
  }

  @Override
  public void onChangesetComplete(final ChangeSet changeSet) {
    for (final IChangeSetProcessor processor : processors) {
      processor.onChangesetComplete(changeSet);
    }
  }

}
