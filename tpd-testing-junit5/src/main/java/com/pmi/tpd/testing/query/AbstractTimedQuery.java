package com.pmi.tpd.testing.query;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.pmi.tpd.testing.query.util.ClockAware;
import com.pmi.tpd.testing.query.util.Clocks;
import com.pmi.tpd.testing.query.util.StringConcat;

/**
 * <p>
 * Abstract query that implements {@link #byDefaultTimeout()} in terms of {@link #by(long)}, and {@link #by(long)} as a
 * template method calling the following hooks (to be implemented by subclasses):
 * <ul>
 * <li>{@link #currentValue()} - to determine current evaluation of the query
 * <li>{@link #shouldReturn(Object)} - which indicates, if current value of the query should be returned
 * </ul>
 * <p>
 * <p>
 * In addition, an {@link ExpirationHandler} must be provided to handle the case of expired query.
 *
 * @see ExpirationHandler
 */
public abstract class AbstractTimedQuery<T> extends AbstractPollingQuery implements TimedQuery<T>, ClockAware {

    private final Clock clock;

    private final Poll<T> poll;

    protected AbstractTimedQuery(final Clock clock, final long defTimeout, final long interval,
            final ExpirationHandler expirationHandler) {
        super(interval, defTimeout);
        this.clock = requireNonNull(clock, "clock");
        this.poll = new Poll<>(this, this.clock()).every((int) interval, TimeUnit.MILLISECONDS)
                .until(new CustomMatcher())
                .onFailure(expirationHandler);
    }

    @Override
    public T call() throws Exception {
        return currentValue();
    }

    class CustomMatcher extends TypeSafeMatcher<T> {

        @Override
        protected boolean matchesSafely(final T item) {
            return AbstractTimedQuery.this.shouldReturn(item);
        }

        @Override
        public void describeTo(final Description description) {

        }
    }

    protected ExpirationHandler expirationHandler() {
        return this.poll.expirationHandler;
    }

    protected AbstractTimedQuery(final long defTimeout, final long interval,
            final ExpirationHandler expirationHandler) {
        this(Clock.systemDefaultZone(), defTimeout, interval, expirationHandler);
    }

    protected AbstractTimedQuery(final PollingQuery other, final ExpirationHandler expirationHandler) {
        this(Clocks.getClock(other), other.defaultTimeout(), requireNonNull(other, "other").interval(),
                expirationHandler);
    }

    /**
     * Checks the condition immediately, once every interval, and finally once after timeout reached. If the condition
     * ever passes, return immediately
     *
     * @param timeout
     *            in milliseconds (ms) to wait for the condition to pass
     */
    @Override
    public final T by(final long timeout) {
        try {
            return poll.withTimeout((int) timeout, TimeUnit.MILLISECONDS).call();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final T by(final long timeout, final TimeUnit unit) {
        return by(TimeUnit.MILLISECONDS.convert(timeout, unit));
    }

    @Override
    public T byDefaultTimeout() {
        return by(defaultTimeout);
    }

    @Override
    public final T now() {
        return by(1, TimeUnit.MILLISECONDS);
    }

    /**
     * If the current evaluated query value should be returned.
     *
     * @param currentEval
     *            current query evaluation expires
     * @return <code>true</code>, if the current query evaluation should be returned as a result of this timed query
     */
    protected abstract boolean shouldReturn(T currentEval);

    /**
     * Current evaluation of the query.
     *
     * @return current evaluation of the query
     */
    protected abstract T currentValue();

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public String toString() {
        return StringConcat
                .asString(getClass().getName(), "[interval=", interval, ",defaultTimeout=", defaultTimeout, "]");
    }
}