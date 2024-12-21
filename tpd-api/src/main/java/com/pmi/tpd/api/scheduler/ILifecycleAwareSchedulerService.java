package com.pmi.tpd.api.scheduler;

/**
 * An interface that scheduler service implementations will generally be expected to implement so that they can be
 * stopped from accepting jobs when the application is not at a point in its lifecycle where it should be running them.
 * For example, if the plugin system has not finished coming up, then this node's scheduler should not be claiming jobs,
 * because it would steal clustered jobs that another node in the cluster would be able to process when we know we are
 * unlikely to be ready.
 * <p>
 * As a general rule, scheduler implementations should begin in {@link #standby()} mode, where they will not run any
 * jobs until explicitly {@link #start() started}. Once started, the scheduler may be placed back into {@code standby}
 * mode and restarted as often as needed. The {@link #shutdown()} method should only be called when the
 * {@code SchedulerService} is being disposed and will not be asked to {@code start()} again.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILifecycleAwareSchedulerService extends ISchedulerService, ISchedulerServiceController {

    /**
     * A representation of the scheduler's current state.
     */
    enum State {
        STANDBY,
        STARTED,
        SHUTDOWN
    }
}
