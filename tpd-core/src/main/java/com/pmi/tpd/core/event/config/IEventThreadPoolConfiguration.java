package com.pmi.tpd.core.event.config;

import java.util.concurrent.TimeUnit;

/**
 * A configuration object for thread pools used by asynchronous event dispatchers.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IEventThreadPoolConfiguration {

    /**
     * Gets the size of the thread pool.
     *
     * @return Returns the number the thread in the pool.
     */
    int getCorePoolSize();

    /**
     * Gets the maximum size of the thread pool.
     *
     * @return Returns the maximum number of thread accepted in the pool
     */
    int getMaximumPoolSize();

    /**
     * Gets the number of time (unit depending {@link #getTimeUnit()}) the thread keep alive in the pool.
     *
     * @return Returns the the number of time (unit depending {@link #getTimeUnit()}) the thread keep alive in the pool.
     * @see #getTimeUnit()
     */
    long getKeepAliveTime();

    /**
     * Gets the time unit for the {@code #getKeepAliveTime()} method.
     *
     * @return Returns the time unit for the {@code #getKeepAliveTime()} method.
     */
    TimeUnit getTimeUnit();
}
