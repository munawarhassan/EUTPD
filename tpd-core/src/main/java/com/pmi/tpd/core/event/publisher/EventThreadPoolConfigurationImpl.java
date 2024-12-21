package com.pmi.tpd.core.event.publisher;

import java.util.concurrent.TimeUnit;

import com.pmi.tpd.core.event.config.IEventThreadPoolConfiguration;

/**
 * Default Thread pool configuration.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class EventThreadPoolConfigurationImpl implements IEventThreadPoolConfiguration {

    /** */
    private static final int CORE_POOL_SIZE = 16;

    /** */
    private static final int MAXIMUM_POOL_SIZE = 64;

    /** */
    private static final long KEEP_ALIVE_TIME = 60L;

    /** {@inheritDoc} */
    @Override
    public int getCorePoolSize() {
        return CORE_POOL_SIZE;
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximumPoolSize() {
        return MAXIMUM_POOL_SIZE;
    }

    /** {@inheritDoc} */
    @Override
    public long getKeepAliveTime() {
        return KEEP_ALIVE_TIME;
    }

    /** {@inheritDoc} */
    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }
}
