package com.pmi.tpd.api.scheduler;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.scheduler.ILifecycleAwareSchedulerService.State;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ISchedulerServiceController {

  /**
   * Starts the scheduler if it had never been started or had been placed in
   * {@link #standby()} mode. If the scheduler
   * was already active, then the request has no effect.
   *
   * @throws SchedulerServiceException
   *                                   if the scheduler cannot be started
   */
  void start() throws SchedulerServiceException;

  /**
   * Places the scheduler into standby mode. This stops jobs from running until it
   * is {@link #start() started} again.
   * If the scheduler was already in standby, then the request has no effect. The
   * standby mode only affects the local
   * node's ability to schedule jobs. For any
   * {@link com.pmi.tpd.api.scheduler.config.scheduler.config.RunMode#RUN_ONCE_PER_CLUSTER}
   * jobs the jobs may still
   * run on other nodes if they exist and have started. If a job should have run
   * while the scheduler was in standby
   * mode, the implementation may trigger those jobs when restarted, but this is
   * not guaranteed.
   *
   * @throws SchedulerServiceException
   *                                   if the scheduler cannot be be placed in
   *                                   standby mode
   */
  void standby() throws SchedulerServiceException;

  /**
   * Permanent shutdown of the scheduler. Once this has been called, no more jobs
   * will be run and most requests will
   * fail. The scheduler cannot be restarted once it has been shut down.
   */
  void shutdown();

  /**
   * Returns the list of jobs that are currently executing on this node. In a
   * clustered configuration, this will not
   * reflect any jobs that are running on another node of the cluster. This is
   * guaranteed to be safe to call even
   * after a {@link #shutdown()}.
   *
   * @return the job IDs for all jobs that are currently running on this node.
   * @since v1.3
   */
  @Nonnull
  Collection<IRunningJob> getLocallyRunningJobs();

  /**
   * Waits for up to {@code timeout} {@code units} for any currently executing
   * jobs to complete. Note that if the
   * scheduler has not been {@link #shutdown()} or placed in {@link #standby()}
   * mode, then jobs could start after this
   * returns {@code true}. As with {@link #getLocallyRunningJobs()}, this is only
   * aware of jobs running on this node
   * of the cluster, and it is guaranteed to be safe to call even after a
   * {@link #shutdown()}.
   *
   * @param timeout
   *                the timeout period, in the specified units; non-positive
   *                values request an immediate poll &mdash; that
   *                is, it is equivalent to
   *                {@code getLocallyRunningJobs().isEmpty()}
   * @return {@code true} if the scheduler is now idle; {@code false} if jobs are
   *         still executing.
   * @throws InterruptedException
   *                                  if the current thread is interrupted while
   *                                  waiting for the scheduler to become idle.
   * @throws IllegalArgumentException
   *                                  if {@code timeout} is negative
   * @since v1.3
   */
  boolean waitUntilIdle(long timeout, TimeUnit units) throws InterruptedException;

  /**
   * Returns the scheduler service's running state. The scheduler is initially in
   * {@link State#STANDBY} and can be
   * moved between this state and {@link State#STANDBY} freely.
   * {@link State#SHUTDOWN} is terminal &mdash; that is,
   * once shut down, the scheduler's state can no longer be changed.
   *
   * @return the scheduler service's running state.
   */
  @Nonnull
  State getState();
}
