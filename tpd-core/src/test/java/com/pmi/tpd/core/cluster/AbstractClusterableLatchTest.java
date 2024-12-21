package com.pmi.tpd.core.cluster;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.cluster.latch.LatchState;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.service.testing.cluster.SpringManagedCluster;
import com.pmi.tpd.testing.junit5.TestCase;

public class AbstractClusterableLatchTest extends TestCase {

    @RegisterExtension
    public static SpringManagedCluster cluster = new SpringManagedCluster.Builder().size(3).build();

    private SomeLatchableService node1Service;

    private SomeLatchableService node2Service;

    private SomeLatchableService node3Service;

    @BeforeAll
    public static void setupEvent() throws IOException {
        try (InputStream in = getResourceAsStream(AbstractClusterableLatchTest.class, "minimal-event-config.xml")) {
            EventAdvisorService.initialize(in);
        }
    }

    @AfterAll
    public static void tearDownEvent() {
        EventAdvisorService.getInstance().terminate();
    }

    @BeforeEach
    public void setUp() throws Exception {
        cluster.reset();

        node1Service = createLatchableService(0);
        node2Service = createLatchableService(1);
        node3Service = createLatchableService(2);

        cluster.registerBeansForNode(0, node1Service);
        cluster.registerBeansForNode(1, node2Service);
        cluster.registerBeansForNode(2, node3Service);
    }

    @AfterEach
    public void clearAnyEvents() {
        final IEventContainer container = EventAdvisorService.getInstance().getEventContainer();
        while (container.hasEvents()) {
            container.discardEvent(container.getEvents().iterator().next());
        }
    }

    @Test
    public void testAcquireCluster() {
        final SomeLatchableService.SomeLatch latch1 = node1Service.acquireLatch(LatchMode.CLUSTER);

        // verify node1
        assertNotNull(latch1);
        assertEquals(LatchMode.CLUSTER, latch1.getMode());
        assertLatch(latch1, 1, 0, 0);

        // verify node2 and node 3
        assertLatch(node2Service.getCurrentLatch(), 1, 0, 0);
        assertLatch(node3Service.getCurrentLatch(), 1, 0, 0);
    }

    @Test
    @Timeout(5)
    public void testAcquireClusterFailureUnlocks() throws InterruptedException {
        assertThrows(IllegalStateException.class, () -> {
            node2Service.failOnAcquire = true;
            node3Service.failOnUnlatch = true;

            try {
                node1Service.acquireLatch(LatchMode.CLUSTER);
                fail("node1Service.acquireLatch should have thrown an IllegalStateException");
            } finally {
                // node 1 and node 2 should be unlatched
                assertFalse(node1Service.isLatched());
                assertFalse(node2Service.isLatched());

                // node 3 should be evented, but it happens async so we may have to wait a bit
                // for the task
                // to complete
                assertTrue(node3Service.isLatched());
                while (!EventAdvisorService.getInstance().getEventContainer().hasEvents()) {
                    Thread.sleep(25L);
                }
                assertTrue(EventAdvisorService.getInstance().getEventContainer().hasEvents());
            }
        });
    }

    @Test
    public void testDrainCluster() {
        final SomeLatchableService.SomeLatch latch1 = node1Service.acquireLatch(LatchMode.CLUSTER);
        final SomeLatchableService.SomeLatch latch2 = node2Service.getCurrentLatch();
        final SomeLatchableService.SomeLatch latch3 = node3Service.getCurrentLatch();

        assertNotNull(latch2);
        assertNotNull(latch3);

        // both latches will fail to drain until SomeLatch.drained is set manually
        assertFalse(latch1.drain(25, TimeUnit.MILLISECONDS));
        assertFalse(latch2.drain(25, TimeUnit.MILLISECONDS));
        assertFalse(latch3.drain(25, TimeUnit.MILLISECONDS));

        // simulate one node having drained, both latches should still fail to drain
        latch1.drained = true;
        assertFalse(latch1.drain(25, TimeUnit.MILLISECONDS));
        assertFalse(latch2.drain(25, TimeUnit.MILLISECONDS));
        assertFalse(latch3.drain(25, TimeUnit.MILLISECONDS));

        // now simulate all nodes having drained
        latch2.drained = true;
        latch3.drained = true;
        assertTrue(latch1.drain(25, TimeUnit.MILLISECONDS));
        assertTrue(latch2.drain(25, TimeUnit.MILLISECONDS));
        assertTrue(latch3.drain(25, TimeUnit.MILLISECONDS));

        // latch1 had drained earlier. As an optimisation, it should not have been asked
        // whether it had drained after
        // it had been discovered that it had already drained
        assertLatch(latch1, 1, 4, 0);
        assertLatch(latch2, 1, 7, 0);
        assertLatch(latch3, 1, 7, 0);
    }

    @Test
    public void testUnlatch() {
        node1Service.acquireLatch(LatchMode.CLUSTER);

        final SomeLatchableService.SomeLatch latch3 = node3Service.getCurrentLatch();

        assertNotNull(latch3);

        // unlatch latch3 and verify that the other latches have unlatched as well
        latch3.unlatch();

        assertFalse(node1Service.isLatched());
        assertFalse(node2Service.isLatched());
        assertFalse(node3Service.isLatched());
    }

    private void assertLatch(final SomeLatchableService.SomeLatch latch,
        final int expectedAcquireCount,
        final int expectedDrainCount,
        final int expectedUnlatchCount) {
        assertNotNull(latch);
        assertEquals(expectedAcquireCount, latch.acquiredCount);
        assertEquals(expectedDrainCount, latch.drainedCount);
        assertEquals(expectedUnlatchCount, latch.unlatchedCount);
    }

    private SomeLatchableService createLatchableService(final int nodeIndex) {
        final HazelcastInstance hazelcast = cluster.getNode(nodeIndex);
        return new SomeLatchableService(hazelcast.getCluster(), hazelcast.getExecutorService("test.executor"));
    }

    private static class SomeLatchableService implements ILatchableService<ILatch> {

        private final Cluster cluster;

        private final IExecutorService executorService;

        private volatile SomeLatch latch;

        boolean failOnAcquire = false;

        boolean failOnUnlatch = false;

        private SomeLatchableService(final Cluster cluster, final IExecutorService executorService) {
            this.cluster = cluster;
            this.executorService = executorService;
        }

        @Nonnull
        @Override
        public SomeLatch acquireLatch(@Nonnull final LatchMode latchMode) {
            return acquireLatch(latchMode, null);
        }

        @Nonnull
        @Override
        public SomeLatch acquireLatch(@Nonnull final LatchMode latchMode, final String latchId) {
            checkState(latch == null, "already latched!");

            if (failOnAcquire) {
                throw new IllegalStateException("Simulating a failure in acquiring the latch");
            }

            final SomeLatch someLatch = new SomeLatch(latchMode, cluster, executorService, getClass().getName());
            someLatch.acquire(latchId);
            this.latch = someLatch;
            return someLatch;
        }

        @Nullable
        @Override
        public SomeLatch getCurrentLatch() {
            return latch;
        }

        @Override
        public boolean isLatched() {
            return latch != null;
        }

        @Override
        public LatchState getState() {
            final SomeLatch current = latch;

            return current == null ? LatchState.AVAILABLE
                    : current.drain(0, TimeUnit.MILLISECONDS) ? LatchState.DRAINED : LatchState.LATCHED;
        }

        class SomeLatch extends AbstractClusterableLatch {

            volatile int acquiredCount;

            volatile int drainedCount;

            volatile int unlatchedCount;

            volatile boolean drained;

            protected SomeLatch(final LatchMode mode, final Cluster cluster, final IExecutorService executor,
                    final String latchServiceBeanName) {
                super(mode, cluster, executor, EventAdvisorService.getInstance(), latchServiceBeanName);
            }

            @Override
            protected void acquireLocally() {
                acquiredCount++;
            }

            @Override
            protected boolean drainLocally(final long timeout, @Nonnull final TimeUnit timeUnit, final boolean force) {
                drainedCount++;
                return drained;
            }

            @Override
            protected void unlatchLocally() {
                unlatchedCount++;
                if (failOnUnlatch) {
                    throw new IllegalStateException("Simulating failure to unlatch");
                }
                latch = null;
            }
        }
    }
}
