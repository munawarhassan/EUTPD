package com.pmi.tpd.database.config;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.util.Assert;

/**
 * Base class for configuration file amendments.
 *
 * @since 3.8
 */
public abstract class AbstractConfigurationAmendment implements IConfigurationAmendment {

  /**
   * The clock that will tell us the current date-time, which we'll then include
   * in the comment block.
   */
  private final IClock clock;

  public AbstractConfigurationAmendment(@Nonnull final IClock clock) {
    this.clock = Assert.checkNotNull(clock, "clock");
  }

  /**
   * Do any pending tasks after file has processed. Amendment might or might not
   * have been applied.
   *
   * @param writer
   *               where the amendment is applied.
   */
  @Override
  public void finalize(@Nonnull final Writer writer) throws IOException {
    // Default no-op
  }

  /**
   * Parses the line and extracts the property key.
   *
   * @return the property key, or an empty string if the line does not have a
   *         property key
   */
  @VisibleForTesting
  @Nonnull
  protected static String getPropertyKey(@Nonnull final String line) {
    return Iterables.getFirst(Splitter.on('=').trimResults().split(line), "");
  }

  @Nonnull
  protected IClock getClock() {
    return clock;
  }

  /**
   * Writes the given text to the writer, followed by a new-line character.
   */
  protected void writeLine(@Nonnull final Writer writer, @Nonnull final String text) throws IOException {
    writer.write(text);
    writer.append('\n');
  }
}
