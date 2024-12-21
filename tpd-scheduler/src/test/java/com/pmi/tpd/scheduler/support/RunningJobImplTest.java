package com.pmi.tpd.scheduler.support;

import static com.pmi.tpd.scheduler.Constants.JOB_ID;
import static com.pmi.tpd.scheduler.Constants.KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Date;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.scheduler.IRunningJob;
import com.pmi.tpd.api.scheduler.config.JobConfig;
import com.pmi.tpd.testing.junit5.TestCase;

@SuppressWarnings({ "ResultOfObjectAllocationIgnored", "ConstantConditions" })
public class RunningJobImplTest extends TestCase {

    @Test
    public void testStartTimeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RunningJobImpl(null, JOB_ID, JobConfig.forJobRunnerKey(KEY));
        });
    }

    @Test
    public void testJobIdNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RunningJobImpl(new Date(), null, JobConfig.forJobRunnerKey(KEY));
        });
    }

    @Test
    public void testJobConfigNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RunningJobImpl(new Date(), JOB_ID, null);
        });
    }

    @Test
    public void testValues() {
        final Date startTime = new Date();
        final long originalTime = startTime.getTime();
        final JobConfig config = JobConfig.forJobRunnerKey(KEY);
        final IRunningJob job = new RunningJobImpl(startTime, JOB_ID, config);

        MatcherAssert.assertThat(job.getStartTime(), equalTo(startTime));
        MatcherAssert.assertThat(job.getJobId(), equalTo(JOB_ID));
        MatcherAssert.assertThat(job.getJobConfig(), sameInstance(config));

        // Date copy checks
        startTime.setTime(42L);
        MatcherAssert.assertThat(job.getStartTime().getTime(), equalTo(originalTime));
        job.getStartTime().setTime(42L);
        MatcherAssert.assertThat(job.getStartTime().getTime(), equalTo(originalTime));
    }

    @Test
    public void testCancelFlag() {
        final JobConfig config = JobConfig.forJobRunnerKey(KEY);
        final IRunningJob job = new RunningJobImpl(new Date(), JOB_ID, config);

        MatcherAssert.assertThat("Not cancelled initially", job.isCancellationRequested(), is(false));

        job.cancel();
        MatcherAssert.assertThat("Cancelled once requested", job.isCancellationRequested(), is(true));

        job.cancel();
        MatcherAssert.assertThat("Redundant cancel() has no effect", job.isCancellationRequested(), is(true));
    }
}
