package com.pmi.tpd.core.maintenance;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.hazelcast.collection.ISet;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.maintenance.event.MaintenanceApplicationEvent;
import com.pmi.tpd.service.testing.cluster.HazelcastCluster;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class HazelcastMaintenanceModeHelperTest extends MockitoTestCase {

    // maximum wait time for ItemListener callback
    private final long MAX_WAIT = 2000L;

    @RegisterExtension
    public static HazelcastCluster cluster = new HazelcastCluster.Builder().size(2).build();

    private MockHelper localHelper;

    private static IEventAdvisorService<?> eventAdvisorService;

    @BeforeAll
    public static void setupAdvisor() throws IOException {
        eventAdvisorService = EventAdvisorService
                .initialize(getResourceAsStream(HazelcastMaintenanceModeHelperTest.class, "minimal-event-config.xml"));
    }

    @AfterAll
    public static void tearDownAdvisor() {
        eventAdvisorService.terminate();
    }

    @BeforeEach
    public void setUp() throws Exception {
        cluster.reset();
        localHelper = new MockHelper();
    }

    @Test
    public void testJoinClusterDefault() throws Exception {
        final ISet<MaintenanceApplicationEvent> events = getEventsSet();
        final HazelcastMaintenanceModeHelper helper = new HazelcastMaintenanceModeHelper(events, localHelper);
        helper.init();

        // there are no events in the cluster-wide set -> the cluster is not in maintenance mode. The local node should
        // not be locked
        assertFalse(localHelper.wasLocked(0));
    }

    @Test
    public void testJoinClusterInMaintenanceMode() throws Exception {
        // prepare the cluster state
        final MaintenanceApplicationEvent event = createMaintenanceEvent();
        final ISet<MaintenanceApplicationEvent> events = getEventsSet();
        events.add(event);

        // now create a new helper
        final HazelcastMaintenanceModeHelper helper = new HazelcastMaintenanceModeHelper(events, localHelper);
        helper.init();

        assertTrue(localHelper.wasLocked(0));
    }

    @Test
    public void testLockAndUnlockLocally() throws Exception {
        final ISet<MaintenanceApplicationEvent> events = getEventsSet();
        final HazelcastMaintenanceModeHelper helper = new HazelcastMaintenanceModeHelper(events, localHelper);
        helper.init();

        // lock locally
        final MaintenanceApplicationEvent event = createMaintenanceEvent();
        helper.lock(event);
        assertTrue(localHelper.wasLocked(MAX_WAIT)); // locking happens async through the ItemListener
        assertEquals(1, events.size());

        helper.unlock(event);
        assertTrue(localHelper.wasUnlocked(MAX_WAIT)); // unlocking happens async through the ItemListener
        assertTrue(events.isEmpty());
    }

    @Test
    public void testLockAndUnlockRemotely() throws Exception {
        final ISet<MaintenanceApplicationEvent> events = getEventsSet();
        final HazelcastMaintenanceModeHelper helper = new HazelcastMaintenanceModeHelper(events, localHelper);
        helper.init();

        // lock remotely - add the event to the events set
        final MaintenanceApplicationEvent event = createMaintenanceEvent();
        events.add(event);

        assertTrue(localHelper.wasLocked(MAX_WAIT)); // locking happens async through the ItemListener

        // unlock remotely - remove the event from the events set
        events.remove(event);
        assertTrue(localHelper.wasUnlocked(MAX_WAIT)); // unlocking happens async through the ItemListener
    }

    private MaintenanceApplicationEvent createMaintenanceEvent() {
        return new MaintenanceApplicationEvent(eventAdvisorService.getEventType("performing-maintenance").orElseThrow(),
                "backup task", eventAdvisorService.getEventLevel("maintenance").orElseThrow(), MaintenanceType.BACKUP);
    }

    private ISet<MaintenanceApplicationEvent> getEventsSet() {
        return cluster.getNode(0).getSet("maintenance.events");
    }

    private static class MockHelper implements IMaintenanceModeHelper {

        private final CountDownLatch lockLatch = new CountDownLatch(1);

        private final CountDownLatch unlockLatch = new CountDownLatch(1);

        @Override
        public void lock(@Nonnull final MaintenanceApplicationEvent event) {
            lockLatch.countDown();
        }

        @Override
        public void unlock(@Nonnull final MaintenanceApplicationEvent event) {
            unlockLatch.countDown();
        }

        boolean wasLocked(final long timeoutMillis) throws InterruptedException {
            return lockLatch.await(timeoutMillis, TimeUnit.SECONDS);
        }

        boolean wasUnlocked(final long timeoutMillis) throws InterruptedException {
            return unlockLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }
}
