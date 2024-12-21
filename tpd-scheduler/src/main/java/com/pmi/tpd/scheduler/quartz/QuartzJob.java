package com.pmi.tpd.scheduler.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.spi.TriggerFiredBundle;

import com.google.common.base.MoreObjects;
import com.pmi.tpd.scheduler.AbstractSchedulerService;
import com.pmi.tpd.scheduler.JobLauncher;
import com.pmi.tpd.api.scheduler.config.JobId;
import com.pmi.tpd.api.scheduler.config.RunMode;

/**
 * Quartz 2.x {@code Job} that delegates to {@link JobLauncher}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class QuartzJob implements Job {

    private final JobLauncher jobLauncher;

    QuartzJob(final AbstractSchedulerService schedulerService, final RunMode schedulerRunMode,
            final TriggerFiredBundle bundle) {
        final JobId jobId = JobId.of(bundle.getTrigger().getKey().getName());
        this.jobLauncher = new JobLauncher(schedulerService, schedulerRunMode, bundle.getFireTime(), jobId);
    }

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
        jobLauncher.launch();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("jobLauncher", jobLauncher).toString();
    }
}
