package com.pmi.tpd.api.event.advisor.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.EventObject;

import javax.annotation.Nonnull;

/**
 * An {@code EventObject} indicating the provided {@link Event} should be
 * removed from the system event container.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class RemoveEvent extends EventObject {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final Event event;

  /**
   * Constructs a new {@code RemoveEvent}, setting its source and the system
   * {@link Event} to be removed.
   *
   * @param o
   *              the event source
   * @param event
   *              the event to removed
   */
  public RemoveEvent(@Nonnull final Object o, @Nonnull final Event event) {
    super(o);

    this.event = checkNotNull(event, "event");
  }

  /**
   * Retrieves the system {@link Event} to remove from the container.
   *
   * @return the event to remove
   */
  @Nonnull
  public Event getEvent() {
    return event;
  }
}
