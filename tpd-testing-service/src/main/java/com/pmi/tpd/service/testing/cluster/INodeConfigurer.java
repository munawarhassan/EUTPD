package com.pmi.tpd.service.testing.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface INodeConfigurer {

    /**
     * @param paramInt
     * @return
     */
    Config createConfig(int paramInt);

    /**
     * @param paramHazelcastInstance
     */
    void preDestroy(HazelcastInstance paramHazelcastInstance);

    /**
     * @param paramHazelcastInstance
     */
    void postCreate(HazelcastInstance paramHazelcastInstance);

    /**
     * @param paramHazelcastInstance
     */
    void onReset(HazelcastInstance paramHazelcastInstance);
}
