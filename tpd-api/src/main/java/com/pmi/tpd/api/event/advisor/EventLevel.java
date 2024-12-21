package com.pmi.tpd.api.event.advisor;

import static com.pmi.tpd.api.util.Assert.notNull;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * <p>
 * EventLevel class.
 * </p>
 *
 * @author devacfr
 */
public class EventLevel {

  /** */
  public static final String ERROR = "error";

  /** */
  public static final String FATAL = "fatal";

  /** */
  public static final String WARNING = "warning";

  /** */
  private final String description;

  /** */
  private final String level;

  /**
   * <p>
   * Constructor for EventLevel.
   * </p>
   *
   * @param level
   *                    a {@link java.lang.String} object.
   * @param description
   *                    a {@link java.lang.String} object.
   */
  public EventLevel(@Nonnull final String level, @Nonnull final String description) {
    this.level = notNull(level);
    this.description = notNull(description);
  }

  /**
   * <p>
   * getLevel.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Nonnull
  public String getLevel() {
    return level;
  }

  /**
   * <p>
   * Getter for the field <code>description</code>.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Nonnull
  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventLevel)) {
      return false;
    }

    final EventLevel e = (EventLevel) o;
    return Objects.equals(getDescription(), e.getDescription()) && Objects.equals(getLevel(), e.getLevel());
  }

  @Override
  public int hashCode() {
    int result = 7;
    result = 31 * result + Objects.hashCode(getLevel());
    result = 31 * result + Objects.hashCode(getDescription());
    return result;
  }

  @Override
  public String toString() {
    return "(EventLevel: " + level + ")";
  }
}
