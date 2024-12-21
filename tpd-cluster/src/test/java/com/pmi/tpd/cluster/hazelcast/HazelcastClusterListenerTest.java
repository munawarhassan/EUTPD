package com.pmi.tpd.cluster.hazelcast;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.hazelcast.core.HazelcastInstance;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.cluster.event.ClusterNodeRemovedEvent;
import com.pmi.tpd.service.testing.cluster.HazelcastCluster;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastClusterListenerTest extends MockitoTestCase {

  // Allow the cluster to "grow" by 1 so that we can test both added and removed
  // events
  @RegisterExtension
  public static final HazelcastCluster cluster = new HazelcastCluster.Builder().size(1, 2).build();

  @Mock
  private IEventPublisher eventPublisher;

  @Test
  public void testMembershipEvents() {
    final ArgumentCaptor<ClusterNodeAddedEvent> addCaptor = ArgumentCaptor.forClass(ClusterNodeAddedEvent.class);
    final ArgumentCaptor<ClusterNodeRemovedEvent> removeCaptor = ArgumentCaptor.forClass(ClusterNodeRemovedEvent.class);

    final HazelcastClusterListener listener = new HazelcastClusterListener(eventPublisher, cluster.getNode(0));
    listener.register();
    try {
      final HazelcastInstance instance = cluster.addNode();
      verify(eventPublisher, timeout(2000)).publish(addCaptor.capture());

      final ClusterNodeAddedEvent addEvent = addCaptor.getValue();
      assertNotNull(addEvent);
      assertEquals(instance.getCluster().getLocalMember().getUuid(), addEvent.getAddedNode().getId());
      assertTrue(addEvent.getCurrentNodes().contains(addEvent.getAddedNode()));

      reset(eventPublisher); // Just to make sure the verification doesn't get confused

      instance.shutdown();
      verify(eventPublisher, timeout(2000)).publish(removeCaptor.capture());

      final ClusterNodeRemovedEvent removeEvent = removeCaptor.getValue();
      assertNotNull(removeEvent);
      assertEquals(addEvent.getAddedNode().getId(), removeEvent.getRemovedNode().getId());
      assertFalse(removeEvent.getCurrentNodes().contains(removeEvent.getRemovedNode()));
    } finally {
      assertTrue(listener.unregister()); // Ensure our unregistration logic works as expected
    }
  }
}
