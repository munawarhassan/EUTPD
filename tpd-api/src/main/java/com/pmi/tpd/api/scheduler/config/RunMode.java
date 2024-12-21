package com.pmi.tpd.api.scheduler.config;

/**
 * Represents how a Job will be run by the scheduler.
 * <p/>
 * This mostly defines how a job will run in a clustered environment; however, it also affects whether or not the job
 * will survive a restart of the underlying application.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public enum RunMode {
    /**
     * The job is scheduled such that it will only run on one node of the cluster each time that it triggers.
     * <p/>
     * Although jobs scheduled with this run mode must still register the {@code JobRunner} for the job on each restart,
     * the job's schedule will persist across restarts.
     */
    RUN_ONCE_PER_CLUSTER,

    /**
     * The job is scheduled such that it will apply only to this particular node of the cluster.
     * <p/>
     * This job will not be persisted, and the job must be recreated if the application is restarted.
     */
    RUN_LOCALLY
}
