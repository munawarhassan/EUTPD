package com.pmi.tpd.api.scheduler.status;

import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;

/**
 * Indicates what the result was the last time that this job attempted to fire.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum RunOutcome {
    /**
     * The job ran {@link JobRunnerResponse#success() successfully}.
     */
    SUCCESS,

    /**
     * The job had no job runner registered when an attempt was made to fire it. This likely means that the job is left
     * over from an older version of the application or from an add-on that is no longer installed.
     * <p/>
     * <strong>Note</strong>: This outcome is only produced by the scheduler implementations; it cannot be returned in a
     * {@link JobRunnerResponse}.
     */
    UNAVAILABLE,

    /**
     * We did not start the job at the scheduled time because it was not ready. Some possible causes include:
     * <ul>
     * <li>No job details could be found for that job ID. It is possible that the job was deleted right as it was
     * scheduled to run.</li>
     * <li>The parameter map could not be reconstructed from its serialized form</li>
     * <li>The job has been registered with inconsistent run modes, probably with this node using
     * {@link RunMode#RUN_LOCALLY} and another node registering afterwards using {@link RunMode#RUN_ONCE_PER_CLUSTER}
     * </li>
     * <li>The job performed its own checks and explicitly returned a
     * {@link com.pmi.tpd.core.backup.scheduler.JobRunnerResponse} that indicated the job had been
     * {@link com.pmi.tpd.core.backup.scheduler.JobRunnerResponse#aborted(String) aborted}.</li>
     * </ul>
     */
    ABORTED,

    /**
     * The job was started but it either threw an exception or returned a
     * {@link com.pmi.tpd.core.backup.scheduler.JobRunnerResponse} that indicated the job had
     * {@link com.pmi.tpd.core.backup.scheduler.JobRunnerResponse#failed(String) failed}.
     */
    FAILED
}
