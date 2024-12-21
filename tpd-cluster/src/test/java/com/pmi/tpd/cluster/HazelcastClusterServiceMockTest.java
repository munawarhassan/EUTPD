package com.pmi.tpd.cluster;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastClusterServiceMockTest extends MockitoTestCase {

    @Mock(lenient = true)
    private Cluster cluster;

    @Mock
    private HazelcastInstance hazelcast;

    @Mock
    private LifecycleService lifecycleService;

    @Mock(lenient = true)
    private Member member;

    private HazelcastClusterService service;

    private UUID id;

    @BeforeEach
    public void setUp() throws Exception {
        when(cluster.getMembers()).thenReturn(Collections.singleton(member));
        when(cluster.getLocalMember()).thenReturn(member);

        when(hazelcast.getCluster()).thenReturn(cluster);
        when(hazelcast.getLifecycleService()).thenReturn(lifecycleService);
        id = UUID.randomUUID();
        when(member.getUuid()).thenReturn(id);

        service = new HazelcastClusterService(hazelcast);
    }

    @Test
    public void testGetInformation() {
        final IClusterInformation information = service.getInformation();
        assertNotNull(information);
        assertFalse(information.isRunning()); // Nothing trained on LifecycleService

        final IClusterNode localNode = information.getLocalNode();
        assertNotNull(localNode);
        assertEquals(id, localNode.getId());

        final Set<IClusterNode> nodes = information.getNodes();
        assertNotNull(nodes);
        assertEquals(1, nodes.size());
        assertEquals(id, Iterables.getOnlyElement(nodes).getId());
    }

    @Test
    public void testIsAvailable() {
        when(lifecycleService.isRunning()).thenReturn(false, true);

        assertFalse(service.isAvailable());
        assertTrue(service.isAvailable());

        verify(lifecycleService, times(2)).isRunning();
    }

    @Test
    public void testIsClustered() {
        final Member second = mock(Member.class, withSettings().lenient());
        when(second.getUuid()).thenReturn(UUID.randomUUID());

        when(lifecycleService.isRunning()).thenReturn(false, true);

        assertFalse(service.isClustered()); // Not available
        assertFalse(service.isClustered()); // Available, but only one node

        reset(cluster);
        when(cluster.getMembers()).thenReturn(ImmutableSet.of(member, second));

        assertTrue(service.isClustered()); // Available and 2 members in the cluster
    }
}
