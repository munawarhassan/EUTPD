package com.pmi.tpd.database.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.Nonnull;

import org.joda.time.format.ISODateTimeFormat;

import com.pmi.tpd.api.context.IClock;

/**
 * Removes properties and replaces each of them with a commented line.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class RemovePropertiesAmendment extends AbstractConfigurationAmendment {

  /** */
  protected final Set<String> properties;

  public static String formatComment(@Nonnull final String line, @Nonnull final IClock clock) {
    return String.format("# %s removed by unattended setup on %s",
        getPropertyKey(line),
        ISODateTimeFormat.dateTime().print(clock.now()));
  }

  /**
   * @param clock
   * @param properties
   */
  public RemovePropertiesAmendment(@Nonnull final IClock clock, @Nonnull final Set<String> properties) {
    super(clock);

    this.properties = checkNotNull(properties);
  }

  /**
   * A line is amendable if a property key can be found and it is also present in
   * {@link #properties}.
   *
   * @param line
   *             The line to test
   * @return {@code true} if the line can be amended. {@code false} otherwise.
   */
  @Override
  public boolean isAmendable(@Nonnull final String line) {
    return isAmendableProperty(line);
  }

  /**
   * Replace the line with a comment letting users know what we've changed.
   *
   * @param line
   *               line being amended.
   * @param writer
   *               where the amendment must be written
   * @throws IOException
   *                     If there are I/O problems writing to the writer.
   */
  @Override
  public void amend(@Nonnull final Writer writer, @Nonnull final String line) throws IOException {
    writeLine(writer, formatComment(line, getClock()));
  }

  /**
   * @param line
   * @return
   */
  protected boolean isAmendableProperty(@Nonnull final String line) {
    return properties.contains(getPropertyKey(line));
  }
}
