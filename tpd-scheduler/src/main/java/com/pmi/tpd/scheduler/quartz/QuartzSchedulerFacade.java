package com.pmi.tpd.scheduler.quartz;

import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.jobGroupEquals;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Suppliers;
import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.scheduler.AbstractSchedulerService;
import com.pmi.tpd.scheduler.quartz.spi.IQuartzSchedulerConfiguration;

import io.atlassian.util.concurrent.LazyReference;

/**
 * Wraps the quartz scheduler to make it work a bit closer to how we need it to. Specifically, this hides Quartz's
 * checked exceptions and provides the association between our jobId/jobRunnerKey and the trigger and group names.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
class QuartzSchedulerFacade {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerFacade.class);

    /** */
    static final String QUARTZ_JOB_GROUP = "SchedulerServiceJobs";

    /** */
    static final String QUARTZ_TRIGGER_GROUP = "SchedulerServiceTriggers";

    /** */
    static final String QUARTZ_PARAMETERS_KEY = "parameters";

    /** */
    private final Supplier<Scheduler> quartzRef;

    private QuartzSchedulerFacade(final Supplier<Scheduler> quartzRef) {
        this.quartzRef = quartzRef;
    }

    /**
     * Creates the local scheduler facade by wrapping a supplied Scheduler instance.
     */
    static QuartzSchedulerFacade createLocal(final @Nonnull AbstractSchedulerService schedulerService,
        @Nonnull final Scheduler scheduler) throws SchedulerServiceException {
        checkNotNull(scheduler, "scheduler");
        return createFacade(schedulerService, scheduler, RUN_LOCALLY);
    }

    /**
     * Creates the clustered scheduler facade by wrapping a supplied Scheduler instance.
     */
    static QuartzSchedulerFacade createClustered(final @Nonnull AbstractSchedulerService schedulerService,
        @Nonnull final Scheduler scheduler) throws SchedulerServiceException {
        checkNotNull(scheduler, "scheduler");
        return createFacade(schedulerService, scheduler, RUN_ONCE_PER_CLUSTER);
    }

    /**
     * Creates a schedule scheduler facade by wrapping a supplied scheduler instance.
     */
    private static QuartzSchedulerFacade createFacade(final @Nonnull AbstractSchedulerService schedulerService,
        final @Nonnull Scheduler scheduler,
        final @Nonnull RunMode runMode) throws SchedulerServiceException {
        try {
            configureScheduler(scheduler, schedulerService, runMode);
            final Supplier<Scheduler> quartzRef = Suppliers.ofInstance(scheduler);
            return new QuartzSchedulerFacade(quartzRef);
        } catch (final SchedulerException se) {
            throw checked("Unable to configure the underlying Quartz scheduler", se);
        }
    }

    /**
     * Creates the local scheduler facade lazily using the supplied configuration.
     */
    static QuartzSchedulerFacade createLocal(@Nonnull final AbstractSchedulerService schedulerService,
        @Nonnull final IQuartzSchedulerConfiguration config) throws SchedulerServiceException {
        checkNotNull(config, "config");
        final Properties localSettings = checkNotNull(config.getLocalSettings(), "config.getLocalSettings()");
        return createFacade(schedulerService, localSettings, RUN_LOCALLY);
    }

    /**
     * Creates the clustered scheduler facade lazily using the supplied configuration.
     */
    static QuartzSchedulerFacade createClustered(final AbstractSchedulerService schedulerService,
        @Nonnull final IQuartzSchedulerConfiguration config) throws SchedulerServiceException {
        checkNotNull(config, "config");
        final Properties clusteredSettings = checkNotNull(config.getClusteredSettings(),
            "config.getClusteredSettings()");
        return createFacade(schedulerService, clusteredSettings, RUN_ONCE_PER_CLUSTER);
    }

    /**
     * Creates a schedule scheduler facade lazily using the supplied configuration.
     */
    private static QuartzSchedulerFacade createFacade(final AbstractSchedulerService schedulerService,
        final Properties customConfig,
        final RunMode runMode) throws SchedulerServiceException {
        try {
            final Properties config = new Properties();
            for (final String key : customConfig.stringPropertyNames()) {
                config.setProperty(key, customConfig.getProperty(key));
            }

            // JRA-23747 -- never allow Quartz to run its update check
            config.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");

            final StdSchedulerFactory schedulerFactory = new StdSchedulerFactory(config);
            final Supplier<Scheduler> quartzRef = createQuartzRef(schedulerService, runMode, schedulerFactory);
            return new QuartzSchedulerFacade(quartzRef);
        } catch (final SchedulerException se) {
            throw checked("Unable to create the underlying Quartz scheduler", se);
        }
    }

    private static Supplier<Scheduler> createQuartzRef(final AbstractSchedulerService schedulerService,
        final RunMode runMode,
        final StdSchedulerFactory schedulerFactory) {
        return new LazyReference<>() {

            @Override
            protected Scheduler create() throws Exception {
                // SCHEDULER-11: Quartz cares about the thread's CCL. This makes sure that
                // the class loader that was used to construct the Quartz1SchedulerService
                // is set as the thread's CCL at the time the scheduler is lazily constructed.
                // otherwise, if a plugin is given direct access to Quartz scheduler before
                // the application initializes it, the plugin bundle's classloader could be
                // set as the thread's CCL, with unpleasant results like HOT-6239.
                final Thread thd = Thread.currentThread();
                final ClassLoader originalContextClassLoader = thd.getContextClassLoader();
                try {
                    thd.setContextClassLoader(schedulerService.getClass().getClassLoader());
                    final Scheduler quartz = schedulerFactory.getScheduler();
                    configureScheduler(quartz, schedulerService, runMode);
                    return quartz;
                } finally {
                    thd.setContextClassLoader(originalContextClassLoader);
                }
            }
        };
    }

    static void configureScheduler(final @Nonnull Scheduler quartz,
        final @Nonnull AbstractSchedulerService schedulerService,
        final @Nonnull RunMode runMode) throws SchedulerException {
        quartz.setJobFactory(new QuartzJobFactory(schedulerService, runMode));
    }

    @Nullable
    Trigger getTrigger(final @Nonnull JobId jobId) {
        try {
            return getScheduler().getTrigger(jobId(jobId));
        } catch (final SchedulerException se) {
            logWarn("Error getting quartz trigger for '{}'", jobId, se);
            return null;
        }
    }

    @Nullable
    JobDetail getQuartzJob(final @Nonnull JobRunnerKey jobRunnerKey) {
        try {
            return getScheduler().getJobDetail(jobRunnerKey(jobRunnerKey));
        } catch (final SchedulerException se) {
            logWarn("Error getting quartz job details for '{}'", jobRunnerKey, se);
            return null;
        }
    }

    boolean hasAnyTriggers(final @Nonnull JobRunnerKey jobRunnerKey) {
        return !getTriggersOfJob(jobRunnerKey).isEmpty();
    }

    Collection<JobRunnerKey> getJobRunnerKeys() {
        try {
            final Set<JobKey> jobKeys = getScheduler().getJobKeys(jobGroupEquals(QUARTZ_JOB_GROUP));
            final Set<JobRunnerKey> jobRunnerKeys = new HashSet<>((int) (jobKeys.size() * 1.25));
            for (final JobKey jobKey : jobKeys) {
                jobRunnerKeys.add(JobRunnerKey.of(jobKey.getName()));
            }
            return jobRunnerKeys;
        } catch (final SchedulerException se) {
            throw unchecked("Could not get the triggers from Quartz", se);
        }
    }

    @SuppressWarnings("null")
    @Nonnull
    List<? extends Trigger> getTriggersOfJob(final @Nonnull JobRunnerKey jobRunnerKey) {
        try {
            return getScheduler().getTriggersOfJob(jobRunnerKey(jobRunnerKey));
        } catch (final SchedulerException se) {
            throw unchecked("Could not get the triggers from Quartz", se);
        }
    }

    boolean deleteTrigger(final JobId jobId) {
        try {
            return getScheduler().unscheduleJob(jobId(jobId));
        } catch (final SchedulerException se) {
            logWarn("Error removing Quartz trigger for '{}'", jobId, se);
            return false;
        }
    }

    boolean deleteJob(final @Nonnull JobRunnerKey jobRunnerKey) {
        try {
            return getScheduler().deleteJob(jobRunnerKey(jobRunnerKey));
        } catch (final SchedulerException se) {
            logWarn("Error removing Quartz job for '{}'", jobRunnerKey, se);
            return false;
        }
    }

    private void scheduleJob(final TriggerBuilder<?> trigger) throws SchedulerServiceException {
        try {
            getScheduler().scheduleJob(trigger.build());
        } catch (final SchedulerException se) {
            throw checked("Unable to create the Quartz trigger", se);
        }
    }

    void scheduleJob(final @Nonnull JobRunnerKey jobRunnerKey, final TriggerBuilder<?> trigger)
            throws SchedulerServiceException {
        if (getQuartzJob(jobRunnerKey) != null) {
            scheduleJob(trigger.forJob(jobRunnerKey(jobRunnerKey)));
            return;
        }

        try {
            final JobDetail quartzJob = JobBuilder.newJob()
                    .withIdentity(jobRunnerKey(jobRunnerKey))
                    .ofType(QuartzJob.class)
                    .storeDurably(false)
                    .build();
            getScheduler().scheduleJob(quartzJob, trigger.build());
        } catch (final SchedulerException se) {
            throw checked("Unable to create the Quartz job and trigger", se);
        }
    }

    boolean unscheduleJob(final @Nonnull JobId jobId) {
        final Trigger trigger = getTrigger(jobId);
        if (trigger == null) {
            return false;
        }
        final JobRunnerKey jobRunnerKey = JobRunnerKey.of(trigger.getJobKey().getName());
        if (deleteTrigger(jobId) && !hasAnyTriggers(jobRunnerKey)) {
            deleteJob(jobRunnerKey);
        }
        return true;
    }

    void start() throws SchedulerServiceException {
        try {
            getScheduler().start();
        } catch (final SchedulerException se) {
            throw checked("Quartz scheduler refused to start", se);
        }
    }

    void standby() throws SchedulerServiceException {
        try {
            getScheduler().standby();
        } catch (final SchedulerException se) {
            throw checked("Quartz scheduler refused to enter standby mode", se);
        }
    }

    void shutdown() {
        try {
            getScheduler().shutdown();
        } catch (final SchedulerException se) {
            LOGGER.error("Error shutting down internal scheduler", se);
        }
    }

    private Scheduler getScheduler() {
        try {
            return quartzRef.get();
        } catch (final LazyReference.InitializationException ex) {
            throw unchecked("Error creating underlying Quartz scheduler", ex.getCause());
        }
    }

    /**
     * Log a warning message, including the full stack trace iff DEBUG logging is enabled.
     *
     * @param message
     *                the log message, which should have exactly one {@code {}} placeholder for {@code arg}
     * @param arg
     *                the argument for the message; typically a {@code JobId} or {@code JobRunnerKey}
     * @param e
     *                the exception, which will be full stack traced if DEBUG logging is enabled; otherwise, appended to
     *                the message in {@code toString()} form.
     */
    private static void logWarn(final String message, final Object arg, final Throwable e) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(message, arg, e);
        } else {
            LOGGER.warn(message + ": {}", arg, e.toString());
        }
    }

    @SuppressWarnings("null")
    @Nonnull
    static TriggerKey jobId(final @Nonnull JobId jobId) {
        return triggerKey(jobId.toString(), QUARTZ_TRIGGER_GROUP);
    }

    @SuppressWarnings("null")
    @Nonnull
    static JobKey jobRunnerKey(final @Nonnull JobRunnerKey jobRunnerKey) {
        return jobKey(jobRunnerKey.toString(), QUARTZ_JOB_GROUP);
    }

    private static SchedulerServiceException checked(final String message, final Throwable e) {
        return new SchedulerServiceException(message, e);
    }

    private static SchedulerRuntimeException unchecked(final String message, final Throwable e) {
        return new SchedulerRuntimeException(message, e);
    }
}
