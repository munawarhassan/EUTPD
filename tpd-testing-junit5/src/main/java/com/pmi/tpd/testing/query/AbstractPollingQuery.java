package com.pmi.tpd.testing.query;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.hamcrest.StringDescription;

/**
 * Abstract implementation of the {@link PollingQuery} interface.
 */
public class AbstractPollingQuery implements PollingQuery {

    protected final long interval;

    protected final long defaultTimeout;

    protected AbstractPollingQuery(final long interval, final long defaultTimeout) {
        checkArgument(interval > 0,
            new StringDescription().appendText("interval is ")
                    .appendValue(interval)
                    .appendText(" should be > 0")
                    .toString());
        checkArgument(defaultTimeout > 0,
            new StringDescription().appendText("defaultTimeout is ")
                    .appendValue(defaultTimeout)
                    .appendText(" should be > 0")
                    .toString());
        checkArgument(defaultTimeout >= interval,
            new StringDescription().appendText("defaultTimeout is ")
                    .appendValue(defaultTimeout)
                    .appendText(" interval is ")
                    .appendValue(interval)
                    .appendText(" defaultTimeout should be >= interval")
                    .toString());
        this.interval = interval;
        this.defaultTimeout = defaultTimeout;
    }

    protected AbstractPollingQuery(final PollingQuery other) {
        this(requireNonNull(other, "other").interval(), other.defaultTimeout());
    }

    @Override
    public long interval() {
        return interval;
    }

    @Override
    public long defaultTimeout() {
        return defaultTimeout;
    }
}
