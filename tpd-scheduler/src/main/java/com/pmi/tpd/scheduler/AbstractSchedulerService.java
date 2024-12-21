package com.pmi.tpd.scheduler;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService;
import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.scheduler.status.IRunDetails;
import com.pmi.tpd.api.scheduler.status.RunDetailsImpl;
import com.pmi.tpd.api.scheduler.status.RunOutcome;
import com.pmi.tpd.api.scheduler.util.JobRunnerRegistry;
import com.pmi.tpd.api.scheduler.util.ParameterMapSerializer;
import com.pmi.tpd.api.tenant.ITenantAccessor;
import com.pmi.tpd.scheduler.spi.IRunDetailsDao;

/**
 * Base class for implementing a scheduler service. Provides {@link IJobRunner} registration, and job status tracking.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class AbstractSchedulerService implements ILifecycleAwareSchedulerService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSchedulerService.class);

    /**
     * Sorts by job ID (and nothing else). This is useful for implementing {@link #getJobsByJobRunnerKey(JobRunnerKey)}.
     */
    protected static final Comparator<IJobDetails> BY_JOB_ID = new ByJobId();

    /** */
    private static final int MAX_ATTEMPTS = 100;

    /** */
    private final Lock idleLock = new ReentrantLock();

    /** */
    private final Condition idleCondition = idleLock.newCondition();

    /** */
    private final JobRunnerRegistry jobRunnerRegistry = new JobRunnerRegistry();

    /** */
    private final ConcurrentMap<JobId, IRunningJob> runningJobs = new ConcurrentHashMap<>(16);

    /** */
    private final IRunDetailsDao runDetailsDao;

    /** */
    private final ITenantAccessor tenantAccessor;

    /** */
    private final ParameterMapSerializer parameterMapSerializer;

    /** */
    @Nonnull
    private volatile State state = State.STANDBY;

    protected AbstractSchedulerService(final @Nonnull IRunDetailsDao runDetailsDao,
            final @Nonnull ITenantAccessor tenantAccessor) {
        this(runDetailsDao, tenantAccessor, new ParameterMapSerializer());
    }

    protected AbstractSchedulerService(final @Nonnull IRunDetailsDao runDetailsDao,
            final @Nonnull ITenantAccessor tenantAccessor, final ParameterMapSerializer parameterMapSerializer) {
        this.runDetailsDao = checkNotNull(runDetailsDao, "runDetailsDao");
        this.tenantAccessor = checkNotNull(tenantAccessor, "tenantAccessor");
        this.parameterMapSerializer = parameterMapSerializer;
    }

    @Override
    public void registerJobRunner(final @Nonnull JobRunnerKey jobRunnerKey, final @Nonnull IJobRunner jobRunner) {
        LOGGER.debug("registerJobRunner: {}", jobRunnerKey);
        jobRunnerRegistry.registerJobRunner(jobRunnerKey, jobRunner);
    }

    @Override
    public void unregisterJobRunner(final @Nonnull JobRunnerKey jobRunnerKey) {
        LOGGER.debug("unregisterJobRunner: {}", jobRunnerKey);
        jobRunnerRegistry.unregisterJobRunner(jobRunnerKey);
    }

    @Nullable
    public IJobRunner getJobRunner(final @Nonnull JobRunnerKey jobRunnerKey) {
        return jobRunnerRegistry.getJobRunner(jobRunnerKey);
    }

    @Nonnull
    @Override
    public Set<JobRunnerKey> getRegisteredJobRunnerKeys() {
        return jobRunnerRegistry.getRegisteredJobRunnerKeys();
    }

    @Nonnull
    @Override
    public JobId scheduleJobWithGeneratedId(final JobConfig jobConfig) throws SchedulerServiceException {
        assertTenantAvailable();
        final JobId jobId = generateUniqueJobId();
        LOGGER.debug("scheduleJobWithGeneratedId: {} -> {}", jobConfig, jobId);
        scheduleJob(jobId, jobConfig);
        return jobId;
    }

    @Override
    final public void scheduleJob(final JobId jobId, final JobConfig jobConfig) throws SchedulerServiceException {
        assertTenantAvailable();
        doScheduleJob(jobId, jobConfig);
    }

    private void assertTenantAvailable() {
        if (Iterables.isEmpty(tenantAccessor.getAvailableTenants())) {
            throw new IllegalStateException("You are not allowed to schedule jobs before a tenant is available.");
        }
    }

    protected abstract void doScheduleJob(final JobId jobId, final JobConfig jobConfig)
            throws SchedulerServiceException;

    @SuppressWarnings("null")
    @Nonnull
    private JobId generateUniqueJobId() throws SchedulerServiceException {
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            final JobId jobId = JobId.of(UUID.randomUUID().toString());
            if (getJobDetails(jobId) == null) {
                return jobId;
            }
        }
        throw new SchedulerServiceException("Unable to generate a unique job ID");
    }

    /**
     * Creates or updates the job status record for the given job ID.
     *
     * @param jobId
     *                   the job ID for which the status is being updated
     * @param startedAt
     *                   the clock time that the run started at
     * @param runOutcome
     *                   the result of the run
     * @param message
     *                   an optional informational message to include in the {@code RunDetails}
     * @return the newly created run details.
     */
    public IRunDetails addRunDetails(final JobId jobId,
        final Date startedAt,
        final RunOutcome runOutcome,
        @Nullable final String message) {
        LOGGER.debug("addRunDetails: jobId={} startedAt={} runOutcome={} message={}",
            new Object[] { jobId, startedAt, runOutcome, message });

        checkNotNull(jobId, "jobId");
        checkNotNull(startedAt, "startedAt");
        checkNotNull(runOutcome, "runOutcome");

        final long duration = System.currentTimeMillis() - startedAt.getTime();
        final IRunDetails runDetails = new RunDetailsImpl(startedAt, runOutcome, duration, message);
        runDetailsDao.addRunDetails(jobId, runDetails);
        return runDetails;
    }

    /**
     * This method is called before a job begins execution.
     */
    public void preJob() {
    }

    /**
     * This method is called after a job has finished execution.
     */
    public void postJob() {
    }

    @Override
    synchronized public final void start() throws SchedulerServiceException {
        LOGGER.debug("{} -> STARTED", state);
        switch (state) {
            case STARTED:
                return;
            case SHUTDOWN:
                throw new SchedulerServiceException(
                        "The scheduler service has been shut down; it cannot be restarted.");
            default:
                break;
        }
        startImpl();
        state = State.STARTED;
    }

    @Override
    synchronized public final void standby() throws SchedulerServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} -> STANDBY", state);
        }
        switch (state) {
            case STANDBY:
                return;
            case SHUTDOWN:
                throw new SchedulerServiceException(
                        "The scheduler service has been shut down; it cannot be restarted.");
            default:
                break;
        }
        cancelJobs();
        standbyImpl();
        state = State.STANDBY;
    }

    @Override
    synchronized public final void shutdown() {
        LOGGER.debug("{} -> SHUTDOWN", state);
        if (state == State.SHUTDOWN) {
            return;
        }
        state = State.SHUTDOWN;
        cancelJobs();
        shutdownImpl();
    }

    private void cancelJobs() {
        for (final IRunningJob job : runningJobs.values()) {
            job.cancel();
        }
    }

    /**
     * Records beginning to run a job.
     * <p>
     * Any successful call to this method <strong>MUST</strong> be paired with a call to
     * {@link #leaveJob(JobId, RunningJob)} using the same arguments upon completion of the job. Success is defined by
     * this method returning {@code null} as opposed to an existing job.
     * </p>
     *
     * @param jobId
     *              the job ID that will be run
     * @param job
     *              the job that is about to be started
     * @return {@code null} if successful, meaning that the job was not running and has been successfully registered. If
     *         another job with that job ID is already running, then it is returned, instead.
     */
    IRunningJob enterJob(final JobId jobId, final IRunningJob job) {
        return runningJobs.putIfAbsent(jobId, job);
    }

    /**
     * Records the completion of a running job.
     * <p>
     * This must be called exactly once for each successful call to {@link #enterJob(JobId, RunningJob)}. Failing to
     * call it, calling it multiple times, or calling it with different arguments is a serious error and will throw an
     * {@code IllegalStateException} if/when it is detected. This behaviour should not be relied upon; it is intended
     * only to serve as an aid for checking the correctness of the scheduler implementation.
     * </p>
     *
     * @param jobId
     *              the job ID that was running
     * @param job
     *              the running job that has completed
     * @throws IllegalStateException
     *                               if this call to {@code leaveJob} does not follow a call to
     *                               {@link #enterJob(JobId,RunningJob)} with the same arguments, which indicates a
     *                               serious error in the scheduler implementation.
     */
    void leaveJob(final JobId jobId, final IRunningJob job) {
        if (!runningJobs.remove(jobId, job)) {
            throw new IllegalStateException("Invalid call to leaveJob(" + jobId + ", " + job
                    + "; actual running job for that ID is: " + runningJobs.get(jobId));
        }

        if (runningJobs.isEmpty()) {
            signalIdle();
        }
    }

    @GuardedBy("idleLock")
    private void signalIdle() {
        try {
            idleLock.lock();
            idleCondition.signalAll();
        } finally {
            idleLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementations may override the default implementation for {@code waitUntilIdle}. The default implementation
     * that {@code AbstractSchedulerService} provides will poll {@link #getLocallyRunningJobs()} every 100ms until one
     * of the following occurs:
     * </p>
     * <ol>
     * <li>{@link #getLocallyRunningJobs()} returns an empty collection.</li>
     * <li>The current thread is interrupted.</li>
     * <li>The timeout is exhausted.</li>
     * </ol>
     */
    @Override
    public boolean waitUntilIdle(final long timeout, final TimeUnit units) throws InterruptedException {
        if (runningJobs.isEmpty()) {
            return true;
        }
        if (timeout <= 0L) {
            return false;
        }

        try {
            idleLock.lock();
            return waitUntilIdle(units.toNanos(timeout));
        } finally {
            idleLock.unlock();
        }
    }

    @GuardedBy("idleLock")
    boolean waitUntilIdle(final long timeoutInNanos) throws InterruptedException {
        long nanosLeft = timeoutInNanos;
        while (nanosLeft > 0L) {
            nanosLeft = awaitNanos(nanosLeft);
            if (runningJobs.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    long awaitNanos(final long nanosLeft) throws InterruptedException {
        return idleCondition.awaitNanos(nanosLeft);
    }

    @SuppressWarnings("null")
    @Nonnull
    @Override
    public Collection<IRunningJob> getLocallyRunningJobs() {
        return ImmutableList.copyOf(runningJobs.values());
    }

    @Nonnull
    @Override
    public final State getState() {
        return state;
    }

    /**
     * Provided by the scheduler service to implement {@link #start()} requests. This is only ever called while in
     * {@link State#STANDBY}, and throwing an exception will prevent the state from being updated.
     *
     * @throws SchedulerServiceException
     *                                   if the scheduler implementation fails to start
     */
    protected abstract void startImpl() throws SchedulerServiceException;

    /**
     * Provided by the scheduler service to implement {@link #standby()} requests. This is only ever called while in
     * {@link State#STARTED}, and throwing an exception will prevent the state from being updated.
     *
     * @throws SchedulerServiceException
     *                                   if the scheduler implementation fails to enter standby mode
     */
    protected abstract void standbyImpl() throws SchedulerServiceException;

    /**
     * Provided by the scheduler service to implement {@link #shutdown()} requests. This is only ever called if the
     * scheduler has not already been shut down, and throwing an exception <em>does not</em> prevent the scheduler
     * service from entering this state.
     */
    protected abstract void shutdownImpl();

    /**
     * Returns the parameter map serializer used by this scheduler.
     *
     * @return the parameter map serializer used by this scheduler.
     */
    public ParameterMapSerializer getParameterMapSerializer() {
        return parameterMapSerializer;
    }

    /**
     * Converts the unchecked {@code SchedulerRuntimeException} to the checked {@code SchedulerServiceException}. It
     * makes sense to do this in many cases where the checked exception is declared, as the caller is already having to
     * handle it.
     * <p>
     * The message and cause of the {@code SchedulerServiceException} are taken from the {@link Throwable#getCause()
     * cause} of the runtime exception. If no cause was set, then the runtime exception itself is used.
     * </p>
     *
     * @param sre
     *            the scheduler runtime exception to convert
     * @return the converted exception
     */
    protected static SchedulerServiceException checked(final SchedulerRuntimeException sre) {
        Throwable cause = sre.getCause();
        if (cause == null) {
            cause = sre;
        }
        return new SchedulerServiceException(cause.toString(), cause);
    }

    static class ByJobId implements Comparator<IJobDetails>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(final IJobDetails jd1, final IJobDetails jd2) {
            return jd1.getJobId().compareTo(jd2.getJobId());
        }
    }
}
