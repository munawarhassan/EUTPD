package com.pmi.tpd.testing.query;

import java.time.Clock;

import com.pmi.tpd.testing.query.util.ClockAware;
import com.pmi.tpd.testing.query.util.Clocks;

/**
 * Abstract timed condition based on {@link com.pmi.pageobjects.elements.query.AbstractTimedQuery}. Override
 * {@link #currentValue()} to complete implementation.
 */
public abstract class AbstractTimedCondition extends AbstractTimedQuery<Boolean> implements TimedCondition, ClockAware {

    protected AbstractTimedCondition(final Clock clock, final long defTimeout, final long interval) {
        super(clock, defTimeout, interval, ExpirationHandler.RETURN_CURRENT);
    }

    protected AbstractTimedCondition(final long defTimeout, final long interval) {
        this(Clock.systemDefaultZone(), defTimeout, interval);
    }

    protected AbstractTimedCondition(final PollingQuery other) {
        this(Clocks.getClock(other), other.defaultTimeout(), other.interval());
    }

    @Override
    protected final boolean shouldReturn(final Boolean currentEval) {
        return currentEval;
    }
}