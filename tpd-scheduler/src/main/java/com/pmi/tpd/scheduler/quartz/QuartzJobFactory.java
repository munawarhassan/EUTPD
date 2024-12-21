package com.pmi.tpd.scheduler.quartz;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.simpl.SimpleJobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.pmi.tpd.api.scheduler.config.RunMode;
import com.pmi.tpd.scheduler.AbstractSchedulerService;

/**
 * Assigned to the real scheduler so that we can escape the Quartz-centric
 * world.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
class QuartzJobFactory extends SimpleJobFactory {

  /** */
  private final AbstractSchedulerService schedulerService;

  /** */
  private final RunMode schedulerRunMode;

  QuartzJobFactory(@Nonnull final AbstractSchedulerService schedulerService,
      @Nonnull final RunMode schedulerRunMode) {
    this.schedulerService = checkNotNull(schedulerService, "schedulerService");
    this.schedulerRunMode = checkNotNull(schedulerRunMode, "schedulerRunMode");
  }

  @Override
  public Job newJob(final TriggerFiredBundle bundle, final Scheduler scheduler) throws SchedulerException {
    final JobDetail jobDetail = bundle.getJobDetail();
    if (QuartzJob.class.equals(jobDetail.getJobClass())) {
      return new QuartzJob(schedulerService, schedulerRunMode, bundle);
    }
    return new ClassLoaderProtectingWrappedJob(super.newJob(bundle, scheduler), schedulerService);
  }

  // : Ensure that the Job runs with its own class loader set as the thread's CCL
  static class ClassLoaderProtectingWrappedJob implements Job {

    /** */
    private final Job delegate;

    /** */
    private final AbstractSchedulerService service;

    ClassLoaderProtectingWrappedJob(final Job delegate, final AbstractSchedulerService service) {
      this.service = service;
      this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException {
      service.preJob();
      final Thread thd = Thread.currentThread();
      final ClassLoader originalClassLoader = thd.getContextClassLoader();
      try {
        thd.setContextClassLoader(delegate.getClass().getClassLoader());
        delegate.execute(context);
      } finally {
        thd.setContextClassLoader(originalClassLoader);
        service.postJob();
      }
    }
  }
}
