package com.pmi.tpd.api.event.advisor.event;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * EventType class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventType {

  /** */
  private final String description;

  /** */
  private final String type;

  /**
   * <p>
   * Constructor for EventType.
   * </p>
   *
   * @param type
   *                    a {@link java.lang.String} object.
   * @param description
   *                    a {@link java.lang.String} object.
   */
  public EventType(@Nonnull final String type, @Nonnull final String description) {
    this.type = Assert.notNull(type);
    this.description = Assert.notNull(description);
  }

  /**
   * <p>
   * getType.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  @Nonnull
  public String getType() {
    return type;
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
    if (!(o instanceof EventType)) {
      return false;
    }

    final EventType e = (EventType) o;
    return Objects.equals(getDescription(), e.getDescription()) && Objects.equals(getType(), e.getType());
  }

  @Override
  public int hashCode() {
    int result = 29;
    result = 31 * result + Objects.hashCode(getType());
    result = 31 * result + Objects.hashCode(getDescription());
    return result;
  }

  @Override
  public String toString() {
    return "(EventType: " + type + ")";
  }

}
