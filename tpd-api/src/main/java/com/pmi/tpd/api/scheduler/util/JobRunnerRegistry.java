package com.pmi.tpd.api.scheduler.util;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;

/**
 * Encapsulates the registration and retrieval of job runners.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class JobRunnerRegistry {

    /** */
    private final ConcurrentMap<JobRunnerKey, IJobRunner> jobRunnerRegistry = Maps.newConcurrentMap();

    public void registerJobRunner(@Nonnull final JobRunnerKey jobRunnerKey, @Nonnull final IJobRunner jobRunner) {
        jobRunnerRegistry.put(checkNotNull(jobRunnerKey, "jobRunnerKey"), checkNotNull(jobRunner, "jobRunner"));
    }

    public void unregisterJobRunner(@Nonnull final JobRunnerKey jobRunnerKey) {
        jobRunnerRegistry.remove(checkNotNull(jobRunnerKey, "jobRunnerKey"));
    }

    @Nullable
    public IJobRunner getJobRunner(@Nonnull final JobRunnerKey jobRunnerKey) {
        return jobRunnerRegistry.get(checkNotNull(jobRunnerKey, "jobRunnerKey"));
    }

    @SuppressWarnings("null")
    @Nonnull
    public Set<JobRunnerKey> getRegisteredJobRunnerKeys() {
        return ImmutableSet.copyOf(jobRunnerRegistry.keySet());
    }
}
