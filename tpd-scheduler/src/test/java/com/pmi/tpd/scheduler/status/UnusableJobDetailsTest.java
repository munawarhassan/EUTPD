package com.pmi.tpd.scheduler.status;

import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.config.Schedule.runOnce;
import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static com.pmi.tpd.scheduler.Constants.KEY;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.scheduler.JobRunnerNotRegisteredException;
import com.pmi.tpd.api.scheduler.SchedulerRuntimeException;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings({ "ResultOfObjectAllocationIgnored", "CastToConcreteClass", "ConstantConditions" })
public class UnusableJobDetailsTest extends TestCase {

    private static final byte[] RAW_PARAMETERS = new byte[] { 1, 2, 3, 4 };

    private static final Throwable CAUSE = new Throwable("For test");

    @Test
    public void testJobIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnusableJobDetails(null, KEY, RUN_LOCALLY, runOnce(null), new Date(), RAW_PARAMETERS, CAUSE);
        });
    }

    @Test
    public void testJobRunnerKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnusableJobDetails(JOB_ID, null, RUN_LOCALLY, runOnce(null), new Date(), RAW_PARAMETERS, CAUSE);
        });
    }

    @Test
    public void testRunModeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnusableJobDetails(JOB_ID, KEY, null, runOnce(null), new Date(), RAW_PARAMETERS, CAUSE);
        });
    }

    @Test
    public void testScheduleNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new UnusableJobDetails(JOB_ID, KEY, RUN_LOCALLY, null, new Date(), RAW_PARAMETERS, CAUSE);
        });
    }

    @Test
    public void testGetParametersWithNoCauseProvided() {
        final IJobDetails jobDetails = new UnusableJobDetails(JOB_ID, KEY, RUN_LOCALLY, runOnce(null), null, null,
                null);
        assertNull(((AbstractJobDetails) jobDetails).getRawParameters());
        try {
            final Map<String, Serializable> parameters = jobDetails.getParameters();
            fail("Should throw SchedulerRuntimeException, but got: " + parameters);
        } catch (final SchedulerRuntimeException sre) {
            final JobRunnerNotRegisteredException cause = (JobRunnerNotRegisteredException) sre.getCause();
            assertEquals(KEY, cause.getJobRunnerKey());
        }
    }

    @Test
    public void testGetParametersWithCauseProvided() {
        final IJobDetails jobDetails = new UnusableJobDetails(JOB_ID, KEY, RUN_LOCALLY, runOnce(null), null,
                RAW_PARAMETERS, CAUSE);
        assertArrayEquals(RAW_PARAMETERS, ((AbstractJobDetails) jobDetails).getRawParameters());
        try {
            final Map<String, Serializable> parameters = jobDetails.getParameters();
            fail("Should throw SchedulerRuntimeException, but got: " + parameters);
        } catch (final SchedulerRuntimeException sre) {
            assertEquals(CAUSE, sre.getCause());
        }
    }

    @Test
    public void testValues1() {
        final Date nextRunTime = new Date();
        final Date expectedNextRunTime = new Date(nextRunTime.getTime());

        final IJobDetails jobDetails = new UnusableJobDetails(JOB_ID, KEY, RUN_LOCALLY, runOnce(null), nextRunTime,
                RAW_PARAMETERS, null);
        assertEquals(JOB_ID, jobDetails.getJobId());
        assertEquals(KEY, jobDetails.getJobRunnerKey());
        assertEquals(RUN_LOCALLY, jobDetails.getRunMode());
        assertEquals(runOnce(null), jobDetails.getSchedule());
        assertArrayEquals(RAW_PARAMETERS, ((AbstractJobDetails) jobDetails).getRawParameters());
        assertFalse(jobDetails.isRunnable(), "Should not be runnable");

        nextRunTime.setTime(42L);
        assertEquals(expectedNextRunTime, jobDetails.getNextRunTime());
        jobDetails.getNextRunTime().setTime(42L);
        assertEquals(expectedNextRunTime, jobDetails.getNextRunTime());
    }

    @Test
    public void testValues2() {
        final IJobDetails jobDetails = new UnusableJobDetails(JobId.of("x"), JobRunnerKey.of("z"), RUN_ONCE_PER_CLUSTER,
                runOnce(new Date(42L)), null, null, CAUSE);
        assertEquals(JobId.of("x"), jobDetails.getJobId());
        assertEquals(JobRunnerKey.of("z"), jobDetails.getJobRunnerKey());
        assertEquals(RUN_ONCE_PER_CLUSTER, jobDetails.getRunMode());
        assertEquals(runOnce(new Date(42L)), jobDetails.getSchedule());
        assertNull(jobDetails.getNextRunTime());
        assertNull(((AbstractJobDetails) jobDetails).getRawParameters());
        assertFalse(jobDetails.isRunnable(), "Should not be runnable");
    }
}
