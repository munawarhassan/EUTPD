package com.pmi.tpd.core.maintenance;

import static org.mockito.ArgumentMatchers.same;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.cluster.Member;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import com.pmi.tpd.cluster.IClusterNode;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class ClusteredMaintenanceTaskStatusSupplierTest extends MockitoTestCase {

    public static final UUID ADDED_NODE_ID = UUID.randomUUID();

    public static final UUID LOCAL_NODE_ID = UUID.randomUUID();

    @Mock
    private IMaintenanceTaskStatusSupplier delegate;

    @Mock
    private IRunnableMaintenanceTaskStatus status;

    @Mock
    private ITopic<IRunnableMaintenanceTaskStatus> topic;

    @Mock(lenient = true)
    private IClusterNode localNode;

    @Mock(lenient = true)
    private IClusterNode addedNode;

    @Mock
    private Member member;

    private ClusteredMaintenanceTaskStatusSupplier holder;

    @BeforeEach
    public void setUp() throws Exception {
        holder = new ClusteredMaintenanceTaskStatusSupplier(delegate, topic);
        when(localNode.getId()).thenReturn(LOCAL_NODE_ID);
        when(localNode.isLocal()).thenReturn(true);
        when(addedNode.getId()).thenReturn(ADDED_NODE_ID);
    }

    @Test
    public void testOnNodeAddedWhenNoStatus() throws Exception {
        holder.onNodeAdded(newEvent());
        verifyZeroInteractions(topic);
    }

    @Test
    public void testOnNodeAddedWhenStatusOwnedByLocal() throws Exception {
        mockStatus(LOCAL_NODE_ID);
        holder.onNodeAdded(newEvent());
        verify(topic).publish(same(status));
    }

    @Test
    public void testOnNodeAddedWhenStatusOwnedByOtherNode() throws Exception {
        mockStatus(ADDED_NODE_ID);
        holder.onNodeAdded(newEvent());
        verifyZeroInteractions(topic);
    }

    @Test
    public void testOnNodeAddedWhenStatusOwnedByNeither() throws Exception {
        mockStatus(UUID.randomUUID());
        holder.onNodeAdded(newEvent());
        verify(topic).publish(same(status));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testListener() throws Exception {
        final UUID id = UUID.randomUUID();
        doReturn(id).when(topic).addMessageListener(any(MessageListener.class));
        holder.addListener();

        final ArgumentCaptor<MessageListener<IRunnableMaintenanceTaskStatus>> captor = (ArgumentCaptor) ArgumentCaptor
                .forClass(MessageListener.class);
        verify(topic).addMessageListener(captor.capture());
        final MessageListener<IRunnableMaintenanceTaskStatus> listener = captor.getValue();

        // Remote node
        when(member.localMember()).thenReturn(false);
        listener.onMessage(new Message<>("topicName", status, 0L, member));
        verify(delegate).set(same(status));
        verifyNoMoreInteractions(delegate);

        // Local node
        when(member.localMember()).thenReturn(true);
        listener.onMessage(new Message("topicName", status, 0L, member));
        verifyNoMoreInteractions(delegate);

        holder.removeListener();
        verify(topic).removeMessageListener(id);
    }

    @Test
    public void testRemoveListenerWhenNotRegistered() throws Exception {
        holder.removeListener();
        verifyZeroInteractions(topic);
    }

    @Test
    public void testSet() throws Exception {
        holder.set(status);
        final InOrder ordered = inOrder(delegate, topic);
        ordered.verify(delegate).set(same(status));
        ordered.verify(topic).publish(same(status));
    }

    private void mockStatus(final UUID ownerId) {
        when(status.getOwnerNodeId()).thenReturn(ownerId);
        when(holder.get()).thenReturn(status);
    }

    private ClusterNodeAddedEvent newEvent() {
        return new ClusterNodeAddedEvent(this, addedNode, ImmutableSet.of(addedNode, localNode));
    }
}
