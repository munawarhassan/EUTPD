package com.pmi.tpd.core.cluster;

import static com.pmi.tpd.cluster.ClusterJoinCheckAction.CONNECT;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_OTHER_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_THIS_NODE;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.cluster.ClusterJoinCheckResult;
import com.pmi.tpd.cluster.IClusterJoinRequest;
import com.pmi.tpd.core.cluster.check.MaintenanceClusterJoinCheck;
import com.pmi.tpd.core.maintenance.IMaintenanceTaskStatusSupplier;
import com.pmi.tpd.core.maintenance.IRunnableMaintenanceTaskStatus;
import com.pmi.tpd.core.maintenance.ITaskMaintenanceMonitor;
import com.pmi.tpd.core.maintenance.LocalMaintenanceTaskStatusSupplier;

public class ClusterJoinCheckTest extends AbstractClusterJoinCheckTest {

    private IRunnableMaintenanceTaskStatus acceptingStatus;

    private IRunnableMaintenanceTaskStatus connectingStatus;

    @Mock
    private IClusterJoinRequest request;

    @Mock
    private ITaskMaintenanceMonitor runningTask;

    @Test
    public void testNoLatestTask() throws Exception {
        final CheckResult result = doJoin();

        assertEquals(CONNECT, result.acceptResult.getAction());
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testLatestTaskCompleteOnBoth() throws Exception {
        acceptingStatus = mockLatestTask("task-id", TaskState.SUCCESSFUL);
        connectingStatus = mockLatestTask("other-task-id", TaskState.SUCCESSFUL);

        final CheckResult result = doJoin();

        assertEquals(CONNECT, result.acceptResult.getAction());
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testAcceptingSideInMaintenancePassivatesConnectingWhenNoLatestTask() throws Exception {
        acceptingStatus = mockLatestTask("task-id", TaskState.RUNNING);

        final CheckResult result = doJoin();

        assertEquals(PASSIVATE_OTHER_NODE, result.acceptResult.getAction());
        assertEquals(PASSIVATE_THIS_NODE, result.connectResult.getAction());
    }

    @Test
    public void testAcceptingSideInMaintenancePassivatesConnectingWhenLatestTaskCompleted() throws Exception {
        acceptingStatus = mockLatestTask("task-id", TaskState.RUNNING);
        connectingStatus = mockLatestTask("other-task-id", TaskState.FAILED);

        final CheckResult result = doJoin();

        assertEquals(PASSIVATE_OTHER_NODE, result.acceptResult.getAction());
        assertEquals(PASSIVATE_THIS_NODE, result.connectResult.getAction());
    }

    @Test
    public void testConnectingSideInMaintenancePassivatesAcceptingWhenNoLatestTask() throws Exception {
        connectingStatus = mockLatestTask("task-id", TaskState.RUNNING);

        final CheckResult result = doJoin();

        assertEquals(PASSIVATE_THIS_NODE, result.acceptResult.getAction());
        assertEquals(PASSIVATE_OTHER_NODE, result.connectResult.getAction());
    }

    @Test
    public void testConnectingSideInMaintenancePassivatesAcceptingWhenLatestTaskCompleted() throws Exception {
        acceptingStatus = mockLatestTask("other-task-id", TaskState.FAILED);
        connectingStatus = mockLatestTask("task-id", TaskState.RUNNING);

        final CheckResult result = doJoin();

        assertEquals(PASSIVATE_THIS_NODE, result.acceptResult.getAction());
        assertEquals(PASSIVATE_OTHER_NODE, result.connectResult.getAction());
    }

    @Test
    public void testOnUnknown() throws Exception {
        assertEquals(ClusterJoinCheckResult.OK,
            createCheck(acceptingStatus).onUnknown(mock(IClusterJoinRequest.class)));
    }

    @Test
    public void testSplitBrainResolutionAcceptingCompleted() throws Exception {
        acceptingStatus = mockLatestTask("task-id", TaskState.SUCCESSFUL);
        connectingStatus = mockLatestTask("task-id", TaskState.RUNNING);

        final CheckResult result = doJoin();

        assertEquals(CONNECT, result.acceptResult.getAction());
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testSplitBrainResolutionConnectingFailed() throws Exception {
        acceptingStatus = mockLatestTask("task-id", TaskState.SUCCESSFUL);
        connectingStatus = mockLatestTask("task-id", TaskState.RUNNING);

        final CheckResult result = doJoin();

        assertEquals(CONNECT, result.acceptResult.getAction());
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    @Test
    public void testSplitBrainResolutionStillRunning() throws Exception {
        acceptingStatus = mockLatestTask("task-id", TaskState.RUNNING);
        connectingStatus = mockLatestTask("task-id", TaskState.RUNNING);

        final CheckResult result = doJoin();

        assertEquals(CONNECT, result.acceptResult.getAction());
        assertEquals(CONNECT, result.connectResult.getAction());
    }

    private MaintenanceClusterJoinCheck createCheck(final IRunnableMaintenanceTaskStatus status) {
        final IMaintenanceTaskStatusSupplier maintenanceTaskStatusSupplier = new LocalMaintenanceTaskStatusSupplier();
        if (status != null) {
            maintenanceTaskStatusSupplier.set(status);
        }
        return new MaintenanceClusterJoinCheck(maintenanceTaskStatusSupplier);
    }

    private CheckResult doJoin() throws InterruptedException, ExecutionException, IOException {
        return executeJoinCheck(createCheck(connectingStatus), createCheck(acceptingStatus));
    }

    private IRunnableMaintenanceTaskStatus mockLatestTask(final String taskId, final TaskState state) {
        final IRunnableMaintenanceTaskStatus latestTask = mock(IRunnableMaintenanceTaskStatus.class);

        when(latestTask.getId()).thenReturn(taskId);
        when(latestTask.getState()).thenReturn(state);

        return latestTask;
    }
}
