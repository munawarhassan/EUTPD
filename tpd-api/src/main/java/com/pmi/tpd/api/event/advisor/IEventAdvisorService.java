package com.pmi.tpd.api.event.advisor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IEventAdvisorService<CONTEXT> extends IEventAdvisorAccessor {

  /** */
  String EVENT_TYPE_NODE_PASSIVATED = "node-passivated";

  /** */
  String LEVEL_MAINTENANCE = "maintenance";

  /** */
  String LEVEL_SYSTEM_MAINTENANCE = "system-maintenance";

  /**
   * Attempts to retrieve the {@link IEventContainer} from the provided context
   * under the key
   * {@link #ATTR_EVENT_CONTAINER} before falling back on the statically-bound
   * instance.
   *
   * @return the working event container
   * @throws IllegalStateException
   *                               if {@link #initialize} has not been called
   */
  @Override
  @Nonnull
  IEventContainer getEventContainer();

  /**
   * Terminates, clearing the statically-bound {@link IEventConfig} and
   * {@link IEventContainer}.
   */
  void terminate();

  /**
   * Terminates , removing event-related attributes from the provided
   * {@code context} and clearing the
   * statically-bound {@link IEventConfig} and {@link IEventContainer}.
   *
   * @param context
   *                the servlet context
   */
  public void terminate(@Nonnull final CONTEXT context);

  /**
   * @return the highest event level within the event container or {@code null} if
   *         the no events are contained
   */
  @Nullable
  String findHighestEventLevel();

  /**
   * @return {@code true} if the {@code level} is higher or equal to the
   *         {@code minimumLevel}
   */
  boolean isLevelAtLeast(@Nonnull final String level, @Nonnull final String minimumLevel);

  /**
   * @return {@code true} if the {@code level} is lower or equal to the
   *         {@code maximumLevel}
   */
  boolean isLevelAtMost(@Nonnull final String level, @Nonnull final String maximumLevel);

}