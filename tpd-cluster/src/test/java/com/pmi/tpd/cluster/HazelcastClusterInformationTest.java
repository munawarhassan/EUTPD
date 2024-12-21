package com.pmi.tpd.cluster;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.util.Set;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastClusterInformationTest extends MockitoTestCase {

    @Mock(lenient = true)
    private Cluster cluster;

    @Mock
    private HazelcastInstance hazelcast;

    private HazelcastClusterInformation information;

    @Mock(lenient = true)
    private Member local;

    @Mock(lenient = true)
    private Member remote;

    private final UUID localId = UUID.fromString("83945aa2-3657-4247-8e9b-d0dca41fd685");

    private final UUID remoteId = UUID.fromString("f80ae673-0452-4e46-9565-8a5e2317fb40");

    @BeforeEach
    public void setUp() throws Exception {
        when(cluster.getLocalMember()).thenReturn(local);
        when(cluster.getMembers()).thenReturn(ImmutableSet.of(local, remote));

        when(hazelcast.getCluster()).thenReturn(cluster);
        when(local.getUuid()).thenReturn(localId);
        when(remote.getUuid()).thenReturn(remoteId);

        information = new HazelcastClusterInformation(hazelcast);
    }

    @Test
    public void testGetLocalNode() {
        final IClusterNode node = information.getLocalNode();
        assertNotNull(node);
        assertEquals(localId, node.getId());

        verify(cluster).getLocalMember();
    }

    @Test
    public void testGetNodes() {
        final Set<IClusterNode> nodes = information.getNodes();
        assertNotNull(nodes);
        assertEquals(2, nodes.size());
        // "local" < "remote"
        assertThat(nodes,
            Matchers.contains(hasProperty("id", equalTo(local.getUuid())),
                hasProperty("id", equalTo(remote.getUuid()))));
    }

    @Test
    public void testIsRunning() {
        final LifecycleService lifecycleService = mock(LifecycleService.class);
        when(lifecycleService.isRunning()).thenReturn(true);

        when(hazelcast.getLifecycleService()).thenReturn(lifecycleService);

        assertTrue(information.isRunning());

        verify(hazelcast).getLifecycleService();
        verify(lifecycleService).isRunning();
    }
}
