package com.pmi.tpd.core.restore;

import java.io.File;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.scheduler.exec.CompositeRunableTask;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class RestorePhase extends CompositeRunableTask {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(RestorePhase.class);

  /** */
  private final IRestoreState state;

  /**
   * @param state
   * @param steps
   * @param totalWeight
   */
  protected RestorePhase(final IRestoreState state, @Nonnull final Step[] steps, final int totalWeight) {
    super(steps, totalWeight);

    this.state = state;
  }

  @Override
  public void run() {
    try {
      super.run();
    } finally {
      final File unpackedDirectory = state.getUnzippedBackupDirectory();
      if (unpackedDirectory != null && unpackedDirectory.exists()) {
        if (FileUtils.deleteQuietly(unpackedDirectory)) {
          LOGGER.debug("Successfully deleted unpacked files from {}", unpackedDirectory.getAbsolutePath());
        } else {
          LOGGER.warn(
              "Failed to delete unpacked files at {}; the directory has been marked for deletion on exit",
              unpackedDirectory.getAbsolutePath());
          // TODO: Apply this to every file and directory
          unpackedDirectory.deleteOnExit();
        }
      }
    }
  }

  /**
   * @author Christophe Friederich @since1.3
   */
  public static class Builder extends CompositeRunableTask.AbstractBuilder<Builder> {

    /** */
    private final IRestoreState state;

    /**
     * @param state
     */
    @Inject
    public Builder(final IRestoreState state) {
      this.state = state;
    }

    // NOT REMOVE: for mockito proxying
    @Override
    public Builder add(@Nonnull final IRunnableTask step, final int weight) {
      return super.add(step, weight);
    }

    @Nonnull
    @Override
    public RestorePhase build() {
      return new RestorePhase(state, steps.toArray(new Step[steps.size()]), totalWeight);
    }

    @Override
    protected Builder self() {
      return this;
    }
  }
}
