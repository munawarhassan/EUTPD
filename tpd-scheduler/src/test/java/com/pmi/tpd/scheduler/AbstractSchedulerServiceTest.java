package com.pmi.tpd.scheduler;

import static com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService.State.SHUTDOWN;
import static com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService.State.STANDBY;
import static com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService.State.STARTED;
import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.SchedulerServiceException;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.testing.junit5.TestCase;

public class AbstractSchedulerServiceTest extends TestCase {

    @Test
    public void testWaitForScheduledJobsToCompleteWithTimeoutSuccessful() throws Exception {
        final AbstractSchedulerService schedulerService = new SchedulerServiceFixture();
        final long start = System.currentTimeMillis();
        assertThat("should go idle", schedulerService.waitUntilIdle(1000L, MILLISECONDS), is(true));
        assertThat("should detect idle quickly, not take most or all of the whole timeout",
            System.currentTimeMillis() - start,
            lessThan(300L));
    }

    @Test
    public void testWaitForScheduledJobsToCompleteWithTimeoutUnsuccessful() throws Exception {
        final SchedulerServiceFixture schedulerService = spy(new SchedulerServiceFixture());
        schedulerService.hackAwaitNanos(); // Force instant 30ms decrement per call
        assertThat(schedulerService.enterJob(JOB_ID, mock(IRunningJob.class)), nullValue());

        assertThat("should time out", schedulerService.waitUntilIdle(100L, MILLISECONDS), is(false));

        // 100 -> 70 -> 40 -> 10 -> -20 => Timeout!
        verify(schedulerService, times(4)).awaitNanos(anyLong());
    }

    @Test
    public void testWaitForScheduledJobsToCompleteWithTimeoutInterrupted() throws Exception {
        assertThrows(InterruptedException.class, () -> {
            final AbstractSchedulerService schedulerService = spy(new SchedulerServiceFixture());
            MatcherAssert.assertThat(schedulerService.enterJob(JOB_ID, mock(IRunningJob.class)), nullValue());

            boolean hadToCallFail = false;
            try {
                Thread.currentThread().interrupt();
                final boolean result = schedulerService.waitUntilIdle(50L, MILLISECONDS);
                hadToCallFail = true;
                fail("Expected an InterruptedException but got result=" + result);
            } finally {
                // Don't leave the interrupted state polluted by the test
                final boolean threadWasInterrupted = Thread.interrupted();
                if (!hadToCallFail) {
                    assertThat("Interrupted flag should have already been clear", threadWasInterrupted, is(false));
                }
                verify(schedulerService).awaitNanos(anyLong());
            }
        });
    }

    @Test
    public void testWaitForScheduledJobsToCompleteWithTimeoutInterruptedButAlreadyIdle() throws Exception {
        final AbstractSchedulerService schedulerService = spy(new SchedulerServiceFixture());
        try {
            Thread.currentThread().interrupt();
            assertThat("should succeed", schedulerService.waitUntilIdle(50L, MILLISECONDS), is(true));
            assertThat("should still be interrupted", Thread.interrupted(), is(true));

            verify(schedulerService, never()).awaitNanos(anyLong());
        } finally {
            // Don't leave the interrupted state polluted by the test
            Thread.interrupted();
        }

    }

    @Test
    public void testEnterLeaveJob() {
        final AbstractSchedulerService schedulerService = new SchedulerServiceFixture();
        final IRunningJob job1 = mock(IRunningJob.class);
        final IRunningJob job2 = mock(IRunningJob.class);
        final JobId otherJobId = JobId.of("Some other job ID");
        final IRunningJob otherJob = mock(IRunningJob.class);

        assertThat("No jobs initially", schedulerService.getLocallyRunningJobs(), hasSize(0));
        assertThat("Enter other job", schedulerService.enterJob(otherJobId, otherJob), nullValue());
        assertThat("Entered other job", schedulerService.getLocallyRunningJobs(), contains(sameInstance(otherJob)));

        assertFailedLeave("Job is not running", schedulerService, job1);
        assertThat("Successful first entry when idle", schedulerService.enterJob(JOB_ID, job1), nullValue());
        assertThat(schedulerService.getLocallyRunningJobs(), containsInAnyOrder(job1, otherJob));

        assertFailedLeave("Wrong job specified", schedulerService, job2);
        assertThat(schedulerService.getLocallyRunningJobs(), containsInAnyOrder(job1, otherJob));

        assertThat("Unsuccessful re-entry", schedulerService.enterJob(JOB_ID, job1), sameInstance(job1));
        assertThat("Unsuccessful entry of second request", schedulerService.enterJob(JOB_ID, job2), sameInstance(job1));
        assertThat(schedulerService.getLocallyRunningJobs(), containsInAnyOrder(job1, otherJob));
        schedulerService.leaveJob(otherJobId, otherJob); // successful
        assertThat("Left other job", schedulerService.getLocallyRunningJobs(), contains(job1));

        schedulerService.leaveJob(JOB_ID, job1); // successful
        assertThat("Left job1", schedulerService.getLocallyRunningJobs(), hasSize(0));
        assertFailedLeave("Unsuccessful re-leave", schedulerService, job1);

        assertThat("Successful second entry when idle", schedulerService.enterJob(JOB_ID, job2), nullValue());
        assertThat("Left other job", schedulerService.getLocallyRunningJobs(), contains(job2));
    }

    static void assertFailedLeave(final String reason,
        final AbstractSchedulerService schedulerService,
        final IRunningJob job) {
        try {
            schedulerService.leaveJob(JOB_ID, job);
            fail("Expected unsuccessful leaveJob because " + reason);
        } catch (final IllegalStateException ise) {
            MatcherAssert.assertThat(ise.getMessage(), containsString("Invalid call to leaveJob"));
        }
    }

    @Test
    public void testLifecycle() throws SchedulerServiceException {
        final SchedulerServiceFixture schedulerService = new SchedulerServiceFixture();

        schedulerService.assertState(0, 0, 0, STANDBY);
        schedulerService.standby();
        schedulerService.assertState(0, 0, 0, STANDBY);

        schedulerService.start();
        schedulerService.assertState(1, 0, 0, STARTED);
        schedulerService.start();
        schedulerService.assertState(1, 0, 0, STARTED);

        schedulerService.standby();
        schedulerService.assertState(1, 1, 0, STANDBY);
        schedulerService.standby();
        schedulerService.assertState(1, 1, 0, STANDBY);

        schedulerService.start();
        schedulerService.assertState(2, 1, 0, STARTED);
        schedulerService.start();
        schedulerService.assertState(2, 1, 0, STARTED);

        schedulerService.shutdown();
        schedulerService.assertState(2, 1, 1, SHUTDOWN);
        schedulerService.shutdown();
        schedulerService.assertState(2, 1, 1, SHUTDOWN);

        try {
            schedulerService.start();
        } catch (final SchedulerServiceException sse) {
            MatcherAssert.assertThat(sse.getMessage(), containsString("shut down"));
            schedulerService.assertState(2, 1, 1, SHUTDOWN);
        }

        try {
            schedulerService.standby();
        } catch (final SchedulerServiceException sse) {
            MatcherAssert.assertThat(sse.getMessage(), containsString("shut down"));
            schedulerService.assertState(2, 1, 1, SHUTDOWN);
        }
    }
}
