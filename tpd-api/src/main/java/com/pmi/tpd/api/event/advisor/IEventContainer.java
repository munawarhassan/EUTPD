package com.pmi.tpd.api.event.advisor;

import java.util.List;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.event.Event;

/**
 * Interface defining a container for Event Application {@link Event}s
 * <p/>
 * Event Application maintains <i>exactly one</i> event container, which is
 * accessed statically. As a result, all
 * implementations of this interface are required to be thread-safe. However,
 * because Event-Application may be used to
 * filter all requests to the application, implementations should be careful in
 * how they achieve that thread-safety.
 * Using heavy synchronisation techniques may impose significant performance
 * penalties.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IEventContainer {

  /**
   * Adds the provided event to the collection.
   *
   * @param event
   *              the event to add
   */
  void publishEvent(@Nonnull Event event);

  /**
   * Retrieves an <i>immutable</i> view of the contained {@link Event}s.
   *
   * @return the current events
   */
  @Nonnull
  List<Event> getEvents();

  /**
   * Retrieves a flag indicating whether there are {@link Event}s in the
   * container.
   * <p/>
   * This can be thought of as a shortcut for {@code !getEvents().isEmpty()}.
   *
   * @return {@code true} if there are events; otherwise, {@code false}
   */
  boolean hasEvents();

  /**
   * Removes the specified {@link Event} from the container, if it can be found.
   * <p/>
   * Warning: Due to how {@link Event#equals(Object)} and {@link Event#hashCode()}
   * are implemented, an <i>exact</i>
   * match on every field is required in order to match an event. As a result, it
   * may be necessary to iterate over the
   * {@link #getEvents() events} in the collection and remove the event using the
   * exact instance already in the
   * collection.
   *
   * @param event
   *              the event to remove
   */
  void discardEvent(@Nonnull Event event);
}
