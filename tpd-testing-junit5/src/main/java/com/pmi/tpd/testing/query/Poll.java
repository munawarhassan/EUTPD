package com.pmi.tpd.testing.query;

import static org.hamcrest.Matchers.notNullValue;

import java.time.Clock;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.testing.query.util.Backoff;
import com.pmi.tpd.testing.query.util.LinearBackoff;

public class Poll<T> implements Callable<T> {

    Callable<T> query;

    Matcher<? super T> condition;

    Backoff interval;

    long timeout;

    Clock clock;

    ExpirationHandler expirationHandler;

    @VisibleForTesting
    public Poll(final Callable<T> query, final Clock clock) {
        this(query);
        this.clock = clock;
    }

    private Poll(final Callable<T> query) {
        this.query = query;
        this.condition = notNullValue();
        this.interval = new LinearBackoff(50);
        this.timeout = 10000;
        this.clock = Clock.systemDefaultZone();
        this.expirationHandler = ExpirationHandler.RETURN_CURRENT;
    }

    public static <T> Poll<T> poll(final Callable<T> query) {
        return new Poll<>(query);
    }

    public Poll<T> every(final long interval, final TimeUnit unit) {
        this.interval = new LinearBackoff(TimeUnit.MILLISECONDS.convert(interval, unit));
        return this;
    }

    public Poll<T> until(final Matcher<? super T> condition) {
        this.condition = condition;
        return this;
    }

    public Poll<T> withTimeout(final long time, final TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException("Timeout must be a positive value.");
        }
        this.timeout = TimeUnit.MILLISECONDS.convert(time, unit);
        return this;
    }

    public Poll<T> onFailure(final ExpirationHandler expirationHandler) {
        this.expirationHandler = expirationHandler;
        return this;
    }

    @Override
    public T call() throws RuntimeException {
        try {
            final long start = clock.millis();
            final long deadline = start + timeout;
            T current;

            while (clock.millis() < deadline) {
                current = query.call();
                if (condition.matches(current)) {
                    return current;
                } else {
                    final long bounded = Math.max(1, deadline - clock.millis());
                    interval.yield(bounded);
                }
            }
            interval.resetBackoff();

            current = query.call();
            if (condition.matches(current)) {
                return current;
            }

            return expirationHandler.expired(query.toString(), current, timeout);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
