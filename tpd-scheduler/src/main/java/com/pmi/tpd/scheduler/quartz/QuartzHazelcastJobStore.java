package com.pmi.tpd.scheduler.quartz;

import com.hazelcast.core.Hazelcast;
import com.pmi.tpd.cluster.hazelcast.HazelcastConstants;
import com.pmi.tpd.scheduler.quartz.hazelcast.HazelcastJobStore;

/**
 * A subclass of {@link HazelcastJobStore} which wires up the correct hazelcast instance
 */
public class QuartzHazelcastJobStore extends HazelcastJobStore {

  public QuartzHazelcastJobStore() {
    super(Hazelcast.getHazelcastInstanceByName(HazelcastConstants.HAZELCAST_INSTANCE_NAME));
  }

}
