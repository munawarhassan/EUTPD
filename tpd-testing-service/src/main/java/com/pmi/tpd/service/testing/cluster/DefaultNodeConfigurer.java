package com.pmi.tpd.service.testing.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultNodeConfigurer implements INodeConfigurer {

    @Override
    public void preDestroy(final HazelcastInstance instance) {
    }

    @Override
    public void postCreate(final HazelcastInstance instance) {
    }

    @Override
    public Config createConfig(final int nodeIndex) {
        return new Config();
    }

    @Override
    public void onReset(final HazelcastInstance instance) {
        for (final DistributedObject object : instance.getDistributedObjects()) {
            object.destroy();
        }
    }
}
