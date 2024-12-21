package com.pmi.tpd.core.cluster.check;

import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_OTHER_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_THIS_NODE;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.cluster.ClusterJoinCheckResult;
import com.pmi.tpd.cluster.IClusterJoinCheck;
import com.pmi.tpd.cluster.IClusterJoinRequest;
import com.pmi.tpd.cluster.IClusterJoinRequirement;
import com.pmi.tpd.core.maintenance.IMaintenanceTaskStatusSupplier;
import com.pmi.tpd.core.maintenance.IRunnableMaintenanceTaskStatus;

/**
 * A {@link IClusterJoinRequirement} which requires that the server node is not currently performing a backup or
 * database migration.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class MaintenanceClusterJoinCheck implements IClusterJoinCheck {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceClusterJoinCheck.class);

    /** */
    private static final String MAINTENANCE_MESSAGE = "The node is currently performing a maintenance task";

    /** */
    private final IMaintenanceTaskStatusSupplier statusHolder;

    /**
     * Note, the {@code localLatestTaskHolder} must be used instead of the clustered version to avoid a cyclic
     * dependency between Hazelcast and the {@link IMaintenanceTaskStatusSupplier} implementation.
     *
     * @param statusHolder
     */
    @Inject
    public MaintenanceClusterJoinCheck(
            @Qualifier("localMaintenanceTaskStatusSupplier") final IMaintenanceTaskStatusSupplier statusHolder) {
        this.statusHolder = statusHolder;
    }

    @Nonnull
    @Override
    public String getName() {
        return "migrationStatus";
    }

    @Nonnull
    @Override
    public ClusterJoinCheckResult accept(@Nonnull final IClusterJoinRequest request) throws IOException {
        return checkMaintenanceTasks(request);
    }

    @Nonnull
    @Override
    public ClusterJoinCheckResult connect(@Nonnull final IClusterJoinRequest request) throws IOException {
        return checkMaintenanceTasks(request);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Nonnull
    @Override
    public ClusterJoinCheckResult onUnknown(@Nonnull final IClusterJoinRequest request) {
        return ClusterJoinCheckResult.OK;
    }

    private ClusterJoinCheckResult checkMaintenanceTasks(final IClusterJoinRequest request) throws IOException {
        final IRunnableMaintenanceTaskStatus latestTask = statusHolder.get();
        String otherId = null;
        TaskState otherState = null;

        final ObjectDataInput in = request.in();
        final ObjectDataOutput out = request.out();

        if (latestTask == null) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            out.writeUTF(latestTask.getId());
            out.writeObject(latestTask.getState());
        }

        // read what maintenance task the other node is performing, if any
        if (in.readBoolean()) {
            otherId = in.readUTF();
            otherState = in.readObject();
        }

        if (latestTask == null) {
            if (otherState == TaskState.RUNNING) {
                // this node is not performing maintenance, but the other node is
                return ClusterJoinCheckResult.passivate(PASSIVATE_THIS_NODE, MAINTENANCE_MESSAGE);
            }
        } else if (latestTask.getId().equals(otherId)) {
            // both nodes are already performing / have performed the same maintenance task.
            // We're most likely
            // recovering
            // from a network partition. Let the nodes rejoin.
            if (latestTask.getState() != otherState) {
                LOGGER.info(
                    "Local node reports {} maintenance task in state {} while the remote node reports state {}. "
                            + "Recovering from a network partition?",
                    latestTask.getType(),
                    latestTask.getState(),
                    otherState);
            }
            return ClusterJoinCheckResult.OK;
        } else if (latestTask.getState() == TaskState.RUNNING) {
            // this node is performing maintenance, but the other node is not or is
            // performing a different maintenance
            // task (task IDs don't match up)
            return ClusterJoinCheckResult.passivate(PASSIVATE_OTHER_NODE, MAINTENANCE_MESSAGE);
        } else if (otherState == TaskState.RUNNING) {
            // this node is not running maintenance, but the other is
            return ClusterJoinCheckResult.passivate(PASSIVATE_THIS_NODE, MAINTENANCE_MESSAGE);
        }

        // neither node is currently performing maintenance
        return ClusterJoinCheckResult.OK;
    }
}
