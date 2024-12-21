package com.pmi.tpd.service.testing.cluster;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;

public class StandaloneNodeConfigurer extends DefaultNodeConfigurer {

    @Override
    public Config createConfig(final int nodeIndex) {
        final Config config = super.createConfig(nodeIndex);

        final JoinConfig join = config.getNetworkConfig().getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(false);
        join.getAwsConfig().setEnabled(false);

        return config;
    }
}
