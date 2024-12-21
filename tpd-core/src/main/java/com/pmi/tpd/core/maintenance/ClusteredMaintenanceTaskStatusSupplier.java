package com.pmi.tpd.core.maintenance;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.hazelcast.topic.ITopic;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.cluster.IClusterNode;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;

/**
 * A clustered implementation of {@link IMaintenanceTaskStatusSupplier} which uses an {@link ITopic} to publish the
 * latest status to all nodes in the cluster.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class ClusteredMaintenanceTaskStatusSupplier implements IMaintenanceTaskStatusSupplier {

    /** */
    private final IMaintenanceTaskStatusSupplier delegate;

    /** */
    private final ITopic<IRunnableMaintenanceTaskStatus> topic;

    /** */
    private volatile UUID listenerId;

    /**
     * @param delegate
     * @param topic
     */
    public ClusteredMaintenanceTaskStatusSupplier(final IMaintenanceTaskStatusSupplier delegate,
            final ITopic<IRunnableMaintenanceTaskStatus> topic) {
        this.delegate = delegate;
        this.topic = topic;
    }

    /**
     *
     */
    @PostConstruct
    public void addListener() {
        listenerId = topic.addMessageListener(message -> {
            if (!message.getPublishingMember().localMember()) {
                delegate.set(message.getMessageObject());
            }
        });
    }

    /**
     *
     */
    @PreDestroy
    public void removeListener() {
        final UUID listenerId = this.listenerId;
        if (listenerId != null) {
            topic.removeMessageListener(listenerId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRunnableMaintenanceTaskStatus get() {
        return delegate.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(@Nonnull final IRunnableMaintenanceTaskStatus status) {
        delegate.set(status);
        topic.publish(status);
    }

    /**
     * @param event
     */
    @EventListener
    public void onNodeAdded(final ClusterNodeAddedEvent event) {
        final IRunnableMaintenanceTaskStatus status = delegate.get();
        if (status == null) {
            return;
        }

        // To avoid concurrency issues and an update storms, only have the
        // node owning the latest status update the other nodes
        boolean publishStatus = true;
        for (final IClusterNode node : event.getCurrentNodes()) {
            if (status.getOwnerNodeId().equals(node.getId())) {
                if (!node.isLocal()) {
                    // Optimisation, the owner node will publish it for us
                    publishStatus = false;
                }
                break;
            }
        }
        if (publishStatus) {
            topic.publish(status);
        }
    }

}
