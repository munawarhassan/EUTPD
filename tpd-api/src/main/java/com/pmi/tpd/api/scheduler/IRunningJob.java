package com.pmi.tpd.api.scheduler;

/**
 * Represents a particular instance of a running job.
 * <p>
 * This adds management capabilities to a job runner request.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IRunningJob extends IJobRunnerRequest {

    /**
     * Requests that the job be cancelled.
     * <p>
     * Calling this method changes the return value of {@link #isCancellationRequested()} permanently to {@code true}.
     * It has no other effect; in particular, it does not interrupt the thread that is running the job. It is the
     * responsibility of the job runner implementation to cooperatively check for an respond to the
     * {@link #isCancellationRequested()} flag.
     * </p>
     */
    void cancel();
}
