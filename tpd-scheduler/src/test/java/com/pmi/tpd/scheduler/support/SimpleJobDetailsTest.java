package com.pmi.tpd.scheduler.support;

import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.config.Schedule.runOnce;
import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static com.pmi.tpd.scheduler.Constants.KEY;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.scheduler.status.AbstractJobDetails;
import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings({ "ResultOfObjectAllocationIgnored", "CastToConcreteClass", "ConstantConditions" })
public class SimpleJobDetailsTest extends TestCase {

    private static final byte[] RAW_PARAMETERS = new byte[] { 1, 2, 3, 4 };

    private static final Map<String, Serializable> NO_PARAMETERS = ImmutableMap.of();

    private static final Map<String, Serializable> PARAMETERS = ImmutableMap.<String, Serializable> builder()
            .put("Hello", 42L)
            .put("World", true)
            .build();

    @Test
    public void testJobIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleJobDetails(null, KEY, RUN_LOCALLY, runOnce(null), new Date(), RAW_PARAMETERS, PARAMETERS);
        });
    }

    @Test
    public void testJobRunnerKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleJobDetails(JOB_ID, null, RUN_LOCALLY, runOnce(null), new Date(), RAW_PARAMETERS, PARAMETERS);
        });
    }

    @Test
    public void testRunModeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleJobDetails(JOB_ID, null, RUN_LOCALLY, runOnce(null), new Date(), RAW_PARAMETERS, PARAMETERS);
        });
    }

    @Test
    public void testScheduleNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new SimpleJobDetails(JOB_ID, KEY, RUN_LOCALLY, null, new Date(), RAW_PARAMETERS, PARAMETERS);
        });
    }

    @Test
    public void testValues1() {
        final Date nextRunTime = new Date();
        final Date expectedNextRunTime = new Date(nextRunTime.getTime());
        final IJobDetails jobDetails = new SimpleJobDetails(JOB_ID, KEY, RUN_LOCALLY, runOnce(null), nextRunTime, null,
                null);

        assertEquals(JOB_ID, jobDetails.getJobId());
        assertEquals(KEY, jobDetails.getJobRunnerKey());
        assertEquals(RUN_LOCALLY, jobDetails.getRunMode());
        assertEquals(runOnce(null), jobDetails.getSchedule());
        assertEquals(NO_PARAMETERS, jobDetails.getParameters());
        assertNull(((AbstractJobDetails) jobDetails).getRawParameters());
        assertTrue(jobDetails.isRunnable(), "Should be runnable");

        nextRunTime.setTime(42L);
        assertEquals(expectedNextRunTime, jobDetails.getNextRunTime());
        jobDetails.getNextRunTime().setTime(42L);
        assertEquals(expectedNextRunTime, jobDetails.getNextRunTime());
    }

    @Test
    public void testValues2() {
        final IJobDetails jobDetails = new SimpleJobDetails(JobId.of("x"), JobRunnerKey.of("z"), RUN_ONCE_PER_CLUSTER,
                runOnce(new Date(42L)), null, RAW_PARAMETERS, PARAMETERS);
        assertEquals(JobId.of("x"), jobDetails.getJobId());
        assertEquals(JobRunnerKey.of("z"), jobDetails.getJobRunnerKey());
        assertEquals(RUN_ONCE_PER_CLUSTER, jobDetails.getRunMode());
        assertEquals(runOnce(new Date(42L)), jobDetails.getSchedule());
        assertNull(jobDetails.getNextRunTime());
        assertArrayEquals(RAW_PARAMETERS, ((AbstractJobDetails) jobDetails).getRawParameters());
        assertEquals(PARAMETERS, jobDetails.getParameters());
        assertTrue(jobDetails.isRunnable(), "Should be runnable");
    }
}
