package com.pmi.tpd.core.maintenance;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.spring.context.SpringAware;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.cluster.hazelcast.NodeIdMemberSelector;
import com.pmi.tpd.cluster.latch.ResultCollectingExecutionCallback;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;

/**
 * {@link IMaintenanceLock} implementation that locks and unlocks all nodes in a cluster for maintenance.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@SpringAware
public class ClusterMaintenanceLock implements IMaintenanceLock, Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterMaintenanceLock.class);

    /** services that will be injected by Hazelcast when the instance is deserialized. */
    private transient IExecutorService executorService;

    /** */
    private transient I18nService i18nService;

    /** */
    private transient IInternalMaintenanceService maintenanceService;

    /** */
    private final IUser owner;

    /** */
    private final String token;

    public ClusterMaintenanceLock(@Nonnull final IUser owner, @Nonnull final String token,
            final IExecutorService executorService, final I18nService i18nService,
            final IInternalMaintenanceService maintenanceService) {
        this.executorService = executorService;
        this.i18nService = i18nService;
        this.maintenanceService = maintenanceService;
        this.owner = checkNotNull(owner, "owner");
        this.token = checkNotNull(token, "token");
    }

    public void lock() {
        final FailureTrackingCallback callback = new FailureTrackingCallback();
        try {
            // lock all the nodes in the cluster
            executorService.submitToAllMembers(new LockTask(this), callback);
            callback.await(1, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        } finally {
            // if one of the nodes failed to lock, unlock them all
            if (!callback.isSuccess()) {
                unlock(token);
                final String failedNodeIds = StringUtils.join(callback.getFailedNodes(), ", ");
                throw new LockFailedMaintenanceException(
                        i18nService.createKeyedMessage("app.service.maintenance.lock.failed", failedNodeIds));
            }
        }
    }

    @Nonnull
    @Override
    public IUser getOwner() {
        return owner;
    }

    @Nonnull
    @Override
    public String getUnlockToken() {
        return token;
    }

    @Autowired
    public void setExecutorService(final IExecutorService executorService) {
        this.executorService = executorService;
    }

    @Autowired
    public void setI18nService(final I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Autowired
    public void setMaintenanceService(final IInternalMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Override
    public void unlock(@Nonnull final String token) {
        if (checkNotNull(token, "token").equals(this.token)) {
            final FailureTrackingCallback callback = new FailureTrackingCallback();
            executorService.submitToAllMembers(new UnlockTask(token), callback);
            try {
                if (!callback.await(1, TimeUnit.MINUTES)) {
                    LOGGER.info("Timed out waiting for all nodes to unlock");
                }
                if (!callback.isSuccess()) {
                    LOGGER.info("Failed to unlock some nodes: {}. Retrying",
                        StringUtils.join(callback.getFailedNodes(), ", "));
                    executorService.submitToMembers(new UnlockTask(token),
                        new NodeIdMemberSelector(callback.getFailedNodes()));
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt(); // retain interrupted flag
                LOGGER.info("Interrupted while waiting for nodes to unlock");
            }
            maintenanceService.clearClusterLock();
        } else {
            LOGGER.warn("An invalid token ({}) was supplied to attempt to unlock the system", token);
            throw new IncorrectTokenException(
                    i18nService.createKeyedMessage("app.service.maintenance.lock.incorrectunlocktoken"), token);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ClusterMaintenanceLock that = (ClusterMaintenanceLock) o;

        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        return token.hashCode();
    }

    private static class FailureTrackingCallback extends ResultCollectingExecutionCallback<Void> {

        private final Set<UUID> failedNodes = Sets.newHashSet();

        public Set<UUID> getFailedNodes() {
            return failedNodes;
        }

        @Override
        protected void onError(final Member member, final Throwable throwable) {
            failedNodes.add(member.getUuid());
        }
    }

    /**
     * Locks a node for maintenance.
     */
    @SpringAware
    private static final class LockTask implements Runnable, Serializable {

        /** */
        private static final long serialVersionUID = 1L;

        /** */
        private final IMaintenanceLock lockInfo;

        /** */
        private transient IInternalMaintenanceService maintenanceService;

        private LockTask(final IMaintenanceLock lockInfo) {
            this.lockInfo = lockInfo;
        }

        @Override
        public void run() {
            maintenanceService.lockNode(lockInfo);
        }

        @Autowired
        public void setMaintenanceService(final IInternalMaintenanceService maintenanceService) {
            this.maintenanceService = maintenanceService;
        }
    }

    /**
     * Unlocks a node from maintenance mode.
     */
    @SpringAware
    private static final class UnlockTask implements Callable<Void>, Serializable {

        private static final long serialVersionUID = 1L;

        private transient IInternalMaintenanceService maintenanceService;

        private final String unlockToken;

        private UnlockTask(final String unlockToken) {
            this.unlockToken = unlockToken;
        }

        @Override
        public Void call() {
            final IMaintenanceLock lock = maintenanceService.getNodeLock();
            if (lock != null) {
                lock.unlock(unlockToken);
            }
            return null;
        }

        @Autowired
        public void setMaintenanceService(final IInternalMaintenanceService maintenanceService) {
            this.maintenanceService = maintenanceService;
        }
    }
}
