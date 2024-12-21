package com.pmi.tpd.api.event.advisor.event;

import java.util.EventObject;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * An {@code EventObject} indicating the provided {@link Event} should be added
 * to the system event container.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class AddEvent extends EventObject {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final Event event;

  /**
   * Constructs a new {@code AddEvent}, setting its source and the system
   * {@link Event} to be added.
   *
   * @param o
   *              the event source
   * @param event
   *              the event to add
   */
  public AddEvent(@Nonnull final Object o, @Nonnull final Event event) {
    super(o);

    this.event = Assert.checkNotNull(event, "event");
  }

  /**
   * Retrieves the system {@link Event} to add to the container.
   *
   * @return the event to add
   */
  @Nonnull
  public Event getEvent() {
    return event;
  }
}
