package com.pmi.tpd.cluster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.jupiter.api.Test;

public class ClusterNodeNameResolverTest {

    private static final String FOO = "foo";

    private static final String EMPTY_STRING = "";

    @Test
    public void returnsNodeNameWhenItIsSet() {
        System.setProperty(ClusterNodeNameResolver.CLUSTER_NODE_NAME, FOO);

        assertThat(ClusterNodeNameResolver.getNodeName(), is(FOO));
    }

    @Test
    public void returnsEmptyStringWhenNodeNameNotSet() {
        System.clearProperty(ClusterNodeNameResolver.CLUSTER_NODE_NAME);

        assertThat(ClusterNodeNameResolver.getNodeName(), is(EMPTY_STRING));
    }
}
