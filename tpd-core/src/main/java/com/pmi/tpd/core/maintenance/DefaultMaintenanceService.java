package com.pmi.tpd.core.maintenance;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import com.google.common.annotations.VisibleForTesting;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.spring.context.SpringAware;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exec.ICompletionCallback;
import com.pmi.tpd.api.exec.IRunnableTask;
import com.pmi.tpd.api.exec.TaskState;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.cluster.IClusterService;
import com.pmi.tpd.cluster.annotation.ClusterableTask;
import com.pmi.tpd.cluster.event.ClusterNodeAddedEvent;
import com.pmi.tpd.cluster.hazelcast.NodeIdMemberSelector;
import com.pmi.tpd.cluster.latch.LatchState;
import com.pmi.tpd.database.spi.IDatabaseManager;
import com.pmi.tpd.scheduler.exec.IRunnableTaskStatus;
import com.pmi.tpd.scheduler.exec.IncorrectTokenException;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.annotation.Unsecured;
import com.pmi.tpd.security.random.ISecureTokenGenerator;
import com.pmi.tpd.spring.context.AbstractSmartLifecycle;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.spi.IRequestContext;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class DefaultMaintenanceService extends AbstractSmartLifecycle implements IInternalMaintenanceService {

    /** */
    private static final Object LOCK_LOCK = new Object();

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMaintenanceService.class);

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final IExecutorService clusterExecutorService;

    /** */
    private final IAtomicReference<ClusterMaintenanceLock> clusterLock;

    /** */
    private final IClusterService clusterService;

    /** */
    private final IDatabaseManager databaseManager;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final ScheduledExecutorService executorService;

    /** */
    private final IEventAdvisorService<?> eventAdvisorService;

    /** */
    private final I18nService i18nService;

    /** */
    private final IAtomicLong isActive;

    /** */
    private final IMaintenanceTaskStatusSupplier latestTask;

    /** */
    private final IRequestManager requestManager;

    /** */
    private final IMaintenanceStatus status;

    /** */
    private final ISecureTokenGenerator tokenGenerator;

    /** */
    private long nodeJoinCheckDelayMillis;

    /** */
    private volatile DefaultMaintenanceLock nodeLock;

    /** */
    private volatile DefaultMaintenanceTaskMonitor runningTask;

    /**
     * @param authenticationContext
     * @param clusterExecutorService
     * @param clusterService
     * @param databaseManager
     * @param eventPublisher
     * @param executorService
     * @param eventAdvisorService
     * @param i18nService
     * @param requestManager
     * @param tokenGenerator
     * @param latestTask
     * @param isActive
     * @param clusterLock
     */
    @Inject
    public DefaultMaintenanceService(final IAuthenticationContext authenticationContext,
            final IExecutorService clusterExecutorService, final IClusterService clusterService,
            final IDatabaseManager databaseManager, final IEventPublisher eventPublisher,
            final ScheduledExecutorService executorService, final IEventAdvisorService<?> eventAdvisorService,
            final I18nService i18nService, final IRequestManager requestManager,
            final ISecureTokenGenerator tokenGenerator, final IMaintenanceTaskStatusSupplier latestTask,
            final IAtomicLong isActive, final IAtomicReference<ClusterMaintenanceLock> clusterLock) {
        this.authenticationContext = authenticationContext;
        this.clusterExecutorService = clusterExecutorService;
        this.clusterLock = clusterLock;
        this.clusterService = clusterService;
        this.databaseManager = databaseManager;
        this.eventPublisher = eventPublisher;
        this.executorService = executorService;
        this.eventAdvisorService = eventAdvisorService;
        this.i18nService = i18nService;
        this.isActive = isActive;
        this.latestTask = latestTask;
        this.requestManager = requestManager;
        this.tokenGenerator = tokenGenerator;

        nodeJoinCheckDelayMillis = TimeUnit.SECONDS.toMillis(10);
        status = new DefaultMaintenanceStatus();
    }

    @Override
    public void clearClusterLock() {
        clusterLock.clear();
    }

    /**
     *
     */
    @PreDestroy
    public void destroy() {
        final DefaultMaintenanceTaskMonitor runningTask = this.runningTask;
        if (runningTask != null) {
            LOGGER.warn("Cancelling task {} in response to shutdown", runningTask.getId());
            if (!runningTask.cancel(runningTask.getCancelToken(), 10, TimeUnit.SECONDS)) {
                LOGGER.warn("Timed out waiting for task {} to cancel in response to shutdown");
            }
        }
    }

    @Override
    @Unsecured("The lock must be available while Events is preventing authentication")
    public IMaintenanceLock getLock() {
        // Note: Because this is unsecured, unlocking the system will validate SYS_ADMIN permission itself
        final IMaintenanceLock lock = clusterLock.get();
        if (lock == null && nodeLock != null) {
            LOGGER.warn("The local node is locked for maintenance, but the cluster is not locked!");
            // return the local lock to allow the node to be unlocked
            return nodeLock;
        }
        return lock;
    }

    @Override
    @Unsecured("The lock must be available while Events is preventing authentication")
    public IMaintenanceLock getNodeLock() {
        // Note: Because this is unsecured, unlocking the system will validate SYS_ADMIN permission itself
        return nodeLock;
    }

    @Override
    public int getPhase() {
        return ApplicationConstants.LifeCycle.LIFECYCLE_PHASE_MAINTENANCE_SERVICE;
    }

    @Override
    @Unsecured("Retrieving the running task cannot be secured; the database may not be available")
    public ITaskMaintenanceMonitor getRunningTask() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("getRunningTask on node: {}", clusterService.getNodeId());
        }
        if (runningTask != null) {
            return runningTask;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("No task is running on the current node");
        }

        // no task is running on the current node, check if another node is running a maintenance task
        final IRunnableMaintenanceTaskStatus taskStatus = latestTask.get();
        if (taskStatus != null && taskStatus.getState() == TaskState.RUNNING) {
            return new RemoteMaintenanceTaskMonitor(taskStatus);
        }
        return null;
    }

    @Nonnull
    @Override
    @Unsecured("Retrieving the status cannot be secured; the database may not be available")
    public IMaintenanceStatus getStatus() {
        return status;
    }

    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public IMaintenanceLock lock() {
        final IUser user = authenticationContext.getCurrentUser()
                .orElseThrow(() -> new AuthorisationException(
                        i18nService.createKeyedMessage("app.service.maintenance.lock.anonymousnotallowed")));
        IMaintenanceLock currentLock = clusterLock.get();
        while (currentLock == null) {
            final ClusterMaintenanceLock newLock = new ClusterMaintenanceLock(user, tokenGenerator.generateToken(),
                    clusterExecutorService, i18nService, this);

            if (clusterLock.compareAndSet(null, newLock)) {
                newLock.lock();
                // if locking fails, newLock will unlock any node it succeeded in locking and clear the clusterLock
                return newLock;
            } else {
                // race condition: another node just locked the system for maintenance
                currentLock = clusterLock.get();
            }
        }

        LOGGER.warn("The system has already been locked for maintenance by {}",
            currentLock.getOwner().getDisplayName());

        throw new LockedMaintenanceException(i18nService.createKeyedMessage("app.service.maintenance.lock.locked"),
                currentLock.getOwner());
    }

    @Override
    @Unsecured("Only called from .lock > ClusterMaintenanceLock.lock _after_ SYS_ADMIN "
            + " permissions have been checked (possibly on another node)")
    public void lockNode(@Nonnull final IMaintenanceLock maintenanceLock) {
        synchronized (LOCK_LOCK) {
            if (nodeLock == null) {
                final DefaultMaintenanceLock lock = new DefaultMaintenanceLock(eventPublisher, i18nService,
                        eventAdvisorService, maintenanceLock.getOwner(), maintenanceLock.getUnlockToken());
                lock.addListener(() -> DefaultMaintenanceService.this.nodeLock = null);
                lock.lock();

                LOGGER.info("The system has been locked for maintenance. It may be unlocked with token: {}",
                    maintenanceLock.getUnlockToken());
                this.nodeLock = lock;
            } else {
                LOGGER.warn("The system has already been locked for maintenance by {}",
                    nodeLock.getOwner().getDisplayName());

                // only throw an exception if the unlock tokens don't match
                if (!nodeLock.getUnlockToken().equals(maintenanceLock.getUnlockToken())) {
                    throw new LockedMaintenanceException(
                            i18nService.createKeyedMessage("app.service.maintenance.lock.locked"), nodeLock.getOwner());
                }
            }
        }
    }

    @EventListener
    public void onNodeAdded(final ClusterNodeAddedEvent event) {
        maybeLockOnJoin();
    }

    @Override
    public void start() {
        super.start();
        // The MaintenanceService may not have been initialized when the node joined a cluster that was already locked.
        maybeLockOnJoin();
    }

    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('SYS_ADMIN')")
    public ITaskMaintenanceMonitor start(@Nonnull final IRunnableTask task, @Nonnull final MaintenanceType type) {
        Assert.checkNotNull(task, "task");
        if (clusterService.isClustered() && !task.getClass().isAnnotationPresent(ClusterableTask.class)) {
            throw new UnsupportedMaintenanceException(
                    i18nService.createKeyedMessage("app.service.maintenance.task.unsupportedincluster", type));
        }

        final IRequestContext requestContext = requestManager.getRequestContext();
        if (requestContext == null) {
            throw new IllegalStateException("Maintenance can only be started in the context of a user request, as "
                    + "performing maintenance may lock out the system and a user must have the ability to restore "
                    + "the system to a non-maintenance state");
        }

        if (isActive.compareAndSet(0L, 1L)) {
            try {
                final String cancelToken = tokenGenerator.generateToken();

                final DefaultMaintenanceTaskMonitor newTask = new DefaultMaintenanceTaskMonitor(task, cancelToken, type,
                        clusterService.getNodeId(), requestContext.getSessionId(), cancelToken, i18nService);
                newTask.registerCallback(() -> {
                    if (isActive.compareAndSet(1L, 0L)) {
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("Task {} is complete on node", runningTask.getId(), clusterService.getNodeId());
                        }
                        latestTask.set(new SimpleMaintenanceTaskStatus(runningTask));
                        runningTask = null;
                    }

                });

                runningTask = newTask;
                runningTask.submitTo(executorService);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{} started. It may be canceled with token: {}", type, cancelToken);
                }
            } catch (final RuntimeException e) {
                // maintenance task was not started, unmark isActive
                isActive.compareAndSet(1L, 0L);
                throw e;
            }
            if (runningTask != null) {
                latestTask.set(new SimpleMaintenanceTaskStatus(runningTask));
            }
            startMonitor();

            return runningTask;
        } else {
            throw new IllegalStateException(
                    type + " maintenance cannot be started; other maintenance is already in progress.");
        }
    }

    @VisibleForTesting
    public void setNodeJoinCheckDelayMillis(final long delay, final TimeUnit timeUnit) {
        nodeJoinCheckDelayMillis = timeUnit.toMillis(delay);
    }

    private boolean lockIfClusterLocked() {
        if (nodeLock == null) {
            final IMaintenanceLock lock = clusterLock.get();
            if (lock != null) {
                LOGGER.info("Locking system for maintenance because the cluster is already locked");
                lockNode(lock);
                return true;
            }
        }
        return false;
    }

    private void maybeLockOnJoin() {
        if (nodeLock == null && !lockIfClusterLocked()) {
            // There is a race condition where the cluster is switching into maintenance mode just as a node is joining
            // the cluster. The newly joined/joining node may not receive the IExecutor command that locks the node,
            // leaving the node unlocked while the rest of the cluster is locked. To prevent this race condition,
            // schedule a single check with a short delay to let any concurrent locking operation complete.
            executorService.schedule(this::lockIfClusterLocked, nodeJoinCheckDelayMillis, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Schedules a monitoring runnable that determines the progress of the currently running task and publishes it to
     * latestTask in order to make it visible to other cluster members. Note that the maintenance task only runs on a
     * single node and therefore it's dynamically calculated progress is only accessible on that node. For other nodes
     * to retrieve the current progress, the 'owning' node either needs to publish the progress to the cluster, or the
     * other nodes need to ask the 'owning' node for the current progress. We've gone with the publishing approach here,
     * because it leads to simpler code.
     */
    private void startMonitor() {
        executorService.schedule(new Runnable() {

            @Override
            public void run() {
                final ITaskMaintenanceMonitor monitor = runningTask;
                if (monitor != null) {
                    // 'publish' the current status to the cluster
                    latestTask.set(new SimpleMaintenanceTaskStatus(monitor));

                    // rerun in another second
                    executorService.schedule(this, 1, TimeUnit.SECONDS);
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * {@link Callable} to cancel the running maintenance task on the node that's running the maintenance task.
     */
    @SpringAware
    @VisibleForTesting
    static final class CancelMaintenance implements Callable<Boolean>, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** */
        private final String cancelToken;

        /** */
        private final long timeoutMillis;

        /** */
        private transient IMaintenanceService maintenanceService;

        private CancelMaintenance(final String cancelToken, final long timeout, final TimeUnit timeUnit) {
            this.cancelToken = cancelToken;
            this.timeoutMillis = timeUnit.toMillis(timeout);
        }

        @Override
        public Boolean call() throws Exception {
            final ITaskMaintenanceMonitor task = maintenanceService.getRunningTask();

            return task != null && task.cancel(cancelToken, timeoutMillis, TimeUnit.MILLISECONDS);
        }

        @Resource
        public void setMaintenanceService(final IMaintenanceService maintenanceService) {
            this.maintenanceService = maintenanceService;
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private class DefaultMaintenanceStatus implements IMaintenanceStatus {

        @Nonnull
        @Override
        public LatchState getDatabaseState() {
            return databaseManager.getState();
        }

        @Override
        public IRunnableTaskStatus getLatestTask() {
            return latestTask.get();
        }

    }

    /**
     * {@link MaintenanceTaskMonitor} stub for a maintenance task that's being executed remotely. This class supports
     * canceling a remotely running maintenance task.
     *
     * @author Christophe Friederich
     * @since 1.3
     */
    private final class RemoteMaintenanceTaskMonitor extends SimpleMaintenanceTaskStatus
            implements ITaskMaintenanceMonitor {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private RemoteMaintenanceTaskMonitor(final IRunnableMaintenanceTaskStatus taskInfo) {
            super(taskInfo);
        }

        @Override
        public void awaitCompletion() {
            throw new UnsupportedOperationException("Cannot await completion of remote tasks");
        }

        @Override
        public boolean cancel(@Nonnull final String token, final long timeout, @Nonnull final TimeUnit timeUnit) {
            Assert.checkNotNull(token, "token");
            Assert.checkNotNull(timeUnit, "unit");

            if (!getCancelToken().equals(token)) {
                throw new IncorrectTokenException(
                        i18nService.createKeyedMessage("app.service.maintenance.task.incorrecttoken"), token);
            }

            final Future<Boolean> result = clusterExecutorService.submit(
                new CancelMaintenance(token, timeout, timeUnit),
                new NodeIdMemberSelector(getOwnerNodeId()));

            try {
                return result.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt(); // retain
            } catch (final ExecutionException e) {
                LOGGER.warn("Error while canceling maintenance", e);
            }
            return false;
        }

        @Override
        public void registerCallback(final ICompletionCallback callback) {
            throw new UnsupportedOperationException("Cannot register callbacks on remote tasks");
        }
    }
}
