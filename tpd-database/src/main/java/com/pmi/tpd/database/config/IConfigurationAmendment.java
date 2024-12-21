package com.pmi.tpd.database.config;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.Nonnull;

/**
 * Encapsulates the modifications to be performed to
 * {@code app-config.properties}. First {@link #isAmendable(String)}
 * is invoked to check if the line should be amended. If it is
 * {@link #amend(java.io.Writer, String)} is invoked. After
 * the file has been fully processed {@link #finalize(java.io.Writer)} is
 * invoked, giving the amendment the chance to do
 * any additions.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IConfigurationAmendment {

  /**
   * @param line
   *               line being amended.
   * @param writer
   *               where the amendment must be written
   */
  void amend(@Nonnull Writer writer, @Nonnull String line) throws IOException;

  /**
   * Do any pending tasks once amendments have completed.
   *
   * @param writer
   *               where the amendment was made
   */
  void finalize(@Nonnull Writer writer) throws IOException;

  /**
   * Test the line to see if this amendment can process it. If {@code false}, this
   * line must not be passed to
   * {@link #amend(java.io.Writer, String)}
   *
   * @param line
   *             The line to test
   * @return {@code true} if the line can be amended. {@code false} otherwise
   */
  boolean isAmendable(@Nonnull String line);
}
