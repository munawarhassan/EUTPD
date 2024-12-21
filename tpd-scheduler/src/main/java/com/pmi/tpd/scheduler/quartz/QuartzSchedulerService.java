package com.pmi.tpd.scheduler.quartz;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.scheduler.quartz.QuartzSchedulerFacade.createClustered;
import static com.pmi.tpd.scheduler.quartz.QuartzSchedulerFacade.createLocal;
import static java.util.Collections.sort;
import static org.quartz.TriggerKey.triggerKey;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.tenant.ITenantAccessor;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.scheduler.AbstractSchedulerService;
import com.pmi.tpd.scheduler.JobLauncher;
import com.pmi.tpd.scheduler.quartz.spi.IQuartzSchedulerConfiguration;
import com.pmi.tpd.scheduler.spi.IRunDetailsDao;
import com.pmi.tpd.scheduler.spi.ISchedulerServiceConfiguration;

/**
 * Quartz 2.x implementation of a {@link SchedulerService}.
 * <p/>
 * <ul>
 * <li>Job runner keys are mapped to Quartz {@code JobKey} names, with the {@code Job} being created or destroyed
 * automatically based on whether or not it has any existing {@code Trigger}s.</li>
 * <li>All Quartz {@code Job}s use {@link QuartzJob}, which immediately delegates to {@link JobLauncher}.</li>
 * <li>Job ids are mapped to Quartz {@code TriggerKey} names.</li>
 * <li>The parameters map is serialized to a {@code byte[]} and stored in the {@code JobDataMap} for the Quartz
 * {@code Trigger}.</li>
 * </ul>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class QuartzSchedulerService extends AbstractSchedulerService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerService.class);

    /** */
    private final QuartzSchedulerFacade localJobs;

    /** */
    private final QuartzSchedulerFacade clusteredJobs;

    /** */
    private final QuartzTriggerFactory triggerFactory;

    /** */
    private final QuartzJobDetailsFactory jobDetailsFactory;

    /**
     * @param runDetailsDao
     * @param config
     * @param tenantAccessor
     * @throws SchedulerServiceException
     */
    public QuartzSchedulerService(@Nonnull final IRunDetailsDao runDetailsDao,
            @Nonnull final IQuartzSchedulerConfiguration config, final @Nonnull ITenantAccessor tenantAccessor)
            throws SchedulerServiceException {
        super(runDetailsDao, tenantAccessor);
        this.localJobs = createLocal(this, config);
        this.clusteredJobs = createClustered(this, config);
        this.triggerFactory = new QuartzTriggerFactory(config, getParameterMapSerializer());
        this.jobDetailsFactory = new QuartzJobDetailsFactory(this);
    }

    /**
     * @param runDetailsDao
     * @param config
     * @param localScheduler
     * @param clusteredScheduler
     * @param tenantAccessor
     * @throws SchedulerServiceException
     */
    public QuartzSchedulerService(@Nonnull final IRunDetailsDao runDetailsDao,
            final ISchedulerServiceConfiguration config, final @Nonnull Scheduler localScheduler,
            final @Nonnull Scheduler clusteredScheduler, @Nonnull final ITenantAccessor tenantAccessor)
            throws SchedulerServiceException {
        super(runDetailsDao, tenantAccessor);
        this.localJobs = createLocal(this, localScheduler);
        this.clusteredJobs = createClustered(this, clusteredScheduler);
        this.triggerFactory = new QuartzTriggerFactory(config, getParameterMapSerializer());
        this.jobDetailsFactory = new QuartzJobDetailsFactory(this);
    }

    @Override
    protected void doScheduleJob(final JobId jobId, final JobConfig jobConfig) throws SchedulerServiceException {
        try {
            Assert.notNull(jobConfig, "jobConfig");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("scheduleJob: {}: {}", jobId, jobConfig);
            }

            localJobs.unscheduleJob(jobId);
            clusteredJobs.unscheduleJob(jobId);

            final QuartzSchedulerFacade facade = getFacade(jobConfig.getRunMode());
            final JobRunnerKey jobRunnerKey = jobConfig.getJobRunnerKey();
            final TriggerBuilder<?> trigger = triggerFactory.buildTrigger(jobId, jobConfig);
            facade.scheduleJob(jobRunnerKey, trigger);
        } catch (final SchedulerRuntimeException sre) {
            throw checked(sre);
        }
    }

    @Override
    public void unscheduleJob(final JobId jobId) {
        // Deliberately avoiding a short-circuit; we want to remove from both
        boolean found = localJobs.unscheduleJob(jobId);
        found |= clusteredJobs.unscheduleJob(jobId);
        if (found) {
            LOGGER.debug("unscheduleJob: {}", jobId);
        } else {
            LOGGER.debug("unscheduleJob for non-existent jobId: {}", jobId);
        }
    }

    @Nullable
    @Override
    public Date calculateNextRunTime(final Schedule schedule) throws SchedulerServiceException {
        final Trigger trigger = triggerFactory.buildTrigger(schedule).withIdentity(triggerKey("name", "group")).build();
        return trigger.getFireTimeAfter(new Date());
    }

    @Nullable
    @Override
    public IJobDetails getJobDetails(final @Nonnull JobId jobId) {
        IJobDetails jobDetails = getJobDetails(clusteredJobs, jobId, RUN_ONCE_PER_CLUSTER);
        if (jobDetails == null) {
            jobDetails = getJobDetails(localJobs, jobId, RUN_LOCALLY);
        }
        return jobDetails;
    }

    @SuppressWarnings("null")
    @Nonnull
    @Override
    public Set<JobRunnerKey> getJobRunnerKeysForAllScheduledJobs() {
        final Set<JobRunnerKey> jobRunnerKeys = new HashSet<>(localJobs.getJobRunnerKeys());
        jobRunnerKeys.addAll(clusteredJobs.getJobRunnerKeys());
        return ImmutableSet.copyOf(jobRunnerKeys);
    }

    @Nullable
    private IJobDetails getJobDetails(final QuartzSchedulerFacade facade,
        @Nonnull final JobId jobId,
        @Nonnull final RunMode runMode) {
        final Trigger trigger = facade.getTrigger(jobId);
        return trigger != null ? jobDetailsFactory.buildJobDetails(jobId, trigger, runMode) : null;
    }

    @Nonnull
    @Override
    public List<IJobDetails> getJobsByJobRunnerKey(final @Nonnull JobRunnerKey jobRunnerKey) {
        final UniqueJobDetailsCollector collector = new UniqueJobDetailsCollector();
        collector.collect(RUN_ONCE_PER_CLUSTER, clusteredJobs.getTriggersOfJob(jobRunnerKey));
        collector.collect(RUN_LOCALLY, localJobs.getTriggersOfJob(jobRunnerKey));
        return collector.getResults();
    }

    /**
     * Start the threads associated with each quartz scheduler. It is the responsibility of the underlying JobStores to
     * determine whether or not to trigger jobs which should have run while the scheduler was in standby mode. This is
     * usually controlled by a misfire threshold on the JobStore implementation
     */
    @Override
    protected void startImpl() throws SchedulerServiceException {
        boolean abort = true;
        localJobs.start();
        try {
            clusteredJobs.start();
            abort = false;
        } finally {
            if (abort) {
                localJobs.standby();
            }
        }
    }

    /**
     * Stop the threads associated with each quartz scheduler
     */
    @Override
    protected void standbyImpl() throws SchedulerServiceException {
        try {
            localJobs.standby();
        } finally {
            clusteredJobs.standby();
        }
    }

    @Override
    protected void shutdownImpl() {
        try {
            localJobs.shutdown();
        } finally {
            clusteredJobs.shutdown();
        }
    }

    private QuartzSchedulerFacade getFacade(final RunMode runMode) {
        switch (Assert.notNull(runMode, "runMode")) {
            case RUN_LOCALLY:
                return localJobs;
            case RUN_ONCE_PER_CLUSTER:
                return clusteredJobs;
        }
        throw new IllegalArgumentException("runMode=" + runMode);
    }

    /**
     * Since it is possible for another node in the cluster to register the same jobId for running clustered when we had
     * it set to run locally, deal with this by pretending the local one is not there.
     */
    class UniqueJobDetailsCollector {

        /** */
        final Set<String> jobIdsSeen = newHashSet();

        /** */
        final List<IJobDetails> jobs = newArrayList();

        @SuppressWarnings("null")
        void collect(@Nonnull final RunMode runMode, final @Nonnull List<? extends Trigger> triggers) {
            for (final Trigger trigger : triggers) {
                final String jobId = trigger.getKey().getName();
                if (jobIdsSeen.add(jobId)) {
                    try {
                        jobs.add(jobDetailsFactory.buildJobDetails(JobId.of(jobId), trigger, runMode));
                    } catch (final SchedulerRuntimeException sre) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Unable to reconstruct log details for jobId '{}': {}", jobId, sre);
                        }
                    }
                }
            }
        }

        @SuppressWarnings("null")
        @Nonnull
        List<IJobDetails> getResults() {
            sort(jobs, new SortByJobId());
            return ImmutableList.copyOf(jobs);
        }
    }

    /**
     * @author Christophe Friederich
     */
    static class SortByJobId implements Comparator<IJobDetails>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(final IJobDetails jd1, final IJobDetails jd2) {
            return jd1.getJobId().compareTo(jd2.getJobId());
        }
    }
}
