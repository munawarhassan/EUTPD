package com.pmi.tpd.core.maintenance;

import static java.util.Optional.of;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.Spy;

import com.google.common.base.Throwables;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.cp.IAtomicReference;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.IProgress;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.ProgressImpl;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.cluster.IClusterInformation;
import com.pmi.tpd.cluster.IClusterNode;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.cluster.annotation.ClusterableTask;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.cluster.latch.LatchState;
import com.pmi.tpd.core.backup.CanceledBackupException;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.service.testing.cluster.SpringManagedCluster;
import com.pmi.tpd.service.testing.junit5.AbstractServiceTest;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

public class DefaultMaintenanceServiceTest extends AbstractServiceTest {

    @RegisterExtension
    public static SpringManagedCluster cluster = new SpringManagedCluster.Builder().size(2).build();

    @Mock(lenient = true)
    private IAuthenticationContext authenticationContext;

    private IClusterService[] clusterServices;

    private int counter;

    @Mock
    private IDatabaseManager databaseManager;

    @Mock
    private IEventPublisher eventPublisher;

    private ScheduledExecutorService executorService;

    @Spy
    private final I18nService i18nService = new SimpleI18nService();

    @Spy
    private final IMaintenanceTaskStatusSupplier latestTask = new LocalMaintenanceTaskStatusSupplier();

    @Mock(lenient = true)
    private IRequestManager requestManager;

    private DefaultMaintenanceService service1;

    private DefaultMaintenanceService service2;

    @Mock(lenient = true)
    private ISecureTokenGenerator tokenGenerator;

    private static IEventAdvisorService<?> eventAdvisorService;

    public DefaultMaintenanceServiceTest() {
        super(DefaultMaintenanceService.class, IMaintenanceService.class);
    }

    @BeforeAll
    public static void setUpEvents() throws IOException {
        try (final InputStream in = getResourceAsStream(DefaultMaintenanceServiceTest.class,
            "minimal-event-config.xml")) {
            eventAdvisorService = EventAdvisorService.initialize(in);
        }
    }

    @AfterAll
    public static void tearDownEvents() {
        eventAdvisorService.terminate();
    }

    @BeforeEach
    public void setupMocks() {
        cluster.reset();
        ++counter;

        assumeThat("nodes haven't formed a cluster!", cluster.getNode(0).getCluster().getMembers().size(), equalTo(2));
        assumeThat("nodes haven't formed a cluster!", cluster.getNode(1).getCluster().getMembers().size(), equalTo(2));

        executorService = Executors.newSingleThreadScheduledExecutor();
        clusterServices = new IClusterService[2];

        service1 = createMaintenanceService(0);
        service2 = createMaintenanceService(1);
    }

    @AfterEach
    public void tearDown() throws Exception {
        executorService.shutdownNow();
    }

    @Test
    public void testGetLockContextOutsideMaintenance() {
        assertNull(service1.getLock());
    }

  @Test
  public void testGetStatus() {
    when(databaseManager.getState()).thenReturn(LatchState.LATCHED);

    final IMaintenanceStatus status = service1.getStatus();
    assertNotNull(status);
    assertEquals(LatchState.LATCHED, status.getDatabaseState());
  }

    @Test
    public void testGetTaskContextOutsideMaintenance() {
        assertNull(service1.getRunningTask());
    }

    @Test
    public void testJoinClusterWhileClusterIsLocking() throws InterruptedException {
        mockAuthenticatedAs("user-join-cluster-while-cluster-is-locking");

        // there is a race condition where the cluster is being in maintenance mode just as a node is joining the
        // cluster. The newly joined/joining node may not receive the IExecutor command that locks the node, leaving
        // the node unlocked while the rest of the cluster is locked.

        // set a the clusterLock
        final IAtomicReference<ClusterMaintenanceLock> clusterLock = cluster.getNode(0)
                .getCPSubsystem()
                .getAtomicReference("cluster-lock-" + counter);
        clusterLock.set(new ClusterMaintenanceLock(authenticationContext.getCurrentUser().get(), "pre-defined-token",
                null, null, null));

        // verify that the node isn't locked yet
        assertNull(service1.getNodeLock());

        // notify that a node has been added to the cluster
        service1.onNodeAdded(mock(ClusterNodeAddedEvent.class));

        // sleep for a very short time to allow node 1 to lock itself on the executor thread
        Thread.sleep(50L);

        final IMaintenanceLock lock = service1.getNodeLock();
        assertNotNull(lock);
        assertEquals("pre-defined-token", lock.getUnlockToken());
    }

    @Test
    public void testLockAnonymously() {
        assertThrows(AuthorisationException.class, () -> service1.lock());
    }

    @Test
    public void testLockAndUnlockDifferentNodes() {
        final String token = "token-lock-and-unlock-different-nodes";

        mockAuthenticatedAs("user-lock-and-unlock-different-nodes");
        when(tokenGenerator.generateToken()).thenReturn(token, "other");

        final IMaintenanceLock lock = service1.lock();
        assertNotNull(lock);

        // verify that the system is locked for maintenance on the other node as well
        final IMaintenanceLock retrieved = service2.getLock();
        assertNotNull(retrieved);
        assertEquals(retrieved, service2.getLock());
        assertEquals(token, retrieved.getUnlockToken());
        assertEquals("user-lock-and-unlock-different-nodes", retrieved.getOwner().getUsername());

        retrieved.unlock(token);

        // verify that the system is no longer locked
        assertNull(service1.getLock());
        assertNull(service2.getLock());
    }

    @Test
    public void testLockAndUnlockSameNode() {
        final String token = "token-lock-and-unlock-same-node";

        mockAuthenticatedAs("user-lock-and-unlock-same-node");
        when(tokenGenerator.generateToken()).thenReturn(token);

        final IMaintenanceLock lock = service1.lock();

        // verify that the system is locked for maintenance
        assertNotNull(lock);
        assertNotNull(service1.getLock());
        assertNotNull(service2.getLock());
        assertEquals(token, lock.getUnlockToken());
        assertSame("user-lock-and-unlock-same-node", lock.getOwner().getUsername());

        lock.unlock(token);

        // verify that the system is no longer locked
        assertNull(service1.getLock());
        assertNull(service2.getLock());
    }

    @Test
    public void testLockWhileLocked() {
        assertThrows(LockedMaintenanceException.class, () -> {
            mockAuthenticatedAs("user-lock-while-locked");
            when(tokenGenerator.generateToken()).thenReturn("token-lock-while-locked");

            service1.lock();
            service1.lock();
        });
    }

    // test what happens when system is locked for maintenance while the one node is already locked. This is an
    // inconsistent state, possibly due to a split-brain. It shouldn't be possible because of the join checks, but
    // still worth testing
    @Test
    public void testLockWhileSingleNodeLocked() {
        mockAuthenticatedAs("user-lock-while-single-node-locked");
        when(tokenGenerator.generateToken()).thenReturn("token-lock-while-single-node-locked");

        // sanity check that the clusterLock is not set
        final IAtomicReference<ClusterMaintenanceLock> clusterLock = cluster.getNode(0)
                .getCPSubsystem()
                .getAtomicReference("cluster-lock-" + counter);
        Assumptions.assumeTrue(clusterLock.get() == null, "clusterlock should not be present!");

        // lock node 1 locally - no cluster lock
        service1.lockNode(new DefaultMaintenanceLock(eventPublisher, i18nService, eventAdvisorService,
                authenticationContext.getCurrentUser().get(), "node1-local-token"));
        assertNotNull(service1.getLock(), "service 1 was not locked");
        assertNotNull(service1.getNodeLock(), "service 1 was not locked locally");

        try {
            service2.lock();
            fail(LockFailedMaintenanceException.class.getName() + " expected");
        } catch (final LockFailedMaintenanceException e) {
            // verify that node 2 is not locked
            assertNull(service2.getLock());
            assertNull(service2.getNodeLock());
        }
    }

    @Test
    @Timeout(5)
    public void testStartFromBackgroundThread() {
        assertThrows(IllegalStateException.class, () -> {
            final ClusterMaintenanceTask task = mock(ClusterMaintenanceTask.class);
            service1.start(task, MaintenanceType.MIGRATION);
        });
    }

    @Test
    @Timeout(10)
    public void testStartClusterableTaskInCluster() throws InterruptedException {
        doTestStart(new ClusterMaintenanceTask("some progress", 2));
    }

    @Test
    @Timeout(5)
    public void testStartClusterableTaskInStandalone() throws InterruptedException {
        mockStandaloneSetup();

        // starting a ClusterableTask in a standalone install should be possible
        doTestStart(new ClusterMaintenanceTask("some progress", 2));
    }

    @Test
    @Timeout(5)
    public void testStartStandaloneTaskInCluster() throws InterruptedException {
        assertThrows(UnsupportedMaintenanceException.class, () -> {
            doTestStart(new StandaloneMaintenanceTask("some progress", 2));
        });

    }

    @Test
    @Timeout(10)
    public void testStartStandaloneTaskInStandalone() throws InterruptedException {
        mockStandaloneSetup();

        doTestStart(new StandaloneMaintenanceTask("some progress", 2));
    }

    @Test
    public void testStartWhileTaskIsRunning() {
        assertThrows(IllegalStateException.class, () -> {
            final String sessionId = "ssssssessssionId";
            final String token = "cancel-me";

            final IRequestContext requestContext = mock(IRequestContext.class);
            when(requestContext.getSessionId()).thenReturn(sessionId);
            when(requestManager.getRequestContext()).thenReturn(requestContext);
            when(tokenGenerator.generateToken()).thenReturn(token);

            service1.start(new ClusterMaintenanceTask("progress", 1), MaintenanceType.MIGRATION);
            service1.start(new ClusterMaintenanceTask("progress task2", 1), MaintenanceType.MIGRATION);
        });
    }

    @Test
    public void testRemotelyRunningTaskCanBeCanceled() throws Exception {
        mockAuthenticatedAs("user-remotely-running-task-can-be-cancelled");
        final ClusterMaintenanceTask task = new ClusterMaintenanceTask("progress", 5);
        when(tokenGenerator.generateToken()).thenReturn("cancel-token-remotely-running-task-can-be-cancelled");

        service1.start(task, MaintenanceType.BACKUP);

        // retrieve the current monitor
        final ITaskMaintenanceMonitor monitor1 = service1.getRunningTask();
        final ITaskMaintenanceMonitor monitor2 = service2.getRunningTask();

        // because it's running remotely, monitor should be non-null
        assertNotNull(monitor1);
        assertNotNull(monitor2);

        // cancel remotely
        assertTrue(monitor2.cancel("cancel-token-remotely-running-task-can-be-cancelled", 10, TimeUnit.SECONDS));
        assertTrue(task.isCanceled());
    }

    @Test
    public void testUnlockWrongToken() {
        assertThrows(IncorrectTokenException.class, () -> {
            mockAuthenticatedAs("user-unlock-wrong-token");
            when(tokenGenerator.generateToken()).thenReturn("token-unlock-wrong-token");

            final IMaintenanceLock lock = service1.lock();
            lock.unlock("wrong-token");
        });
    }

    @Test
    public void testUnlockWrongTokenOtherNode() {
        assertThrows(IncorrectTokenException.class, () -> {
            mockAuthenticatedAs("user");
            when(tokenGenerator.generateToken()).thenReturn("token");

            service1.lock();
            final IMaintenanceLock lock = service2.getLock();

            assertNotNull(lock);
            lock.unlock("wrong-token");
        });

    }

    private DefaultMaintenanceService createMaintenanceService(final int nodeIndex) {
        final HazelcastInstance hazelcast = cluster.getNode(nodeIndex);

        final IClusterService clusterService = mock(IClusterService.class, withSettings().lenient());
        clusterServices[nodeIndex] = clusterService;
        when(clusterService.isClustered()).thenReturn(true);

        final UUID nodeId = hazelcast.getCluster().getLocalMember().getUuid();
        final IClusterInformation clusterInfo = mock(IClusterInformation.class, withSettings().lenient());
        final IClusterNode node = mock(IClusterNode.class, withSettings().lenient());
        when(node.getId()).thenReturn(nodeId);
        when(clusterInfo.getLocalNode()).thenReturn(node);
        when(clusterService.getInformation()).thenReturn(clusterInfo);
        when(clusterService.getNodeId()).thenReturn(nodeId);

        final IExecutorService clusterExecutorService = hazelcast.getExecutorService("app.core." + counter);
        final IAtomicReference<ClusterMaintenanceLock> clusterLock = hazelcast.getCPSubsystem()
                .getAtomicReference("cluster-lock-" + counter);
        final IAtomicLong isActive = hazelcast.getCPSubsystem().getAtomicLong("task-active-" + counter);

        final DefaultMaintenanceService service = new DefaultMaintenanceService(authenticationContext,
                clusterExecutorService, clusterService, databaseManager, eventPublisher, executorService,
                eventAdvisorService, i18nService, requestManager, tokenGenerator, latestTask, isActive, clusterLock);
        // no delays to speed up testing
        service.setNodeJoinCheckDelayMillis(0, TimeUnit.MILLISECONDS);

        cluster.registerBeansForNode(nodeIndex, clusterExecutorService, i18nService, service);
        return service;
    }

    private void doTestStart(final LatchingMaintenanceTask task) throws InterruptedException {
        final String sessionId = "ssssssessssionId";
        final String token = "cancel-me";

        final IRequestContext requestContext = mock(IRequestContext.class, withSettings().lenient());
        when(requestContext.getSessionId()).thenReturn(sessionId);
        when(requestManager.getRequestContext()).thenReturn(requestContext);
        when(tokenGenerator.generateToken()).thenReturn(token);

        final ITaskMaintenanceMonitor taskMonitor = service1.start(task, MaintenanceType.BACKUP);
        assertNotNull(taskMonitor);

        assertEquals(token, taskMonitor.getCancelToken());
        assertTrue(taskMonitor.isOwner(requestContext));
        assertSame(taskMonitor, service1.getRunningTask());

        // verify the status
        IMaintenanceStatus status = service1.getStatus();
        assertEquals(TaskState.RUNNING, status.getLatestTask().getState());
        assertEquals(task.getProgress().getMessage(), status.getLatestTask().getProgress().getMessage());

        // let the task finish
        task.release();
        taskMonitor.awaitCompletion();

        // verify that the task is marked as having completed
        assertNull(service1.getRunningTask());

        // verify the status
        status = service1.getStatus();
        assertEquals(TaskState.SUCCESSFUL, status.getLatestTask().getState());
        assertEquals(task.getProgress().getMessage(), status.getLatestTask().getProgress().getMessage());
        assertNull(service1.getRunningTask());
    }

    private void mockAuthenticatedAs(final String username) {
        mockAuthenticatedAs(username, username + "-session-id");
    }

    private void mockAuthenticatedAs(final String username, final String sessionId) {
        final IRequestContext requestContext = mock(IRequestContext.class, withSettings().lenient());
        when(requestContext.getSessionId()).thenReturn(sessionId);
        when(requestManager.getRequestContext()).thenReturn(requestContext);
        when(requestContext.getAuthenticationContext()).thenReturn(Optional.of(authenticationContext));
        when(authenticationContext.getCurrentUser()).thenReturn(of(User.builder().username(username).build()));
    }

    private void mockStandaloneSetup() {
        reset(clusterServices[0]);
        reset(clusterServices[1]);

        // isClustered and isAvailable return false by default - marking the install as not-clustered
    }

    @ClusterableTask
    private static class ClusterMaintenanceTask extends LatchingMaintenanceTask {

        private ClusterMaintenanceTask(final String message, final int progress) {
            super(message, progress);
        }
    }

    private static class StandaloneMaintenanceTask extends LatchingMaintenanceTask {

        private StandaloneMaintenanceTask(final String message, final int progress) {
            super(message, progress);
        }
    }

    private abstract static class LatchingMaintenanceTask implements IRunnableTask {

        private final CountDownLatch latch = new CountDownLatch(1);

        private volatile boolean canceled;

        private final String message;

        private final int progress;

        private LatchingMaintenanceTask(final String message, final int progress) {
            this.message = message;
            this.progress = progress;
        }

        @Override
        public String getName() {
            return this.getClass().getSimpleName();
        }

        @Override
        public void cancel() {
            canceled = true;
            latch.countDown();
        }

        boolean isCanceled() {
            return canceled;
        }

        @Nonnull
        @Override
        public IProgress getProgress() {
            return new ProgressImpl(message, progress);
        }

        public void release() {
            latch.countDown();
        }

        @Override
        public void run() {
            try {
                latch.await();
                throwIfCanceled();
            } catch (final InterruptedException e) {
                throwIfCanceled();
                Throwables.throwIfUnchecked(e);
                throw new RuntimeException(e);
            }
        }

        private void throwIfCanceled() {
            if (canceled) {
                throw new CanceledBackupException(new KeyedMessage("", "", ""));
            }
        }
    }
}
