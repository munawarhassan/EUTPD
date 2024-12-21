package com.pmi.tpd.api.event.advisor;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.api.event.advisor.event.EventType;

public interface IEventAdvisorAccessor extends Serializable {

  /**
   * @param level
   * @return
   */
  @Nonnull
  Optional<EventLevel> getEventLevel(String level);

  @Nonnull
  Optional<EventType> getEventType(String type);

  /**
   * Retrieves the statically-bound {@link IEventContainer}.
   * <p/>
   * Note: If has not been {@link #initialize(String) initialised}, this method
   * <i>will not</i> initialise it. Before
   * attempting to use, it is left to the application developer to ensure it has
   * been correctly initialised.
   *
   * @return the working event container
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Nonnull
  IEventContainer getEventContainer();

  void publishEvent(final Event event);

  void discardEvent(final Event event);

}
