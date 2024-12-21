package com.pmi.tpd.cluster;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.state;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.CONNECT;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_ANY_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_OTHER_NODE;
import static com.pmi.tpd.cluster.ClusterJoinCheckAction.PASSIVATE_THIS_NODE;
import static com.pmi.tpd.cluster.HazelcastDataUtils.readList;
import static com.pmi.tpd.cluster.HazelcastDataUtils.writeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.core.OrderComparator;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.context.IClock;
import com.pmi.tpd.api.util.Timer;
import com.pmi.tpd.api.util.TimerUtils;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class BaseClusterJoinManager implements IClusterJoinManager {

    /** */
    private final IClock clock;

    /** */
    private final Map<String, IClusterJoinCheck> joinChecks;

    /** */
    private final long startTime;

    @Inject
    public BaseClusterJoinManager(final IClock clock, final IClusterJoinCheck... joinChecks) {
        this.clock = clock;
        this.joinChecks = new LinkedHashMap<>(joinChecks.length); // to preserve order
        this.startTime = clock.nanoTime();

        final List<IClusterJoinCheck> orderedChecks = Lists.newArrayList(joinChecks);
        Collections.sort(orderedChecks, new OrderComparator());
        for (final IClusterJoinCheck joinCheck : joinChecks) {
            this.joinChecks.put(joinCheck.getName(), joinCheck);
        }
    }

    /**
     * The implementation of this method matches that of {@link #connect(IClusterJoinRequest)} but from the other side.
     *
     * @see #connect(IClusterJoinRequest)
     */
    @Override
    public void accept(@Nonnull final IClusterJoinRequest request) throws IOException {
        state(checkNotNull(request, "request").getJoinMode() == ClusterJoinMode.ACCEPT, "Expected accept request");

        // only continue if this node is accepting connections
        checkAcceptingClusterConnections();

        // run all join checks - tell the other side how many checks we've got
        request.out().writeInt(joinChecks.size());
        for (final IClusterJoinCheck joinCheck : joinChecks.values()) {
            try (Timer timer = TimerUtils.start("join check " + joinCheck.getName())) {
                // perform a 'handshake' for the check. Send the name of the check to the other
                // node, which then sends
                // back whether it has that check as well. If so, hand off control to the check.
                // If not, call onUnknown
                request.out().writeUTF(joinCheck.getName());
                ClusterJoinCheckResult outcome;
                if (request.in().readBoolean()) {
                    outcome = joinCheck.accept(request);
                } else {
                    outcome = joinCheck.onUnknown(request);
                }
                // both nodes have an outcome for the check - negotiate what to do next
                // (continue / disconnect /
                // passivate one of the nodes)
                negotiateOutcome(outcome, request);
            }
        }

        // if we've come this far, all join checks known to this node passed. The remote
        // node may have more join checks,
        // but it _knows_ that this node does not know them. The remote node will run
        // it's remaining checks (if any) and
        // will have a final outcome to communicate potential failure. This side will
        // assume that everything is fine.
        negotiateOutcome(ClusterJoinCheckResult.OK, request);
    }

    @Override
    public void connect(@Nonnull final IClusterJoinRequest request) throws IOException {
        state(checkNotNull(request, "request").getJoinMode() == ClusterJoinMode.CONNECT, "Expected connect request");

        // only continue if this node is accepting connections
        checkAcceptingClusterConnections();

        // keep track of the checks that haven't been processed yet
        final Set<String> toBeChecked = new HashSet<>(joinChecks.keySet());
        final int numberOfChecks = request.in().readInt();
        for (int i = 0; i < numberOfChecks; ++i) {
            final String checkName = request.in().readUTF();
            try (Timer timer = TimerUtils.start("join check - " + checkName)) {
                ClusterJoinCheckResult outcome;
                if (toBeChecked.remove(checkName)) {
                    // confirm that the check is known on this side and hand off control to the
                    // check
                    request.out().writeBoolean(true);
                    outcome = joinChecks.get(checkName).connect(request);
                } else {
                    // the requested check is not known on this node - the other node will flag it
                    // as an error if the
                    // check is required.
                    request.out().writeBoolean(false);
                    outcome = ClusterJoinCheckResult.OK;
                }
                // both nodes have an outcome for the check - negotiate what to do next
                // (continue / disconnect /
                // passivate one of the nodes)
                negotiateOutcome(outcome, request);
            }
        }

        // run any checks that the other side has not requested. At this point we know
        // that the other side does not have
        // these checks.
        for (final String checkName : toBeChecked) {
            final ClusterJoinCheckResult outcome = joinChecks.get(checkName).onUnknown(request);
            if (outcome.getAction() != CONNECT) {
                // abort on the first failed check
                negotiateOutcome(outcome, request);
            }
        }

        // if we reach here, all checks passed
        negotiateOutcome(ClusterJoinCheckResult.OK, request);
    }

    protected abstract boolean isSystemUnavailable();

    protected abstract void passivateNode(final List<String> issues);

    private void checkAcceptingClusterConnections() throws IOException {
        if (isSystemUnavailable()) {
            throw new NodeConnectionException("Not accepting cluster connections at the moment");
        }
    }

    private long getUptimeNanos() {
        return clock.nanoTime() - startTime;
    }

    private void negotiateOutcome(final ClusterJoinCheckResult result, final IClusterJoinRequest request)
            throws IOException {
        final ClusterJoinCheckAction localAction = result.getAction();
        request.out().writeInt(result.getAction().getId());
        if (localAction != CONNECT) {
            writeList(request.out(), result.getMessages());
        }

        final ClusterJoinCheckAction remoteAction = ClusterJoinCheckAction.forId(request.in().readInt());
        if (remoteAction == CONNECT && localAction == CONNECT) {
            return;
        }

        final List<String> errors = new ArrayList<>(result.getMessages());
        if (remoteAction != CONNECT) {
            errors.addAll(readList(request.in()));
        }

        final ClusterJoinCheckAction resolvedAction = resolveAction(localAction, remoteAction);
        if (resolvedAction == PASSIVATE_ANY_NODE) {
            // Apply the tie-breaker rule of choosing the smallest cluster or if the
            // clusters are of the same size,
            // the youngest node.

            // publish cluster size + uptime
            final int clusterSize = request.getHazelcast().getCluster().getMembers().size();
            final long uptime = getUptimeNanos();
            request.out().writeInt(clusterSize);
            request.out().writeLong(uptime);

            final long remoteClusterSize = request.in().readInt();
            final long remoteUptime = request.in().readLong();

            if (remoteClusterSize > clusterSize) {
                // clusters are of different size - passivate node from the smallest cluster
                passivateNode(errors);
            } else if (remoteClusterSize == clusterSize) {
                // clusters are of the same size - passivate the youngest node
                if (remoteUptime > uptime
                        || remoteUptime == uptime && request.getJoinMode() == ClusterJoinMode.CONNECT) {
                    passivateNode(errors);
                }
            }
        } else if (resolvedAction == PASSIVATE_THIS_NODE) {
            passivateNode(errors);
        }

        throw new NodeConnectionException(errors);
    }

    private ClusterJoinCheckAction resolveAction(final ClusterJoinCheckAction localAction,
        final ClusterJoinCheckAction remoteAction) {
        switch (remoteAction) {
            case CONNECT:
                return localAction;
            case DISCONNECT:
                // falling through intentionally
            case PASSIVATE_ANY_NODE:
                return localAction.isPassivate() ? localAction : remoteAction;
            case PASSIVATE_OTHER_NODE:
                // remote wants this node to passivate. Comply unless this node explicitly wants
                // the other node to
                // passivate, in which case we fall back to the tie breaker rule
                return localAction == PASSIVATE_OTHER_NODE ? PASSIVATE_ANY_NODE : PASSIVATE_THIS_NODE;
            case PASSIVATE_THIS_NODE:
                // remote want to passivate itself. Comply unless this nodes also wants to
                // passivate, in which case we
                // fall back to the tie breaker rule
                return localAction == PASSIVATE_THIS_NODE ? PASSIVATE_ANY_NODE : PASSIVATE_OTHER_NODE;
            default:
                break;
        }
        throw new IllegalArgumentException("Unsupported ClusterJoinAction " + remoteAction);
    }
}
