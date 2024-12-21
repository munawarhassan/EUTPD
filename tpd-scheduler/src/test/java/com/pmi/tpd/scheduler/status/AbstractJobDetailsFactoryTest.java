package com.pmi.tpd.scheduler.status;

import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.config.Schedule.forInterval;
import static com.pmi.tpd.api.scheduler.config.Schedule.runOnce;
import static com.pmi.tpd.scheduler.Constants.BYTES_DEADF00D;
import static com.pmi.tpd.scheduler.Constants.BYTES_PARAMETERS;
import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static com.pmi.tpd.scheduler.Constants.KEY;
import static com.pmi.tpd.scheduler.Constants.PARAMETERS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.scheduler.util.ParameterMapSerializer;
import com.pmi.tpd.scheduler.AbstractSchedulerService;
import com.pmi.tpd.scheduler.status.AbstractJobDetailsFactoryTest.Fixture;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

@SuppressWarnings({ "ResultOfObjectAllocationIgnored", "ConstantConditions" })
public class AbstractJobDetailsFactoryTest extends MockitoTestCase {

  @Test
  public void testSchedulerServiceIsNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Fixture(null, 1L, KEY, runOnce(null), new Date(), null);
    });
  }

  @Test
  public void testJobIdNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Fixture(service(), 1L, KEY, runOnce(null), new Date(), null).buildJobDetails(null, 1L, RUN_LOCALLY);
    });
  }

  @Test
  public void testJobDataNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Fixture(service(), null, KEY, runOnce(null), new Date(), null)
          .buildJobDetails(JOB_ID, null, RUN_LOCALLY);
    });
  }

  @Test
  public void testRunModeNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Fixture(service(), null, KEY, runOnce(null), new Date(), null).buildJobDetails(JOB_ID, 1L, null);
    });
  }

  @Test
  public void testJobRunnerKeyNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Fixture(service(), 1L, null, runOnce(null), new Date(), null).buildJobDetails(JOB_ID, 1L, RUN_LOCALLY);
    });
  }

  @Test
  public void testScheduleNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      new Fixture(service(), 1L, KEY, null, new Date(), null).buildJobDetails(JOB_ID, 1L, RUN_LOCALLY);
    });
  }

  @Test
  public void testNoJobRunner() {
    final AbstractSchedulerService service = service();
    final Date now = new Date();
    final Fixture fixture = new Fixture(service, 1L, KEY, runOnce(now), null, null);
    final IJobDetails jobDetails = fixture.buildJobDetails(JOB_ID, 1L, RUN_LOCALLY);

    assertThat(jobDetails.getJobId(), is(JOB_ID));
    assertThat(jobDetails.getJobRunnerKey(), is(KEY));
    assertThat(jobDetails.getNextRunTime(), nullValue());
    assertThat(jobDetails.getRunMode(), is(RUN_LOCALLY));
    assertThat(jobDetails.getSchedule(), is(runOnce(now)));
    assertThat(jobDetails.isRunnable(), is(false));
  }

  @Test
  public void testJobRunnerClassLoaderFails() {
    final AbstractSchedulerService service = service();
    final Date now = new Date();
    final Fixture fixture = new Fixture(service, 2L, KEY, runOnce(now), null, BYTES_DEADF00D);
    when(service.getJobRunner(KEY)).thenReturn(mock(IJobRunner.class));

    final IJobDetails jobDetails = fixture.buildJobDetails(JOB_ID, 2L, RUN_LOCALLY);

    assertThat(jobDetails.getJobId(), is(JOB_ID));
    assertThat(jobDetails.getJobRunnerKey(), is(KEY));
    assertThat(jobDetails.getNextRunTime(), nullValue());
    assertThat(jobDetails.getRunMode(), is(RUN_LOCALLY));
    assertThat(jobDetails.getSchedule(), is(runOnce(now)));
    assertThat(jobDetails.isRunnable(), is(false));
  }

  @Test
  public void testValues() {
    final AbstractSchedulerService service = service();
    final Date now = new Date();
    final Fixture fixture = new Fixture(service, 3L, KEY, forInterval(60000L, now), null, BYTES_PARAMETERS);
    when(service.getJobRunner(KEY)).thenReturn(mock(IJobRunner.class));

    final IJobDetails jobDetails = fixture.buildJobDetails(JOB_ID, 3L, RUN_ONCE_PER_CLUSTER);

    assertThat(jobDetails.getJobId(), is(JOB_ID));
    assertThat(jobDetails.getJobRunnerKey(), is(KEY));
    assertThat(jobDetails.getNextRunTime(), nullValue());
    assertThat(jobDetails.getRunMode(), is(RUN_ONCE_PER_CLUSTER));
    assertThat(jobDetails.getSchedule(), is(forInterval(60000L, now)));
    assertThat(jobDetails.isRunnable(), is(true));
    assertThat(jobDetails.getParameters(), is(PARAMETERS));
  }

  static AbstractSchedulerService service() {
    final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class,
        withSettings().lenient());
    when(schedulerService.getParameterMapSerializer()).thenReturn(new ParameterMapSerializer());
    return schedulerService;
  }

  @SuppressWarnings({ "AssignmentToDateFieldFromParameter", "AssignmentToCollectionOrArrayFieldFromParameter" })
  static class Fixture extends AbstractJobDetailsFactory<Long> {

    private final Long expected;

    private final JobRunnerKey jobRunnerKey;

    private final Schedule schedule;

    private final Date nextRunTime;

    private final byte[] parameters;

    public Fixture(final AbstractSchedulerService schedulerService, final Long expected,
        final JobRunnerKey jobRunnerKey, final Schedule schedule, final Date nextRunTime,
        final byte[] parameters) {
      super(schedulerService);
      this.expected = expected;
      this.jobRunnerKey = jobRunnerKey;
      this.schedule = schedule;
      this.nextRunTime = nextRunTime;
      this.parameters = parameters;
    }

    @Nonnull
    @Override
    protected JobRunnerKey getJobRunnerKey(final Long jobData) {
      assertEquals(expected, jobData);
      return jobRunnerKey;
    }

    @Nonnull
    @Override
    protected Schedule getSchedule(final Long jobData) {
      assertEquals(expected, jobData);
      return schedule;
    }

    @Nullable
    @Override
    protected Date getNextRunTime(final Long jobData) {
      assertEquals(expected, jobData);
      return nextRunTime;
    }

    @Nullable
    @Override
    protected byte[] getSerializedParameters(final Long jobData) {
      assertEquals(expected, jobData);
      return parameters;
    }
  }
}
