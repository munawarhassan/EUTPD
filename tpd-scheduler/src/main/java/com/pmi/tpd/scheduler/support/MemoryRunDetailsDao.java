package com.pmi.tpd.scheduler.support;

import static java.util.concurrent.TimeUnit.DAYS;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.status.IRunDetails;
import com.pmi.tpd.api.scheduler.status.RunOutcome;
import com.pmi.tpd.scheduler.spi.IRunDetailsDao;

/**
 * An implementation of the {@code RunDetailsDao} that keeps the scheduler history in memory. This implementation does
 * not share information across nodes in the cluster or persist it across restarts, but provides an otherwise functional
 * implementation. By default, each job's history is expired 30 days after it is last updated.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class MemoryRunDetailsDao implements IRunDetailsDao {

    /** */
    private final Cache<JobId, JobRecord> store;

    /**
     *
     */
    public MemoryRunDetailsDao() {
        this(30);
    }

    /**
     * @param daysToKeepIdleHistory
     */
    public MemoryRunDetailsDao(final int daysToKeepIdleHistory) {
        this.store = CacheBuilder.newBuilder().expireAfterWrite(daysToKeepIdleHistory, DAYS).<JobId, JobRecord> build();
    }

    @Override
    public IRunDetails getLastRunForJob(final JobId jobId) {
        final JobRecord record = store.getIfPresent(jobId);
        return record != null ? record.lastRun : null;
    }

    @Override
    public IRunDetails getLastSuccessfulRunForJob(final JobId jobId) {
        final JobRecord record = store.getIfPresent(jobId);
        return record != null ? record.lastRun : null;
    }

    @Override
    public void addRunDetails(final JobId jobId, final IRunDetails runDetails) {
        // Concurrency note: while we won't get concurrent modification exceptions or anything like
        // that, little effort is made here to keep this information consistent if multiple instances
        // of the same JobId attempt to record run details at once. This is not supposed to happen,
        // anyway, so it should not be important.
        final JobRecord record;
        if (runDetails.getRunOutcome() == RunOutcome.SUCCESS) {
            record = new JobRecord(runDetails, runDetails);
        } else {
            record = new JobRecord(runDetails, getLastSuccessfulRunForJob(jobId));
        }
        store.put(jobId, record);
    }

    /**
     * @author Christophe Friederich
     */
    static class JobRecord {

        /** */
        private final IRunDetails lastRun;

        /** */
        @SuppressWarnings("unused")
        private final IRunDetails lastSuccessfulRun;

        JobRecord(final IRunDetails lastRun, final IRunDetails lastSuccessfulRun) {
            this.lastRun = lastRun;
            this.lastSuccessfulRun = lastSuccessfulRun;
        }
    }
}
