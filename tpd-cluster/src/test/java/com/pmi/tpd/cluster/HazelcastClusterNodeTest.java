package com.pmi.tpd.cluster;

import static com.hazelcast.instance.EndpointQualifier.MEMBER;

import java.net.InetSocketAddress;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.hazelcast.cluster.Member;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastClusterNodeTest extends MockitoTestCase {

    @Mock
    private Member member;

    @InjectMocks
    private HazelcastClusterNode node;

    @Test
    public void testGetAddress() {
        final InetSocketAddress address = new InetSocketAddress(7990);

        when(member.getSocketAddress(eq(MEMBER))).thenReturn(address);

        assertSame(address, node.getAddress());

        verify(member).getSocketAddress(MEMBER);
    }

    @Test
    public void testGetId() {
        final UUID id = UUID.randomUUID();
        when(member.getUuid()).thenReturn(id);

        assertEquals(id, node.getId());

        verify(member).getUuid();
    }

    @Test
    public void testIsLocal() {
        when(member.localMember()).thenReturn(true);

        assertTrue(node.isLocal());

        verify(member).localMember();
    }
}
