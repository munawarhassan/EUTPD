package com.pmi.tpd.cluster;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.spring.context.SpringAware;
import com.pmi.tpd.api.event.advisor.IEventAdvisorService;
import com.pmi.tpd.cluster.hazelcast.NodeIdMemberSelector;
import com.pmi.tpd.cluster.latch.ILatch;
import com.pmi.tpd.cluster.latch.ILatchableService;
import com.pmi.tpd.cluster.latch.LatchMode;
import com.pmi.tpd.cluster.latch.ResultCollectingExecutionCallback;
import com.pmi.tpd.cluster.spi.IUnlatchFailedEventTask;

/**
 * Base implementation of {@link ILatch latches} that support both local and cluster-wide latching and draining.
 * Subclasses need only implement {@link #acquireLocally()}, {@link #drainLocally(long, TimeUnit, boolean)} and
 * {@link #unlatchLocally()}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class BaseClusterableLatch implements ILatch {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClusterableLatch.class);

    /** */
    private final Set<UUID> drainedMembers;

    /** */
    private volatile boolean acquired;

    /** */
    private volatile boolean drained;

    /** */
    private volatile boolean drainedLocally;

    /** */
    private volatile String id;

    /** */
    private volatile boolean unlatched;

    /** */
    protected final Cluster cluster;

    /** */
    protected final IExecutorService executor;

    /** */
    protected final IEventAdvisorService<?> eventAdvisorService;

    /** */
    protected final String latchServiceBeanName;

    /** */
    protected final LatchMode mode;

    /** */
    protected final Object lock;

    /**
     * @param mode
     * @param cluster
     * @param executor
     * @param latchServiceBeanName
     * @param eventService
     */
    protected BaseClusterableLatch(final LatchMode mode, final Cluster cluster, final IExecutorService executor,
            final IEventAdvisorService<?> eventAdvisorService, final String latchServiceBeanName) {
        this.cluster = cluster;
        this.executor = executor;
        this.latchServiceBeanName = latchServiceBeanName;
        this.eventAdvisorService = eventAdvisorService;
        this.mode = mode;

        drainedMembers = new CopyOnWriteArraySet<>();
        lock = new Object();
    }

    // this method is _not_ part of the Latch interface. LatchableServices are
    // expected to call this method straight
    // after construction to acquire the latch either locally or in the cluster
    public void acquire(@Nullable final String latchId) {
        if (acquired && id != null && id.equals(latchId)) {
            // already acquired under the requested id
            return;
        }

        state(!acquired, "Latch has already been acquired");

        final String newId = latchId == null ? UUID.randomUUID().toString() : latchId;
        // acquire the latch locally
        synchronized (lock) {
            state(!acquired, "Latch has already been acquired");
            acquireLocally();

            id = newId;
            acquired = true;
        }

        if (mode == LatchMode.CLUSTER && latchId == null) {
            // no latchId was provided, so this is the node initiating a cluster latch. The
            // local node is already
            // latched,
            // now latch the other nodes
            final Collection<Member> remoteMembers = getRemoteMembers();
            if (!remoteMembers.isEmpty()) {
                final AcquireCallback callback = new AcquireCallback();
                executor.submitToMembers(new AcquireLatchTask(latchServiceBeanName, newId), remoteMembers, callback);

                try {
                    callback.await(2, TimeUnit.MINUTES);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warn("Interrupted while waiting for latch to be acquired in the cluster");
                }
                if (callback.getLatchedMembers().size() != remoteMembers.size()) {
                    // some nodes failed to latch - unlatch all latched nodes
                    unlatchLocally();
                    unlatchOrPassivateNodes(callback.getLatchedMembers());

                    throw new IllegalStateException("Failed to acquire the latch on all nodes in the cluster");
                }
            }
        }
    }

    @Override
    public boolean drain(final long timeout, @Nonnull final TimeUnit timeUnit) {
        return drain(timeout, timeUnit, false);
    }

    @Override
    public boolean forceDrain(final long timeout, @Nonnull final TimeUnit timeUnit) {
        return drain(timeout, timeUnit, true);
    }

    @Nonnull
    @Override
    public LatchMode getMode() {
        return mode;
    }

    public boolean isAcquired() {
        return acquired;
    }

    public void onNodeJoined(final IClusterNode node) {
        if (acquired && !unlatched) {
            // latch the newly added node as well
            executor.submitToMembers(new AcquireLatchTask(latchServiceBeanName, id),
                new NodeIdMemberSelector(node.getId()));
        }
    }

    @Override
    public void unlatch() {
        state(!unlatched, "This latch is no longer active");

        if (mode == LatchMode.LOCAL) {
            doUnlatchLocally();
        } else {
            unlatchOrPassivateNodes(null);
        }
    }

    /** */
    protected abstract void acquireLocally();

    /** */
    protected abstract boolean drainLocally(long timeout, @Nonnull TimeUnit timeUnit, boolean force);

    /** */
    protected abstract void unlatchLocally();

    protected abstract IUnlatchFailedEventTask createUnlatchFailedEventTask();

    private boolean doDrainLocally(final long timeout, final TimeUnit timeUnit, final boolean force) {
        if (!drainedLocally) {
            drainedLocally |= drainLocally(timeout, timeUnit, force);
        }
        return drainedLocally;
    }

    private void doUnlatchLocally() {
        if (unlatched) {
            return;
        }

        synchronized (lock) {
            if (unlatched) {
                return;
            }

            unlatchLocally();
            unlatched = true;
        }
    }

    private boolean drain(final long timeout, @Nonnull final TimeUnit timeUnit, final boolean force) {
        state(acquired, "Latch has not been acquired yet");

        if (unlatched) {
            LOGGER.debug("This latch is no longer active");
            return false;
        }
        if (drained) {
            return true;
        }

        if (mode == LatchMode.LOCAL) {
            drained |= doDrainLocally(timeout, timeUnit, force);
        } else {
            // drain *all* the nodes
            final Collection<Member> nonDrained = getNonDrainedMembers();
            if (!nonDrained.isEmpty()) {
                final DrainCallback callback = new DrainCallback();
                executor.submitToMembers(new DrainTask(latchServiceBeanName, id, timeout, timeUnit, force),
                    nonDrained,
                    callback);

                try {
                    // give the callback a bit of extra time to account for possible latency
                    callback.await(500 + timeUnit.toMillis(timeout), TimeUnit.MILLISECONDS);
                    drained = callback.isDrained();
                } catch (final InterruptedException e) {
                    // restore the interrupted flag
                    Thread.currentThread().interrupt();
                }
            }
        }
        return drained;
    }

    private Collection<Member> getNonDrainedMembers() {
        return from(cluster.getMembers()).filter(member -> !drainedMembers.contains(member.getUuid())).toList();
    }

    private Collection<Member> getRemoteMembers() {
        return from(cluster.getMembers()).filter(member -> !member.localMember()).toList();
    }

    private void unlatchOrPassivateNodes(final Collection<Member> members) {
        if (members != null && members.isEmpty()) {
            // nothing to do
            return;
        }

        final UnlatchCallback callback = new UnlatchCallback();
        final UnlatchTask unlatchTask = new UnlatchTask(latchServiceBeanName, id);
        if (members != null) {
            executor.submitToMembers(unlatchTask, members, callback);
        } else {
            executor.submitToAllMembers(unlatchTask, callback);
        }

        try {
            callback.await(1, TimeUnit.MINUTES);

            if (callback.isSuccess()) {
                LOGGER.debug("Cluster unlatch completed successfully");
            } else {
                LOGGER.warn("Failed to unlatch some cluster members: {}", callback.errorMembers);
                executor.executeOnMembers(createUnlatchFailedEventTask(), callback.errorMembers);
            }
        } catch (final InterruptedException e) {
            // restore interrupted flag
            Thread.currentThread().interrupt();
            LOGGER.warn(
                "Interrupted while waiting for latch to be released on other cluster nodes. Not all nodes may "
                        + "have unlatched",
                e);
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static class AcquireCallback extends ResultCollectingExecutionCallback<Object> {

        /** */
        private final Set<Member> latchedMembers = new CopyOnWriteArraySet<>();

        public Set<Member> getLatchedMembers() {
            return ImmutableSet.copyOf(latchedMembers);
        }

        @Override
        protected void onSuccess(final Member member, final Object value) {
            latchedMembers.add(member);
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    private static final class AcquireLatchTask extends ClusterableLatchTask implements Callable<Void> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** */
        private final String latchId;

        private AcquireLatchTask(final String latchableServiceBeanName, final String latchId) {
            super(latchableServiceBeanName, latchId);
            this.latchId = latchId;
        }

        @Override
        public Void call() throws Exception {
            state(service != null, "LatchableService %s was not injected", beanName);
            service.acquireLatch(LatchMode.CLUSTER, latchId);
            return null;
        }
    }

    private class DrainCallback extends ResultCollectingExecutionCallback<Boolean> {

        private final AtomicBoolean drained = new AtomicBoolean(true);

        public boolean isDrained() {
            return drained.get();
        }

        @Override
        protected void onError(final Member member, final Throwable throwable) {
            drained.set(false);
        }

        @Override
        protected void onSuccess(final Member member, final Boolean value) {
            if (Boolean.TRUE.equals(value)) {
                drainedMembers.add(member.getUuid());
                LOGGER.debug("Node {} drained successfully", member.getUuid());
            } else {
                drained.set(false);
                LOGGER.debug("Node {} did not drain: {}", member.getUuid(), value != null ? value.toString() : "");
            }
        }
    }

    /**
     * @author Christophe Friederich
     */
    private static class DrainTask extends ClusterableLatchTask implements Callable<Boolean> {

        /** */
        private static final long serialVersionUID = 2L;

        /** */
        private final long timeoutMs;

        /** */
        private final boolean force;

        /**
         * @param latchServiceBeanName
         * @param latchId
         * @param timeout
         * @param timeUnit
         * @param force
         */
        public DrainTask(final String latchServiceBeanName, final String latchId, final long timeout,
                final TimeUnit timeUnit, final boolean force) {
            super(latchServiceBeanName, latchId);
            this.force = force;
            timeoutMs = timeUnit.toMillis(timeout);
        }

        @Override
        public Boolean call() throws Exception {
            if (latch == null) {
                // This can happen, for example, if we receive a viewMaintenanceStatus request
                // (which calls drain with
                // zero timeout to retrieve the drained state) before we have latched the
                // service. Just return false.
                return false;
            }
            state(latchId.equals(
                latch.id), "Unexpected latch for %s: expected latch ID %s but got %s", beanName, latchId, latch.id);

            return latch.doDrainLocally(timeoutMs, TimeUnit.MILLISECONDS, force);
        }
    }

    /**
     * @author Christophe Friederich
     */
    private static class UnlatchCallback extends ResultCollectingExecutionCallback<Void> {

        protected final Set<Member> errorMembers = new CopyOnWriteArraySet<>();

        @Override
        protected void onError(final Member member, final Throwable throwable) {
            errorMembers.add(member);
        }
    }

    /**
     * @author Christophe Friederich
     */
    private static class UnlatchTask extends ClusterableLatchTask implements Runnable {

        private static final long serialVersionUID = 1L;

        public UnlatchTask(final String latchServiceBeanName, final String latchId) {
            super(latchServiceBeanName, latchId);
        }

        @Override
        public void run() {
            state(latch != null, "Latch for %s was not injected", beanName);
            state(latchId.equals(
                latch.id), "Unexpected latch for %s: expected latch ID %s but got %s", beanName, latchId, latch.id);

            latch.doUnlatchLocally();
        }
    }

    /**
     * @author Christophe Friederich
     * @since 1.3
     */
    @SpringAware
    protected abstract static class ClusterableLatchTask implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /** */
        protected final String beanName;

        /** */
        protected final String latchId;

        /** */
        protected transient ILatchableService<?> service;

        /** */
        protected transient BaseClusterableLatch latch;

        protected ClusterableLatchTask(final String beanName, final String latchId) {
            this.beanName = checkNotNull(beanName, "beanName");
            this.latchId = checkNotNull(latchId, "latchId");
        }

        @Inject
        public void setApplicationContext(final ApplicationContext applicationContext) {
            // Look up the LatchableService by it's beanName, then retrieve the latch.
            try {
                service = applicationContext.getBean(beanName, ILatchableService.class);
                final ILatch l = service.getCurrentLatch();
                if (l != null) {
                    state(l instanceof BaseClusterableLatch, "Latch %s is not a ClusterableLatch", l);
                    latch = (BaseClusterableLatch) l;
                    state(Objects.equals(latchId, latch.id),
                        "An unexpected latch was found. Expected %s, found %s",
                        latchId,
                        latch.id);
                }
            } catch (final BeansException e) {
                LOGGER.error("Latchable service '{}' not found - cannot drain the cluster latch", beanName, e);
                throw new IllegalStateException(e);
            }
        }
    }

}
