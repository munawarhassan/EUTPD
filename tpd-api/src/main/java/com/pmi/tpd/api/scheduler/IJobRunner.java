package com.pmi.tpd.api.scheduler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.config.JobRunnerKey;

/**
 * Invoked by the {@link ISchedulerService} when it is time for a scheduled job
 * to run.
 * <p>
 * Application code should register the JobRunner on startup, and need do
 * nothing on shutdown.
 * </p>
 * <p>
 * Plugins should register the JobRunner implementation at startup/plugin
 * enabled, and
 * {@link ISchedulerService#unregisterJobRunner(JobRunnerKey) unregister} the
 * {@code JobRunner} when the plugin is
 * disabled.
 * </p>
 */
public interface IJobRunner {

  /**
   * Called by the {@link ISchedulerService} when it is time for a job to run. The
   * job is expected to perform its own
   * error handling by catching exceptions as appropriate and reporting an
   * informative message using
   * {@link JobRunnerResponse#failed(String)}.
   *
   * @param request
   *                the information about the request that was supplied by the
   *                scheduler service
   * @return a {@link JobRunnerResponse} providing additional detail about the
   *         result of running the job. The response
   *         is permitted to be {@code null}, which is treated as identical to
   *         {@link JobRunnerResponse#success()}.
   */
  @Nullable
  JobRunnerResponse runJob(@Nonnull IJobRunnerRequest request);
}
