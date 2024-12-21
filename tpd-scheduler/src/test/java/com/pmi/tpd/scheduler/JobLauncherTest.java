package com.pmi.tpd.scheduler;

import static com.pmi.tpd.api.scheduler.JobRunnerResponse.failed;
import static com.pmi.tpd.api.scheduler.JobRunnerResponse.success;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.ABORTED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.FAILED;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.SUCCESS;
import static com.pmi.tpd.api.scheduler.status.RunOutcome.UNAVAILABLE;
import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static com.pmi.tpd.scheduler.Constants.KEY;
import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.lessThan;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.IJobRunnerRequest;
import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.JobRunnerResponse;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.api.scheduler.config.Schedule;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.scheduler.status.UnusableJobDetails;
import com.pmi.tpd.scheduler.support.RunningJobImpl;
import com.pmi.tpd.scheduler.support.SimpleJobDetails;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 */
@SuppressWarnings({ "ResultOfObjectAllocationIgnored", "ConstantConditions" })
public class JobLauncherTest extends MockitoTestCase {

    private static final Date NOW = new Date();

    private static final Schedule SCHEDULE = Schedule.forInterval(60000L, null);

    @Test
    public void testSchedulerServiceNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JobLauncher(null, RUN_LOCALLY, new Date(), JOB_ID);
        });
    }

    @Test
    public void testRunModeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JobLauncher(mock(AbstractSchedulerService.class), null, new Date(), JOB_ID);
        });
    }

    @Test
    public void testJobIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new JobLauncher(mock(AbstractSchedulerService.class), RUN_LOCALLY, new Date(), null);
        });
    }

    @Test
    public void testLaunchJobDetailsNull() {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, null, JOB_ID);
        assertCloseToNow(jobLauncher.firedAt);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, jobLauncher.firedAt, ABORTED, "No corresponding job details");
    }

    @Test
    public void testLaunchJobDetailsNotRunnable() {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);
        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(unusable(null));

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, UNAVAILABLE, "Job runner key 'test.key' is not registered");
    }

    @Test
    public void testLaunchJobRunnerVanished() {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);
        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(details());

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, UNAVAILABLE, "Job runner key 'test.key' is not registered");
    }

    @Test
    public void testLaunchRunModeInconsistency() {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class,
            withSettings().lenient());
        final IJobRunner jobRunner = mock(IJobRunner.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_ONCE_PER_CLUSTER, NOW, JOB_ID);
        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(details());
        when(schedulerService.getJobRunner(KEY)).thenReturn(jobRunner);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID,
            NOW,
            ABORTED,
            "Inconsistent run mode: expected 'RUN_LOCALLY' got: 'RUN_ONCE_PER_CLUSTER'");
    }

    @Test
    public void testLaunchJobRunnerWhileAlreadyRunning() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final IJobRunner jobRunner = mock(IJobRunner.class);
        final IRunningJob existing = mock(IRunningJob.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);

        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(details());
        when(schedulerService.getJobRunner(KEY)).thenReturn(jobRunner);
        when(schedulerService.enterJob(eq(JOB_ID), any(IRunningJob.class))).thenReturn(existing);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, ABORTED, "Already running");
        verify(schedulerService).enterJob(eq(JOB_ID), any(IRunningJob.class));
        verify(schedulerService, never()).leaveJob(eq(JOB_ID), any(IRunningJob.class));
        verify(schedulerService, never()).unscheduleJob(JOB_ID);
        verify(schedulerService, never()).preJob();
        verify(schedulerService, never()).postJob();
        verify(jobRunner, never()).runJob(new RunningJobImpl(NOW, JOB_ID, config()));
    }

    @Test
    public void testLaunchJobRunnerThatReturnsNull() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final IJobRunner jobRunner = mock(IJobRunner.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);

        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(details());
        when(schedulerService.getJobRunner(KEY)).thenReturn(jobRunner);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, SUCCESS, null);
        verify(schedulerService, never()).unscheduleJob(JOB_ID);
        assertJobLifeCycle(schedulerService, jobRunner, config());
    }

    @Test
    public void testLaunchJobRunnerThatReturnsInfo() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final IJobRunner jobRunner = mock(IJobRunner.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);
        final IJobRunnerRequest request = new RunningJobImpl(NOW, JOB_ID, config());
        final JobRunnerResponse response = success("Info");

        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(details());
        when(schedulerService.getJobRunner(KEY)).thenReturn(jobRunner);
        when(jobRunner.runJob(request)).thenReturn(response);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, SUCCESS, "Info");
        verify(schedulerService, never()).unscheduleJob(JOB_ID);
        assertJobLifeCycle(schedulerService, jobRunner, config());
    }

    @Test
    public void testLaunchDeletesRunOnceJobOnSuccess() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final IJobRunner jobRunner = mock(IJobRunner.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);
        final IJobDetails jobDetails = new SimpleJobDetails(JOB_ID, KEY, RUN_LOCALLY, Schedule.runOnce(null), null,
                null, null);
        final JobConfig config = config(jobDetails);
        final IJobRunnerRequest request = new RunningJobImpl(NOW, JOB_ID, config);
        final JobRunnerResponse response = success("Info");

        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(jobDetails);
        when(schedulerService.getJobRunner(KEY)).thenReturn(jobRunner);
        when(jobRunner.runJob(request)).thenReturn(response);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, SUCCESS, "Info");
        verify(schedulerService).unscheduleJob(JOB_ID);
        assertJobLifeCycle(schedulerService, jobRunner, config);
    }

    @Test
    public void testLaunchDeletesRunOnceJobOnFailure() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final IJobRunner jobRunner = mock(IJobRunner.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);
        final IJobDetails jobDetails = new SimpleJobDetails(JOB_ID, KEY, RUN_LOCALLY, Schedule.runOnce(null), null,
                null, null);
        final JobConfig config = config(jobDetails);
        final IJobRunnerRequest request = new RunningJobImpl(NOW, JOB_ID, config);
        final IllegalArgumentException testEx = new IllegalArgumentException("Just testing!");

        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(jobDetails);
        when(schedulerService.getJobRunner(KEY)).thenReturn(jobRunner);
        when(jobRunner.runJob(request)).thenThrow(testEx);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, FAILED, failed(testEx).getMessage());
        verify(schedulerService).unscheduleJob(JOB_ID);
        assertJobLifeCycle(schedulerService, jobRunner, config);
    }

    @Test
    public void testLaunchDeletesRunOnceJobOnUnavailable() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);
        final IJobDetails jobDetails = new UnusableJobDetails(JOB_ID, KEY, RUN_LOCALLY, Schedule.runOnce(null), null,
                null, null);

        when(schedulerService.getJobDetails(JOB_ID)).thenReturn(jobDetails);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, UNAVAILABLE, "Job runner key 'test.key' is not registered");
        verify(schedulerService).unscheduleJob(JOB_ID);
    }

    @Test
    public void testLaunchJobRunnerDoesNotAttemptToDeleteWithoutJobDetails() throws Exception {
        final AbstractSchedulerService schedulerService = mock(AbstractSchedulerService.class);
        final JobLauncher jobLauncher = new JobLauncher(schedulerService, RUN_LOCALLY, NOW, JOB_ID);

        jobLauncher.launch();

        verify(schedulerService).addRunDetails(JOB_ID, NOW, ABORTED, "No corresponding job details");
        verify(schedulerService, never()).unscheduleJob(JOB_ID);
    }

    private static void assertCloseToNow(final Date date) {
        assertNotNull(date, "Expected a date close to now but got null");
        final long delta = abs(currentTimeMillis() - date.getTime());
        assertThat("Expected date to be close to now but the delta was too large", delta, lessThan(1000L));
    }

    private static void assertJobLifeCycle(final AbstractSchedulerService schedulerService,
        final IJobRunner jobRunner,
        final JobConfig config) {
        final InOrder inOrder = inOrder(schedulerService, jobRunner);
        final ArgumentCaptor<IRunningJob> jobCaptor = ArgumentCaptor.forClass(IRunningJob.class);
        inOrder.verify(schedulerService).enterJob(eq(JOB_ID), jobCaptor.capture());
        inOrder.verify(schedulerService).preJob();
        inOrder.verify(jobRunner).runJob(new RunningJobImpl(NOW, JOB_ID, config));
        inOrder.verify(schedulerService).leaveJob(JOB_ID, jobCaptor.getValue());
        inOrder.verify(schedulerService).postJob();
    }

    private static JobConfig config() {
        return config(details());
    }

    private static JobConfig config(final IJobDetails jobDetails) {
        return JobConfig.forJobRunnerKey(jobDetails.getJobRunnerKey())
                .withRunMode(jobDetails.getRunMode())
                .withSchedule(jobDetails.getSchedule())
                .withParameters(jobDetails.getParameters());
    }

    private static SimpleJobDetails details() {
        return new SimpleJobDetails(JOB_ID, KEY, RUN_LOCALLY, SCHEDULE, null, null, null);
    }

    private static UnusableJobDetails unusable(final String reason) {
        return new UnusableJobDetails(JOB_ID, KEY, RUN_LOCALLY, SCHEDULE, null, null,
                reason != null ? new IllegalStateException("Bet you didn't see this coming!") : null);
    }
}
