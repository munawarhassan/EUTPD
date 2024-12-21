package com.pmi.tpd.scheduler.status;

import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_LOCALLY;
import static com.pmi.tpd.api.scheduler.config.RunMode.RUN_ONCE_PER_CLUSTER;
import static com.pmi.tpd.api.scheduler.config.Schedule.runOnce;
import static com.pmi.tpd.scheduler.Constants.BYTES_DEADF00D;
import static com.pmi.tpd.scheduler.Constants.BYTES_PARAMETERS;
import static com.pmi.tpd.scheduler.Constants.EMPTY_MAP;
import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static com.pmi.tpd.scheduler.Constants.KEY;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.pmi.tpd.api.scheduler.IJobRunner;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.JobRunnerKey;
import com.pmi.tpd.api.scheduler.status.IJobDetails;
import com.pmi.tpd.api.scheduler.util.ParameterMapSerializer;
import com.pmi.tpd.scheduler.AbstractSchedulerService;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

@SuppressWarnings({ "ConstantConditions", "ResultOfObjectAllocationIgnored" })
public class LazyJobDetailsTest extends MockitoTestCase {

    @Mock(lenient = true)
    private AbstractSchedulerService schedulerService;

    @BeforeEach
    public void setUp() {
        when(schedulerService.getParameterMapSerializer()).thenReturn(new ParameterMapSerializer());
    }

    @Test
    public void testSchedulerServiceNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LazyJobDetails(null, JOB_ID, KEY, RUN_LOCALLY, runOnce(null), new Date(), BYTES_PARAMETERS);
        });
    }

    @Test
    public void testJobIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LazyJobDetails(schedulerService, null, KEY, RUN_LOCALLY, runOnce(null), new Date(), BYTES_PARAMETERS);
        });
    }

    @Test
    public void testJobRunnerKeyNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LazyJobDetails(schedulerService, JOB_ID, null, RUN_LOCALLY, runOnce(null), new Date(),
                    BYTES_PARAMETERS);
        });
    }

    @Test
    public void testRunModeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LazyJobDetails(schedulerService, JOB_ID, KEY, null, runOnce(null), new Date(), BYTES_PARAMETERS);
        });
    }

    @Test
    public void testScheduleNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LazyJobDetails(schedulerService, JOB_ID, KEY, RUN_LOCALLY, null, new Date(), BYTES_PARAMETERS);
        });
    }

    @Test
    public void testValues1() {
        final Date nextRunTime = new Date();
        final Date expectedNextRunTime = new Date(nextRunTime.getTime());

        when(schedulerService.getJobRunner(KEY)).thenReturn(mock(IJobRunner.class));

        final IJobDetails jobDetails = new LazyJobDetails(schedulerService, JOB_ID, KEY, RUN_LOCALLY, runOnce(null),
                nextRunTime, null);

        assertEquals(JOB_ID, jobDetails.getJobId());
        assertEquals(KEY, jobDetails.getJobRunnerKey());
        assertEquals(RUN_LOCALLY, jobDetails.getRunMode());
        assertEquals(runOnce(null), jobDetails.getSchedule());

        assertTrue(jobDetails.isRunnable(), "Should be runnable");
        assertEquals(EMPTY_MAP, jobDetails.getParameters());

        nextRunTime.setTime(42L);
        assertEquals(expectedNextRunTime, jobDetails.getNextRunTime());
        jobDetails.getNextRunTime().setTime(42L);
        assertEquals(expectedNextRunTime, jobDetails.getNextRunTime());
    }

    @Test
    public void testValues2() {
        when(schedulerService.getJobRunner(JobRunnerKey.of("z"))).thenReturn(mock(IJobRunner.class));

        final IJobDetails jobDetails = new LazyJobDetails(schedulerService, JobId.of("x"), JobRunnerKey.of("z"),
                RUN_ONCE_PER_CLUSTER, runOnce(new Date(42L)), null, BYTES_DEADF00D);

        assertEquals(JobId.of("x"), jobDetails.getJobId());
        assertEquals(JobRunnerKey.of("z"), jobDetails.getJobRunnerKey());
        assertEquals(RUN_ONCE_PER_CLUSTER, jobDetails.getRunMode());
        assertEquals(runOnce(new Date(42L)), jobDetails.getSchedule());
        assertNull(jobDetails.getNextRunTime());

        assertFalse(jobDetails.isRunnable(), "Should not be runnable");
    }
}
