package com.pmi.tpd.cluster.hazelcast;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MemberSelector;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class NodeIdMemberSelectorTest extends MockitoTestCase {

    @Test
    public void testMatches() {
        final UUID id = UUID.randomUUID();
        final MemberSelector selector = new NodeIdMemberSelector(id);

        assertTrue(selector.select(mockMember(id)));
        assertFalse(selector.select(mockMember(UUID.randomUUID())));
    }

    private Member mockMember(final UUID id) {
        final Member member = mock(Member.class);
        when(member.getUuid()).thenReturn(id);
        return member;
    }
}
