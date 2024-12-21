package com.pmi.tpd.api.context;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

/**
 * An interface for determining what time it is.
 * <p>
 * The use of this type can make classes more testable, since it can be mocked
 * to deliver predictable date-times.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IClock {

  /** */
  long nanoTime();

  /**
   * @return
   */
  @Nonnull
  DateTime now();
}
