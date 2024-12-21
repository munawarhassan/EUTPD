package com.pmi.tpd.cluster;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.common.collect.Iterables;
import com.pmi.tpd.service.testing.cluster.HazelcastCluster;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

//This is a unified integration test for the entire HazelcastCluster* set of classes. It does _not_ cover every
//code path in the various classes, which is why the mock tests also exist.
public class HazelcastClusterServiceTest extends MockitoTestCase {

    @RegisterExtension
    public static final HazelcastCluster cluster = new HazelcastCluster.Builder().size(2).build();

    private HazelcastClusterService service;

    @BeforeEach
    public void setup() {
        service = new HazelcastClusterService(cluster.getNode(0));
    }

    @Test
    public void testGetInformation() {
        final IClusterInformation information = service.getInformation();
        assertNotNull(information);
        assertTrue(information.isRunning());

        final IClusterNode localNode = information.getLocalNode();
        assertNotNull(localNode.getAddress());
        assertNotNull(localNode.getId());
        assertTrue(localNode.isLocal());

        Set<IClusterNode> nodes = information.getNodes();
        assertNotNull(nodes);
        assertEquals(2, nodes.size());

        // This verifies equality relationships work for HazelcastClusterNode. Since
        // ClusterInformation offers both
        // getLocalNode() and getNodes(), it's important to have the ability to locate
        // that node in the set
        nodes = new HashSet<>(nodes);
        assertTrue(nodes.remove(localNode));

        final IClusterNode remoteNode = Iterables.getOnlyElement(nodes);
        assertNotNull(remoteNode.getAddress());
        assertNotNull(remoteNode.getId());
        assertNotEquals(localNode.getAddress(), remoteNode.getAddress());
        assertNotEquals(localNode.getId(), remoteNode.getId());
        assertFalse(remoteNode.isLocal());
    }

    @Test
    public void testIsAvailable() {
        assertTrue(service.isAvailable());
    }

    @Test
    public void testIsClustered() {
        assertTrue(service.isClustered());
    }
}
