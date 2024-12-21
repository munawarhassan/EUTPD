package com.pmi.tpd.scheduler.quartz;

import java.sql.Connection;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;

import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz breaks if a bad job class is seen during scheduler recovery (at
 * startup), leading to the scheduler failing to
 * start. This patches the normal {@code JobStoreTX} to avoid the problem by
 * logging a warning instead of failing to
 * start the scheduler.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class QuartzHardenedJobStore extends JobStoreTX {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(QuartzHardenedJobStore.class);

  /** */
  private final ReentrantLock recoverJobsLock = new ReentrantLock();

  @Override
  protected void recoverJobs() throws JobPersistenceException {
    try {
      recoverJobsLock.lock();
      super.recoverJobs();
    } finally {
      recoverJobsLock.unlock();
    }
  }

  @Override
  protected void storeTrigger(final Connection conn,
      final OperableTrigger newTrigger,
      @Nullable final JobDetail job,
      final boolean replaceExisting,
      final String state,
      final boolean forceState,
      final boolean recovering) throws JobPersistenceException {
    try {
      super.storeTrigger(conn, newTrigger, job, replaceExisting, state, forceState, recovering);
    } catch (final JobPersistenceException jpe) {
      // We don't actually expect this lock to ever be contended. We just need to know
      // if this thread
      // has traversed "recoverJobs" on the way here, and lock ownership is a
      // convenient way to find out.
      if (!recoverJobsLock.isHeldByCurrentThread()) {
        throw jpe;
      }
      LOGGER.warn("Caught an exception storing trigger during scheduler recovery", jpe);
    }
  }
}
