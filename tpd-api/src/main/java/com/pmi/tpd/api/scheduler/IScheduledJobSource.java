package com.pmi.tpd.api.scheduler;

import javax.annotation.Nonnull;

/**
 * Describes a component which provides one or more scheduled jobs to be
 * registered with the {@code ISchedulerService}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IScheduledJobSource {

  /**
   * Registers job runners and schedules jobs.
   *
   * @param schedulerService
   *                         the scheduler to register jobs and runners with
   * @throws SchedulerServiceException
   *                                   if the {@code SchedulerService} throws an
   *                                   exception
   */
  void schedule(@Nonnull ISchedulerService schedulerService) throws SchedulerServiceException;

  /**
   * Unregisters job runners. In general implementations should <i>not</i>
   * unschedule jobs. Unscheduling a job applies
   * to all nodes in the cluster, for {@code RunMode.RUN_ONCE_PER_CLUSTER}, and
   * shutting down a single node should not
   * prevent the remaining nodes from continuing to run jobs. For
   * simplicity/consistency even
   * {@code RunMode.RUN_LOCALLY} jobs should not be unregistered; they will be
   * garbage collected after the application
   * has shutdown.
   *
   * @param schedulerService
   * @throws SchedulerServiceException
   */
  void unschedule(@Nonnull ISchedulerService schedulerService) throws SchedulerServiceException;
}
