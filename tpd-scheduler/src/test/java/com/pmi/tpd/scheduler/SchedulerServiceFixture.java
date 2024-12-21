package com.pmi.tpd.scheduler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.tenant.BareTenantAccessor;
import com.pmi.tpd.scheduler.support.MemoryRunDetailsDao;

/**
 * A test fixture for the {@code AbstractSchedulerService}.
 */
class SchedulerServiceFixture extends AbstractSchedulerService {

  /** */
  private static final long HACK_NANO_STEP = MILLISECONDS.toNanos(30L);

  /** */

  final AtomicInteger startImplCalls = new AtomicInteger();

  /** */

  final AtomicInteger standbyImplCalls = new AtomicInteger();

  /** */

  final AtomicInteger shutdownImplCalls = new AtomicInteger();

  /** */

  private volatile boolean hackAwaitNanos;

  /**
   *
   */
  SchedulerServiceFixture() {
    super(new MemoryRunDetailsDao(), new BareTenantAccessor());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void startImpl() {
    startImplCalls.incrementAndGet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void standbyImpl() {
    standbyImplCalls.incrementAndGet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void shutdownImpl() {
    shutdownImplCalls.incrementAndGet();
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public Set<JobRunnerKey> getJobRunnerKeysForAllScheduledJobs() {
    return ImmutableSet.of();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doScheduleJob(final JobId jobId, final JobConfig jobConfig) throws SchedulerServiceException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unscheduleJob(final JobId jobId) {
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @Override
  public Date calculateNextRunTime(final Schedule schedule) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IJobDetails getJobDetails(final JobId jobId) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Nonnull
  @Override
  public List<IJobDetails> getJobsByJobRunnerKey(final JobRunnerKey jobRunnerKey) {
    return ImmutableList.of();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  long awaitNanos(final long nanosLeft) throws InterruptedException {
    if (hackAwaitNanos) {
      return nanosLeft - HACK_NANO_STEP;
    }
    return super.awaitNanos(nanosLeft);
  }

  void hackAwaitNanos() {
    hackAwaitNanos = true;
  }

  void assertState(final int expectedStartCount,
      final int expectedStandbyCount,
      final int expectedShutdownCount,
      final State state) {
    MatcherAssert.assertThat("assertState(start, standby, shutdown, state)",
        Arrays.<Object>asList(startImplCalls.get(), standbyImplCalls.get(), shutdownImplCalls.get(), getState()),
        Matchers.<Object>contains(expectedStartCount, expectedStandbyCount, expectedShutdownCount, state));
  }

}
