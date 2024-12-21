package com.pmi.tpd.testing.query;

public interface PollingQuery {

    public static final long DEFAULT_INTERVAL = 100L;

    /**
     * An interval (in milliseconds) that will be used to periodically evaluate the query.
     *
     * @return evaluation interval of this query.
     */
    long interval();

    /**
     * Default timeout (in milliseconds) of this query in the current test context.
     *
     * @return default timeout of this query
     */
    default long defaultTimeout() {
        return DEFAULT_INTERVAL;
    }
}