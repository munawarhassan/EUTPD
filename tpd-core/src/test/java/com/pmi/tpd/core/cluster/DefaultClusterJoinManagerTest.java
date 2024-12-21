package com.pmi.tpd.core.cluster;

import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_ANY_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_OTHER_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_THIS_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckResult.OK;
import static com.pmi.tpd.cluster.ClusterJoinCheckResult.disconnect;
import static com.pmi.tpd.cluster.ClusterJoinCheckResult.passivate;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.ArgumentMatchers.isA;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.impl.DefaultSerializationServiceBuilder;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.event.advisor.EventLevel;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.advisor.IEventContainer;
import com.pmi.tpd.api.event.advisor.event.Event;
import com.pmi.tpd.cluster.ClusterJoinCheckResult;
import com.pmi.tpd.cluster.ClusterJoinMode;
import com.pmi.tpd.cluster.IClusterJoinCheck;
import com.pmi.tpd.cluster.IClusterJoinManager;
import com.pmi.tpd.cluster.IClusterJoinRequest;
import com.pmi.tpd.cluster.NodeConnectionException;
import com.pmi.tpd.core.event.advisor.EventAdvisorService;
import com.pmi.tpd.core.event.advisor.servlet.ServletEventAdvisor;
import com.pmi.tpd.core.event.advisor.support.DefaultEventContainer;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

public class DefaultClusterJoinManagerTest extends MockitoTestCase {

    private PipedClusterJoinRequest acceptRequest;

    private PipedClusterJoinRequest connectRequest;

    @Mock
    private IClock clock;

    @Mock
    private ServletContext servletContext;

    private IEventContainer acceptContainer;

    @Mock(lenient = true)
    private ServletContext acceptServletContext;

    private IEventContainer connectContainer;

    @Mock(lenient = true)
    private ServletContext connectServletContext;

    private AtomicInteger acceptClusterSize;

    private AtomicInteger connectClusterSize;

    private static IEventAdvisorService<?> eventAdvisorService;

    @BeforeAll
    public static void setupEvent() throws IOException {
        eventAdvisorService = EventAdvisorService
                .initialize(getResourceAsStream(DefaultClusterJoinManagerTest.class, "minimal-event-config.xml"));
    }

    @AfterAll
    public static void tearDownEvent() {
        eventAdvisorService.terminate();
    }

    @BeforeEach
    public void setUp() throws Exception {
        acceptClusterSize = new AtomicInteger(2);
        connectClusterSize = new AtomicInteger(2);
        connectRequest = new PipedClusterJoinRequest(mockCluster(connectClusterSize), ClusterJoinMode.CONNECT);
        acceptRequest = new PipedClusterJoinRequest(mockCluster(acceptClusterSize),
                new DefaultSerializationServiceBuilder().build(), connectRequest);

        acceptContainer = new DefaultEventContainer();
        when(acceptServletContext.getAttribute(ServletEventAdvisor.ATTR_EVENT_CONTAINER)).thenReturn(acceptContainer);

        connectContainer = new DefaultEventContainer();
        when(connectServletContext.getAttribute(ServletEventAdvisor.ATTR_EVENT_CONTAINER)).thenReturn(connectContainer);

    }

    @AfterEach
    public void tearDown() throws Exception {
        final IEventContainer eventContainer = eventAdvisorService.getEventContainer();
        for (final Event event : eventContainer.getEvents()) {
            eventContainer.discardEvent(event);
        }
    }

    @Test
    public void testAcceptWhenEvented() throws IOException {
        assertThrows(NodeConnectionException.class, () -> {
            addEvent(EventLevel.FATAL);

            new DefaultClusterJoinManager(clock, eventAdvisorService).accept(acceptRequest);
        });
    }

    @Test
    public void testConnectWhenEvented() throws IOException {
        assertThrows(NodeConnectionException.class, () -> {
            addEvent(EventLevel.FATAL);

            new DefaultClusterJoinManager(clock, eventAdvisorService).connect(connectRequest);
        });
    }

    @Test
    public void testClusterJoin() throws IOException, InterruptedException {
        // test of the happy path
        final IClusterJoinCheck check = mockClusterJoinCheck("check", 1, OK, OK, OK);

        final ClusterJoinResult result = executeClusterJoin(
            new DefaultClusterJoinManager(clock, eventAdvisorService, check),
            new DefaultClusterJoinManager(clock, eventAdvisorService, check));

        assertSucceeded(result);
    }

    @Test
    public void testClusterJoinAcceptFails() throws IOException, InterruptedException {
        final IClusterJoinCheck check = mockClusterJoinCheck("check",
            1,
            ClusterJoinCheckResult.disconnect("accept fail"),
            OK,
            OK);

        final ClusterJoinResult result = executeClusterJoin(
            new DefaultClusterJoinManager(clock, eventAdvisorService, check),
            new DefaultClusterJoinManager(clock, eventAdvisorService, check));

        assertFailed(result, "accept fail");
    }

    @Test
    public void testClusterJoinConnectFails() throws IOException, InterruptedException {
        final IClusterJoinCheck check = mockClusterJoinCheck("check",
            1,
            OK,
            ClusterJoinCheckResult.disconnect("connect fail"),
            OK);

        final ClusterJoinResult result = executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(servletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(servletContext), check));

        assertFailed(result, "connect fail");
    }

    @Test
    public void testMissingCheckAcceptingSide() throws Exception {
        final IClusterJoinCheck check1 = mockClusterJoinCheck("check1", 1, OK, OK, disconnect("check 1 missing"));
        final IClusterJoinCheck check2 = mockClusterJoinCheck("check2", 2, OK, OK, disconnect("check 2 missing"));

        final ClusterJoinResult result = executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(servletContext), check1, check2),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(servletContext), check1));

        assertFailed(result, "check 2 missing");
    }

    @Test
    public void testMissingCheckConnectingSide() throws Exception {
        final IClusterJoinCheck check1 = mockClusterJoinCheck("check1", 1, OK, OK, disconnect("check 1 missing"));
        final IClusterJoinCheck check2 = mockClusterJoinCheck("check2", 2, OK, OK, disconnect("check 2 missing"));

        final ClusterJoinResult result = executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(servletContext), check1),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(servletContext), check1, check2));

        assertFailed(result, "check 2 missing");
    }

    @Test
    public void testPassivateSmallestClusterOnLocalFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_ANY_NODE, "accept failed"),
            OK,
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        connectClusterSize.set(3);
        acceptClusterSize.set(10);
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the node from the smaller cluster is evented
        assertEquals(0, acceptContainer.getEvents().size());
        assertEquals(1, connectContainer.getEvents().size());
    }

    @Test
    public void testPassivateSmallestClusterOnRemoteFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_ANY_NODE, "accept failed"),
            OK,
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        connectClusterSize.set(10);
        acceptClusterSize.set(3);
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the node from the smaller cluster is evented
        assertEquals(1, acceptContainer.getEvents().size());
        assertEquals(0, connectContainer.getEvents().size());
    }

    @Test
    public void testPassivateThisNodeOnAcceptFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_THIS_NODE, "passivate the accepting node"),
            passivate(PASSIVATE_ANY_NODE, "connect failed"),
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the accepting node passivated
        assertEquals(1, acceptContainer.getEvents().size());
        assertEquals(0, connectContainer.getEvents().size());
    }

    @Test
    public void testPassivateThisNodeOnConnectFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_ANY_NODE, "accept failed"),
            passivate(PASSIVATE_THIS_NODE, "passivate connecting node"),
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the connecting node passivated
        assertEquals(0, acceptContainer.getEvents().size());
        assertEquals(1, connectContainer.getEvents().size());
    }

    @Test
    public void testPassivateOtherNodeOnAcceptFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_OTHER_NODE, "accepting node passivates OTHER"),
            passivate(PASSIVATE_ANY_NODE, "connect failed"),
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the connecting node passivated
        assertEquals(0, acceptContainer.getEvents().size());
        assertEquals(1, connectContainer.getEvents().size());
    }

    @Test
    public void testPassivateOtherNodeOnConnectFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_ANY_NODE, "accept failed"),
            passivate(PASSIVATE_OTHER_NODE, "connecting node passivates OTHER"),
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the accepting node passivated
        assertEquals(1, acceptContainer.getEvents().size());
        assertEquals(0, connectContainer.getEvents().size());
    }

    @Test
    public void testTieBreakerAppliesWhenBothPassivateThisNode() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_THIS_NODE, "accepting node passivates THIS"),
            passivate(PASSIVATE_THIS_NODE, "connecting node passivates THIS"),
            OK);

        connectClusterSize.set(3);
        acceptClusterSize.set(10);
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the node from the smaller cluster is evented
        assertEquals(0, acceptContainer.getEvents().size());
        assertEquals(1, connectContainer.getEvents().size());
    }

    @Test
    public void testTieBreakerAppliesWhenBothPassivateOtherNode() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            passivate(PASSIVATE_OTHER_NODE, "accepting node passivates OTHER"),
            passivate(PASSIVATE_OTHER_NODE, "connecting node passivates OTHER"),
            OK);

        connectClusterSize.set(3);
        acceptClusterSize.set(10);
        executeClusterJoin(
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(connectServletContext), check),
            new DefaultClusterJoinManager(clock, EventAdvisorService.getInstance(acceptServletContext), check));

        // verify that the node from the smaller cluster is evented
        assertEquals(0, acceptContainer.getEvents().size());
        assertEquals(1, connectContainer.getEvents().size());
    }

    @Test
    public void testPassivateYoungestNodeOnLocalFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            OK,
            passivate(PASSIVATE_ANY_NODE, "connect failed"),
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        final IClock olderClock = mock(IClock.class);
        when(olderClock.nanoTime()).thenReturn(0L, 10000L);

        final IClock youngerClock = mock(IClock.class);
        when(youngerClock.nanoTime()).thenReturn(0L, 5000L);

        final DefaultClusterJoinManager olderManager = new DefaultClusterJoinManager(olderClock,
                EventAdvisorService.getInstance(acceptServletContext), check);
        final DefaultClusterJoinManager youngerManager = new DefaultClusterJoinManager(youngerClock,
                EventAdvisorService.getInstance(connectServletContext), check);

        executeClusterJoin(youngerManager, olderManager);

        // verify that the younger node is evented
        assertEquals(1, connectContainer.getEvents().size());
        assertEquals(0, acceptContainer.getEvents().size());
    }

    @Test
    public void testPassivateYoungestNodeOnRemoteFailure() throws Exception {
        final IClusterJoinCheck check = mockClusterJoinCheck("passivate",
            1,
            OK,
            passivate(PASSIVATE_ANY_NODE, "connect failed"),
            OK);

        // create two different join managers with separate EventContainers so we can determine which node gets
        // evented
        final IClock olderClock = mock(IClock.class);
        when(olderClock.nanoTime()).thenReturn(0L, 10000L);

        final IClock youngerClock = mock(IClock.class);
        when(youngerClock.nanoTime()).thenReturn(0L, 5000L);

        final DefaultClusterJoinManager olderManager = new DefaultClusterJoinManager(olderClock,
                EventAdvisorService.getInstance(connectServletContext), check);
        final DefaultClusterJoinManager youngerManager = new DefaultClusterJoinManager(youngerClock,
                EventAdvisorService.getInstance(acceptServletContext), check);

        // run the cluster join
        executeClusterJoin(olderManager, youngerManager);

        // verify that the younger node is evented
        assertEquals(1, acceptContainer.getEvents().size());
        assertEquals(0, connectContainer.getEvents().size());
    }

    protected void assertFailed(final ClusterJoinResult result, final String... errors) {
        assertThat("Client join should fail", result.getClientError(), instanceOf(NodeConnectionException.class));
        assertThat("Server join should fail", result.getServerError(), instanceOf(NodeConnectionException.class));

        final List<String> expectedErrors = Lists.newArrayList(errors);
        final NodeConnectionException clientException = (NodeConnectionException) result.getClientError();
        final NodeConnectionException serverException = (NodeConnectionException) result.getServerError();

        assertEquals(expectedErrors, clientException.getIssues(), "Client errors don't match");
        assertEquals(expectedErrors, serverException.getIssues(), "Server errors don't match");
    }

    protected void assertSucceeded(final ClusterJoinResult result) {
        assertNull(result.getClientError(), "Client join should succeed");
        assertNull(result.getServerError(), "Server join should succeed");
    }

    protected ClusterJoinResult executeClusterJoin(final IClusterJoinManager connectingManager,
        final IClusterJoinManager acceptingManager) throws IOException, InterruptedException {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            final List<Future<Void>> futures = executorService.invokeAll(Arrays.asList(() -> {
                try {
                    connectingManager.connect(connectRequest);
                } finally {
                    Closeables.close(connectRequest, true);
                }

                return null;
            }, () -> {
                try {
                    acceptingManager.accept(acceptRequest);
                } finally {
                    Closeables.close(acceptRequest, true);
                }

                return null;
            }), 10, TimeUnit.SECONDS);

            Closeables.close(connectRequest, true);
            Closeables.close(acceptRequest, true);

            return new ClusterJoinResult(getJoinResult(futures.get(0)), getJoinResult(futures.get(1)));
        } finally {
            executorService.shutdown();
        }
    }

    private void addEvent(final String level) {
        EventAdvisorService.getInstance()
                .getEventContainer()
                .publishEvent(
                    new Event(EventAdvisorService.getInstance().getEventType("database-unavailable").orElse(null),
                            "description", EventAdvisorService.getInstance().getEventLevel(level).orElse(null)));
    }

    private Throwable getJoinResult(final Future<?> future) {
        try {
            future.get();
            return null;
        } catch (InterruptedException | RuntimeException e) {
            return e;
        } catch (final ExecutionException e) {
            return e.getCause() == null ? e : e.getCause();
        }
    }

    private IClusterJoinCheck mockClusterJoinCheck(final String name,
        final int order,
        final ClusterJoinCheckResult acceptResult,
        final ClusterJoinCheckResult connectResult,
        final ClusterJoinCheckResult unknownResult) throws IOException {
        final IClusterJoinCheck check = mock(IClusterJoinCheck.class, withSettings().lenient());
        when(check.getName()).thenReturn(name);
        when(check.getOrder()).thenReturn(order);
        when(check.accept(isA(IClusterJoinRequest.class))).thenReturn(acceptResult);
        when(check.connect(isA(IClusterJoinRequest.class))).thenReturn(connectResult);
        when(check.onUnknown(isA(IClusterJoinRequest.class))).thenReturn(unknownResult);
        return check;
    }

    @SuppressWarnings("unchecked")
    private HazelcastInstance mockCluster(final AtomicInteger size) {
        final HazelcastInstance instance = mock(HazelcastInstance.class, withSettings().lenient());
        final Cluster cluster = mock(Cluster.class, withSettings().lenient());
        final Set<Member> members = mock(Set.class, withSettings().lenient());
        when(instance.getCluster()).thenReturn(cluster);
        when(cluster.getMembers()).thenReturn(members);
        when(members.size()).thenAnswer(invocation -> size.get());
        return instance;
    }

    public static class ClusterJoinResult {

        private final Throwable clientError;

        private final Throwable serverError;

        public ClusterJoinResult(final Throwable clientError, final Throwable serverError) {
            this.clientError = clientError;
            this.serverError = serverError;
        }

        public Throwable getClientError() {
            return clientError;
        }

        public Throwable getServerError() {
            return serverError;
        }
    }
}
