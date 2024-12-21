package com.pmi.tpd.core.cluster.spring;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OutOfMemoryHandler;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.cluster.NodePassivationException;

/**
 * A factory bean for hazelcast which checks whether the application was
 * passivated as part of the construction of the
 * {@link HazelcastInstance}
 * <p>
 * <b>Warning:</b> This bean uses setter injection because constructor injection
 * can cause issues with Spring's type
 * detection for FactoryBeans. Spring needs to instantiate FactoryBeans so it
 * can call getObjectType, and using
 * constructor injection can cause irreconcilable circular dependencies. Using
 * setter injection allows circular
 * dependencies to be automatically reconciled so that type detection can be
 * applied correctly.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class HazelcastFactoryBean extends AbstractFactoryBean<HazelcastInstance> {

  /** */
  private final Config config;

  /** */
  private final IEventAdvisorService<?> eventAdvisorService;

  public HazelcastFactoryBean(@Nonnull final Config config,
      @Nonnull final IEventAdvisorService<?> eventAdvisorService) {
    this.config = config;
    this.eventAdvisorService = eventAdvisorService;
  }

  @Override
  public Class<?> getObjectType() {
    return HazelcastInstance.class;
  }

  /**
   * @param outOfMemoryHandler
   */
  public void setOutOfMemoryHandler(final OutOfMemoryHandler outOfMemoryHandler) {
    Hazelcast.setOutOfMemoryHandler(outOfMemoryHandler);
  }

  @Override
  protected HazelcastInstance createInstance() {
    final HazelcastInstance hazelcast = Hazelcast.newHazelcastInstance(config);
    checkNotPassivated(hazelcast);

    return hazelcast;
  }

  private void checkNotPassivated(final HazelcastInstance hazelcast) {
    for (final Event event : eventAdvisorService.getEventContainer().getEvents()) {
      if (IEventAdvisorService.EVENT_TYPE_NODE_PASSIVATED.equals(event.getKey().getType())) {
        hazelcast.shutdown();
        throw new NodePassivationException();
      }
    }
  }
}
