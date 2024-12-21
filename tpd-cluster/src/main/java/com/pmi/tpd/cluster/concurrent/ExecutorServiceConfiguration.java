package com.pmi.tpd.cluster.concurrent;

import com.pmi.tpd.cluster.util.PropertiesUtils;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class ExecutorServiceConfiguration {

    /** */
    private static final int MIN_POOL_SIZE = 4;

    /** */
    private final int corePoolSize;

    public ExecutorServiceConfiguration(final String coreThreads) {
        corePoolSize = Math.max(MIN_POOL_SIZE,
            PropertiesUtils.parseExpression(coreThreads, Runtime.getRuntime().availableProcessors()));
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

}
