package com.pmi.tpd.database.bonecp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Driver;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.jolbox.bonecp.BoneCPDataSource;
import com.jolbox.bonecp.ConnectionHandle;
import com.pmi.tpd.api.exec.IForcedDrainable;
import com.pmi.tpd.api.util.IDisposable;
import com.pmi.tpd.database.DelegatingSwappableDataSource;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.config.DefaultDataSourceConfiguration;
import com.pmi.tpd.database.util.StackException;

/**
 * Extends the {@code BoneCPDataSource} to add a constructor which accepts a
 * {@link DefaultDataSourceConfiguration},
 * implement support for {@link #drain(long, TimeUnit) draining} implement
 * {@code Closeable}. The base class already has
 * a {@code close()} method compatible with {@code Closeable}; it just does not
 * implement the interface.
 * <p>
 * To simplify database migration, it is desirable to reuse all of the Spring
 * configuration that is applied to the
 * initial {@code BoneCPDataSource} that is created when the system is started.
 * Only the driver class, credentials and
 * JDBC URL which are used to connect to the database will be different. Adding
 * a constructor which accepts a
 * {@link DefaultDataSourceConfiguration} allows using the Spring
 * {@code ApplicationContext.getBean(String, Object...)}
 * method to override the prototype configured in the Spring context and get
 * pool that uses a different one.
 * <p>
 * Warning: When a bean is retrieved using the
 * {@code getBean(String, Object...)} mechanism, its destroy method, if
 * configured using annotations, will <i>not</i> be called. As a workaround,
 * {@link DelegatingSwappableDataSource} has
 * been annotated with a {@code PreDestroy} method which closes the delegate, if
 * it implements {@code Closeable}.
 */
public class SpringBoneCPDataSource extends BoneCPDataSource implements IDisposable, IForcedDrainable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private static final long DEFAULT_DRAIN_POLL_INTERVAL = TimeUnit.SECONDS.toMillis(2);

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(SpringBoneCPDataSource.class);

  /** */
  private ILeasedConnectionTracker leasedConnectionTracker;

  /** */
  private long drainPollInterval;

  public SpringBoneCPDataSource(final IDataSourceConfiguration configuration) {
    this.drainPollInterval = DEFAULT_DRAIN_POLL_INTERVAL;
    setDriverClass(configuration.getDriverClassName());
    setDriverProperties(configuration.getProperties());
    setJdbcUrl(configuration.getUrl());
    setPassword(configuration.getPassword());
    setUsername(configuration.getUser());
  }

  @PreDestroy
  public void destroy() {
    this.close();
    derbyDestroy();
  }

  @Override
  public void dispose() {
    destroy();
  }

  /**
   * Polls against {@code getTotalLeased()}, waiting for all leased connections to
   * be returned to the pool, for the
   * duration of the specified timeout. This method does <i>nothing</i> to try and
   * force connections to be returned to
   * the pool; that is left to the caller. Additionally, this method does
   * <i>nothing</i> to try and prevent new
   * connections from being leased from the pool after they have all been
   * returned.
   *
   * @param timeout
   *                the timeout which, using the specified unit, will be converted
   *                to milliseconds
   * @param unit
   *                the unit the provided timeout is in
   * @return {@code true} if all leased connections were returned to the pool
   *         before the timeout elapsed; otherwise,
   *         {@code false} to indicate at least one connection is still leased
   */
  @Override
  public boolean drain(final long timeout, @Nonnull final TimeUnit unit) {
    checkArgument(timeout >= 0, "timeout must be non-negative");
    checkNotNull(unit, "unit");

    return drainInterruptibly(timeout, unit) == DrainResult.DRAINED || isDrained();
  }

  @Override
  public boolean forceDrain(final long timeout, @Nonnull final TimeUnit unit) {
    checkArgument(timeout >= 0, "timeout must be non-negative");
    checkNotNull(unit, "unit");

    if (isDrained()) {
      return true;
    }

    LOGGER.info("Force draining the connection pool");

    LOGGER.debug("{} connections still leased. Owning threads will be interrupted with a {} {} delay",
        getTotalLeased(),
        timeout,
        unit);

    interruptThreadsWithConnections();

    switch (drainInterruptibly(timeout, unit)) {
      case INTERRUPTED:
        return isDrained();
      case DRAINED:
        return true;
      case TIMED_OUT:
        break;
      default:
        break;
    }

    LOGGER.debug("{} connections still leased; all leased connections will now be rolled back and closed",
        getTotalLeased());

    forceRollbackAndCloseConnections();

    final int totalLeased = getTotalLeased();
    LOGGER.info("{} connections still leased; forced draining has {}",
        totalLeased,
        totalLeased == 0 ? "succeeded" : "failed");
    return totalLeased == 0;
  }

  /**
   * Delegating implementation of {@code CommonDataSource.getParentLogger()},
   * which is a new method added in Java 7 as
   * part of JDBC 4.1.
   * <p>
   * Note: This shows up as unused when developing against a Java 6 JDK, but is
   * required when developing against a
   * Java 7 JDK. Because of this, it cannot be marked as {@code @Override}.
   *
   * @return the parent logger
   */
  @Override
  public java.util.logging.Logger getParentLogger() {
    return java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
  }

  /**
   * Sets the maximum number of <i>seconds</i> to wait while attempting to obtain
   * a connection.
   * <p>
   * Note: By default, the timeout is <i>infinite</i>.
   *
   * @param connectionTimeout
   *                          the connection timeout, in seconds
   */
  public void setConnectionTimeoutInSeconds(final int connectionTimeout) {
    setConnectionTimeoutInMs(TimeUnit.SECONDS.toMillis(connectionTimeout));
  }

  /**
   * Sets the interval, in milliseconds, which will be waited in between polling
   * the connection pool for the current
   * count of leased connections while {@link #drain(long, TimeUnit) draining}.
   *
   * @param drainPollInterval
   *                          the interval between leased connection checks, in
   *                          <b>milliseconds</b>
   */
  @VisibleForTesting
  void setDrainPollInterval(final long drainPollInterval) {
    this.drainPollInterval = drainPollInterval;
  }

  /**
   * @param leasedConnectionTracker
   */
  public void setLeasedConnectionTracker(final ILeasedConnectionTracker leasedConnectionTracker) {
    this.leasedConnectionTracker = leasedConnectionTracker;
  }

  private void forceRollbackAndCloseConnections() {
    if (leasedConnectionTracker == null) {
      return;
    }
    for (final ConnectionHandle leased : leasedConnectionTracker.getLeased()) {
      final Thread lessee = leased.getThreadUsingConnection();
      final String threadName = threadName(lessee);

      // errors we encounter
      try {
        LOGGER.info("Rolling back database connection in use by thread \"{}\"",
            threadName,
            lessee != null && LOGGER.isDebugEnabled() ? new StackException(lessee) : null);
        leased.rollback();
      } catch (final Exception e) {
        // Debug level because it's probably not an error but a race condition
        LOGGER.debug("Failed to roll back database connection in use by thread \"{}\"", threadName, e);
      }
      try {
        if (!leased.isClosed()) {
          LOGGER.info("Closing database connection in use by thread \"{}\"", threadName);
          leased.close();
        }
      } catch (final Exception e) {
        // Debug level because it's probably not an error but a race condition
        LOGGER.debug("Failed to close database connection in use by thread \"{}\"", threadName, e);
      }
    }
  }

  private void interruptThreadsWithConnections() {
    for (final ConnectionHandle leased : leasedConnectionTracker.getLeased()) {
      final Thread lessee = leased.getThreadUsingConnection();

      // The connection may have been freed by the time we get here
      if (lessee != null) {
        LOGGER.debug(
            "Thread \"{}\" is holding onto a database connection. It is delaying the pool from draining",
            threadName(lessee),
            new StackException(lessee));
        lessee.interrupt();
      }
    }
  }

  private boolean isDrained() {
    return getTotalLeased() == 0;
  }

  private DrainResult drainInterruptibly(final long timeout, @Nonnull final TimeUnit unit) {
    final long start = System.currentTimeMillis();
    final long end = start + unit.toMillis(timeout);

    LOGGER.debug("Draining the connection pool");
    for (int leased = getTotalLeased(); leased > 0; leased = getTotalLeased()) {
      final long tilEnd = end - System.currentTimeMillis();
      final long interval = Math.min(drainPollInterval, tilEnd);

      if (tilEnd <= 0) {
        LOGGER.debug("The connection pool did not drain in {} {}; {} connections are still leased",
            timeout,
            unit,
            leased);

        return DrainResult.TIMED_OUT;
      }

      LOGGER.debug("{} connections still leased; waiting {} milliseconds", leased, interval);
      try {
        Thread.sleep(interval);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        LOGGER.debug("Interrupted while waiting for the connection pool to drain");
        return DrainResult.INTERRUPTED;
      }
    }

    LOGGER.debug("The connection pool has drained in {} milliseconds", System.currentTimeMillis() - start);
    return DrainResult.DRAINED;
  }

  private String threadName(final Thread thread) {
    return thread == null ? "<unknown>" : thread.getName();
  }

  private enum DrainResult {
    DRAINED,
    TIMED_OUT,
    INTERRUPTED
  }

  private void derbyDestroy() {
    if ("org.apache.derby.jdbc.EmbeddedDriver".equals(getDriverClass())) {
      try {
        String url = getJdbcUrl();
        if (url.contains(";")) {
          url = url.substring(0, url.indexOf(";")) + ";shutdown=true";
        } else {
          url += ";shutdown=true";
        }
        LOGGER.info("Shutting down derby connection: " + url);
        // this cleans up the lock files in the embedded derby database folder
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final Class<?> cl = contextClassLoader.loadClass(getDriverClass());
        final Driver driver = (Driver) cl.getConstructor().newInstance();
        driver.connect(url, null);
      } catch (final Exception e) {
        // its too late
      }
    }
  }
}
