package com.pmi.tpd.api.scheduler;

import com.pmi.tpd.api.scheduler.config.JobRunnerKey;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class JobRunnerNotRegisteredException extends SchedulerServiceException {

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private final JobRunnerKey jobRunnerKey;

    /**
     * @param jobRunnerKey
     */
    public JobRunnerNotRegisteredException(final JobRunnerKey jobRunnerKey) {
        super("No job runner registered for job runner key '" + jobRunnerKey + '\'');
        this.jobRunnerKey = jobRunnerKey;
    }

    /**
     * @return
     */
    public JobRunnerKey getJobRunnerKey() {
        return jobRunnerKey;
    }
}
