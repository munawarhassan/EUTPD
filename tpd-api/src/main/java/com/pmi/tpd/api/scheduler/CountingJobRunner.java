package com.pmi.tpd.api.scheduler;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.api.scheduler.config.Schedule;

public class CountingJobRunner implements IJobRunner, IStartable {

    private static final JobRunnerKey RUNNER_KEY = JobRunnerKey.of("SchedulerTest");

    private static final String CRON_JOB_PREFIX = RUNNER_KEY.toString() + ".cron";

    private static final String INTERVAL_JOB_PREFIX = RUNNER_KEY.toString() + ".interval";

    private static final String ONCE_JOB_PREFIX = RUNNER_KEY.toString() + ".once";

    private static final Logger log = LoggerFactory.getLogger(CountingJobRunner.class);

    private final ConcurrentMap<JobId, Counter> counts;

    private final ISchedulerService schedulerService;

    public CountingJobRunner(@Nonnull final ISchedulerService schedulerService) {
        this.schedulerService = checkNotNull(schedulerService, "schedulerService");

        ImmutableMap.Builder<JobId, Counter> builder = ImmutableMap.builder();

        for (final RunMode runMode : RunMode.values()) {
            builder = builder.put(jobId(CRON_JOB_PREFIX, runMode), new Counter(2))
                    .put(jobId(INTERVAL_JOB_PREFIX, runMode), new Counter(2))
                    .put(jobId(ONCE_JOB_PREFIX, runMode), new Counter(1));
        }

        counts = Maps.newConcurrentMap();
    }

    @Override
    public JobRunnerResponse runJob(final IJobRunnerRequest request) {
        final Counter counter = counts.get(request.getJobId());
        if (counter == null) {
            return JobRunnerResponse.aborted("Unknown job id " + request.getJobId());
        }

        final int value = counter.increment();
        if (log.isDebugEnabled()) {
            log.debug("Job {} has now run {} time(s)", request.getJobId(), value);
        }

        return JobRunnerResponse.success();
    }

    @Override
    public void start() {
        schedulerService.registerJobRunner(RUNNER_KEY, this);

        for (final RunMode runMode : RunMode.values()) {
            // Run every minute. Scheduler prevents us from running more frequently than
            // this
            scheduleJob(CRON_JOB_PREFIX, runMode, Schedule.forCronExpression("0 0/1 * * * ?"), 2);

            // Run once per minute. Scheduler prevents us from running more frequently than
            // this
            scheduleJob(INTERVAL_JOB_PREFIX,
                runMode,
                Schedule.forInterval(60000, new Date(System.currentTimeMillis() + 5000)),
                2);

            scheduleJob(ONCE_JOB_PREFIX, runMode, Schedule.runOnce(new Date(System.currentTimeMillis() + 5000)), 1);
        }
    }

    private void scheduleJob(final String jobPrefix,
        final RunMode runMode,
        final Schedule schedule,
        final int minCount) {
        final JobId jobId = jobId(jobPrefix, runMode);
        final Counter newCounter = new Counter(minCount);
        if (counts.putIfAbsent(jobId, new Counter(minCount)) != null) {
            log.warn("Counter already registered for {}", jobId);
        }
        try {
            schedulerService.scheduleJob(jobId,
                JobConfig.forJobRunnerKey(RUNNER_KEY).withSchedule(schedule).withRunMode(runMode));
        } catch (final SchedulerServiceException e) {
            log.warn("Failed to register job {}", jobId, e);
            counts.remove(jobId, newCounter);
        }
    }

    public int awaitCount(final String jobPrefix, final RunMode runMode, final long timeout, final TimeUnit unit) {
        final Counter counter = counts.get(jobId(jobPrefix, runMode));

        if (counter == null) {
            return -1;
        }

        return counter.await(timeout, unit);
    }

    private static JobId jobId(final String prefix, final RunMode runMode) {
        return JobId.of(prefix + " / " + runMode.name());
    }

    private static class Counter {

        private final CountDownLatch latch;

        private final AtomicInteger value;

        private final AtomicInteger waitingCount;

        private Counter(final int minCount) {
            latch = new CountDownLatch(minCount);
            value = new AtomicInteger(0);
            waitingCount = new AtomicInteger(0);
        }

        public int increment() {
            final int value = this.value.incrementAndGet();
            latch.countDown();
            if (log.isDebugEnabled()) {
                log.debug("count = {}, remaining = {}, waiting = {}", value, latch.getCount(), waitingCount.get());
            }

            return value;
        }

        public int await(final long timeout, final TimeUnit unit) {
            try {
                waitingCount.incrementAndGet();
                latch.await(timeout, unit);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                waitingCount.decrementAndGet();
            }

            return value.get();
        }

    }

}
