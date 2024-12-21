package com.pmi.tpd.testing.query;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;

import java.util.function.Supplier;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.pmi.tpd.testing.query.util.ArrayUtil;
import com.pmi.tpd.testing.query.util.StringConcat;

/**
 * Utilities to create miscellaneous {@link TimedCondition}s.
 */
public final class Conditions {

    private static final Logger log = LoggerFactory.getLogger(Conditions.class);

    private static final int DEFAULT_TIMEOUT = 100;

    private Conditions() {
        throw new AssertionError("No way");
    }

    /**
     * Return new timed condition that is a negation of <tt>condition</tt>.
     *
     * @param condition
     *            condition to be negated
     * @return negated {@link TimedCondition} instance.
     */
    public static TimedQuery<Boolean> not(final TimedQuery<Boolean> condition) {
        if (condition instanceof Not) {
            return asDecorator(condition).wrapped;
        }
        return new Not(condition);
    }

    /**
     * <p>
     * Return new combinable condition that is logical product of <tt>conditions</tt>.
     * <p>
     * The resulting condition will have interval of the first condition in the <tt>conditions</tt> array,
     *
     * @param conditions
     *            conditions to conjoin
     * @return product of <tt>conditions</tt>
     * @throws IllegalArgumentException
     *             if <tt>conditions</tt> array is <code>null</code> or empty
     * @see TimedCondition#interval()
     */
    @SafeVarargs
    public static CombinableCondition and(final TimedQuery<Boolean>... conditions) {
        return new And(conditions);
    }

    /**
     * <p>
     * Return new combinable condition that is logical product of <tt>conditions</tt>.
     * <p>
     * The resulting condition will have interval of the first condition in the <tt>conditions</tt> array,
     *
     * @param conditions
     *            conditions to conjoin
     * @return product of <tt>conditions</tt>
     * @throws IllegalArgumentException
     *             if <tt>conditions</tt> array is <code>null</code> or empty
     * @see TimedCondition#interval()
     */
    @SuppressWarnings("unchecked")
    public static CombinableCondition and(final Iterable<TimedQuery<Boolean>> conditions) {
        return and(Iterables.toArray(conditions, TimedQuery.class));
    }

    /**
     * <p>
     * Return new combinable condition that is logical sum of <tt>conditions</tt>.
     * <p>
     * The resulting condition will have interval of the first condition in the <tt>conditions</tt> array,
     *
     * @param conditions
     *            conditions to sum
     * @return logical sum of <tt>conditions</tt>
     * @throws IllegalArgumentException
     *             if <tt>conditions</tt> array is <code>null</code> or empty
     * @see TimedCondition#interval()
     */
    @SafeVarargs
    public static CombinableCondition or(final TimedQuery<Boolean>... conditions) {
        return new Or(conditions);
    }

    /**
     * <p>
     * Return new combinable condition that is logical sum of <tt>conditions</tt>.
     * <p>
     * The resulting condition will have interval of the first condition in the <tt>conditions</tt> array,
     *
     * @param conditions
     *            conditions to sum
     * @return logical sum of <tt>conditions</tt>
     * @throws IllegalArgumentException
     *             if <tt>conditions</tt> array is <code>null</code> or empty
     * @see TimedCondition#interval()
     */
    @SuppressWarnings("unchecked")
    public static CombinableCondition or(final Iterable<TimedQuery<Boolean>> conditions) {
        return or(Iterables.toArray(conditions, TimedQuery.class));
    }

    /**
     * <p>
     * Returns a condition that combines <tt>original</tt> and <tt>dependant</tt> in a manner that dependant condition
     * will only ever be retrieved if the <tt>original</tt> condition is <code>true</code>. This is useful when
     * dependant condition may only be retrieved given the original condition is <code>true</code>.
     * </p>
     * <p>
     * The supplier for dependant condition is allowed to return <code>null</code> or throw exception if the original
     * condition returns false. But it <i>may not</i> do so given the original condition is <code>true</code>, as this
     * will lead to <code>NullPointerException</code> or the raised exception be propagated by this condition
     * respectively.
     * </p>
     *
     * @param original
     *            original condition
     * @param dependant
     *            supplier for dependant condition that will only be evaluated given the original condition evaluates to
     *            <code>true</code>
     * @return new dependant condition
     */
    public static TimedCondition dependantCondition(final TimedQuery<Boolean> original,
        final Supplier<TimedQuery<Boolean>> dependant) {
        return new DependantCondition(original, dependant);
    }

    /**
     * <p>
     * Return condition that will be <code>true</code>, if given <tt>matcher</tt> will match the <tt>query</tt>. Any
     * Hamcrest matcher implementation may be used.
     * </p>
     * <p>
     * Example:<br>
     * <code>
     *     TimedCondition textEquals = Conditions.forMatcher(element.getText(), isEqualTo("blah"));
     * </code>
     * </p>
     *
     * @param query
     *            timed query to match
     * @param matcher
     *            matcher for the query
     * @param <T>
     *            type of the result
     * @return new matching condition
     */
    public static <T> TimedCondition forMatcher(final TimedQuery<T> query, final Matcher<? super T> matcher) {
        return new MatchingCondition<>(query, matcher);
    }

    /**
     * Returns timed condition verifying that given query will evaluate to value equal to <tt>value</tt>. The timeouts
     * are inherited from the provided <tt>query</tt>
     *
     * @param value
     *            value that <tt>query</tt> should be equalt to
     * @param query
     *            the timed query
     * @param <T>
     *            type of the value
     * @return timed condition for query equality to value
     */
    public static <T> TimedCondition isEqual(final T value, final TimedQuery<T> query) {
        return forMatcher(query, equalTo(value));
    }

    /**
     * Returns a timed condition, whose current evaluation is based on a value provided by given <tt>supplier</tt>.
     *
     * @param supplier
     *            supplier of the current condition value
     * @return new query based on supplier
     */
    public static TimedCondition forSupplier(final Supplier<Boolean> supplier) {
        return forSupplier(DEFAULT_TIMEOUT, supplier);
    }

    /**
     * Returns a timed condition, whose current evaluation is based on a value provided by given <tt>supplier</tt>.
     *
     * @param defaultTimeout
     *            default timeout of the condition
     * @param supplier
     *            supplier of the current condition value
     * @return new query based on supplier
     */
    public static TimedCondition forSupplier(final long defaultTimeout, final Supplier<Boolean> supplier) {
        return new AbstractTimedCondition(defaultTimeout, PollingQuery.DEFAULT_INTERVAL) {

            @Override
            protected Boolean currentValue() {
                return supplier.get();
            }
        };
    }

    public static TimedCondition forSupplier(final Duration defaultTimeout,
        final Duration interval,
        final Supplier<Boolean> supplier) {
        return new AbstractTimedCondition(defaultTimeout.toMillis(), interval.toMillis()) {

            @Override
            protected Boolean currentValue() {
                return supplier.get();
            }
        };
    }

    /**
     * Returns a timed condition, whose current evaluation is based on a value provided by given <tt>supplier</tt>.
     *
     * @param defaultTimeout
     *            default timeout (in milliseconds) of the condition
     * @param interval
     *            interval time (in milliseconds) that will be used to periodically evaluate the query.
     * @param supplier
     *            supplier of the current condition value
     * @return new query based on supplier
     */
    public static TimedCondition forSupplier(final long defaultTimeout,
        final long interval,
        final Supplier<Boolean> supplier) {
        return new AbstractTimedCondition(defaultTimeout, interval) {

            @Override
            protected Boolean currentValue() {
                return supplier.get();
            }
        };
    }

    /**
     * Returns a timed condition, whose current evaluation is based on a value provided by given <tt>supplier</tt>.
     *
     * @param timeouts
     *            an instance of timeouts (in milliseconds) to use for the new condition
     * @param supplier
     *            supplier of the current condition value
     * @return new query based on supplier
     */
    public static TimedCondition forSupplier(final Timeouts timeouts, final Supplier<Boolean> supplier) {
        return new AbstractTimedCondition(timeouts.timeoutFor(TimeoutType.DEFAULT),
                timeouts.timeoutFor(TimeoutType.EVALUATION_INTERVAL)) {

            @Override
            protected Boolean currentValue() {
                return supplier.get();
            }
        };
    }

    /**
     * A timed condition that always returns <code>true</code>
     *
     * @return timed condition that always returns true
     */
    public static TimedCondition alwaysTrue() {
        return new StaticCondition(true);
    }

    /**
     * A timed condition that always returns <code>false</code>
     *
     * @return timed condition that always returns false
     */
    public static TimedCondition alwaysFalse() {
        return new StaticCondition(false);
    }

    private static AbstractConditionDecorator asDecorator(final TimedQuery<Boolean> condition) {
        return (AbstractConditionDecorator) condition;
    }

    private static class StaticCondition extends AbstractTimedCondition {

        private final Boolean value;

        public StaticCondition(final Boolean value) {
            super(DEFAULT_TIMEOUT, DEFAULT_INTERVAL);
            this.value = requireNonNull(value);
        }

        @Override
        protected Boolean currentValue() {
            return value;
        }
    }

    /**
     * A timed condition that may be logically combined with others, by means of basic logical operations: 'and'/'or'.
     */
    public static interface CombinableCondition extends TimedCondition {

        /**
         * Combine <tt>other</tt> condition with this condition logical query, such that the resulting condition
         * represents a logical product of this condition and <tt>other</tt>.
         *
         * @param other
         *            condition to combine with this one
         * @return new combined 'and' condition
         */
        CombinableCondition and(TimedCondition other);

        /**
         * Combine <tt>other</tt> condition with this condition logical query, such that the resulting condition
         * represents a logical sum of this condition and <tt>other</tt>.
         *
         * @param other
         *            condition to combine with this one
         * @return new combined 'or' condition
         */
        CombinableCondition or(TimedCondition other);

    }

    private abstract static class AbstractConditionDecorator extends AbstractTimedCondition {

        protected final TimedQuery<Boolean> wrapped;

        public AbstractConditionDecorator(final TimedQuery<Boolean> wrapped) {
            super(wrapped);
            this.wrapped = requireNonNull(wrapped, "wrapped");
        }
    }

    private abstract static class AbstractConditionsDecorator extends AbstractTimedCondition
            implements CombinableCondition {

        protected final TimedQuery<Boolean>[] conditions;

        @SafeVarargs
        public AbstractConditionsDecorator(final TimedQuery<Boolean>... conditions) {
            super(conditions[0]);
            this.conditions = conditions;
        }

        @Override
        public String toString() {
            final StringBuilder answer = new StringBuilder(conditions.length * 20).append(getClass().getName())
                    .append(":\n");
            for (final TimedQuery<Boolean> condition : conditions) {
                answer.append(" -").append(condition.toString()).append('\n');
            }
            return answer.deleteCharAt(answer.length() - 1).toString();
        }
    }

    private static class Not extends AbstractConditionDecorator {

        public Not(final TimedQuery<Boolean> other) {
            super(other);
        }

        @Override
        public Boolean currentValue() {
            return !wrapped.now();
        }

        @Override
        public String toString() {
            return StringConcat.asString("Negated: <", wrapped, ">");
        }
    }

    private static class And extends AbstractConditionsDecorator {

        @SafeVarargs
        public And(final TimedQuery<Boolean>... conditions) {
            super(conditions);
        }

        And(final TimedQuery<Boolean>[] somes, final TimedQuery<Boolean>[] more) {
            super(ArrayUtil.merge(somes, more));
        }

        And(final TimedQuery<Boolean>[] somes, final TimedQuery<Boolean> oneMore) {
            super(ArrayUtil.add(somes, oneMore));
        }

        @Override
        public Boolean currentValue() {
            boolean result = true;
            for (final TimedQuery<Boolean> condition : conditions) {
                // null should not really happen if TimedCondition contract is observed
                final boolean next = condition.now() != null ? condition.now() : false;
                result &= next;
                if (!result) {
                    if (log.isDebugEnabled()) {
                        log.debug(StringConcat.asString("[And] Condition <", condition, "> returned false"));
                    }
                    break;
                }
            }
            return result;
        }

        @Override
        public CombinableCondition and(final TimedCondition other) {
            if (other.getClass().equals(And.class)) {
                return new And(this.conditions, ((And) other).conditions);
            }
            return new And(this.conditions, other);
        }

        @Override
        public CombinableCondition or(final TimedCondition other) {
            if (other instanceof Or) {
                return ((Or) other).or(this);
            }
            return new Or(this, other);
        }
    }

    private static class Or extends AbstractConditionsDecorator {

        @SafeVarargs
        public Or(final TimedQuery<Boolean>... conditions) {
            super(conditions);
        }

        Or(final TimedQuery<Boolean>[] somes, final TimedQuery<Boolean>[] more) {
            super(ArrayUtil.merge(somes, more));
        }

        Or(final TimedQuery<Boolean>[] somes, final TimedQuery<Boolean> oneMore) {
            super(ArrayUtil.add(somes, oneMore));
        }

        @Override
        public Boolean currentValue() {
            boolean result = false;
            for (final TimedQuery<Boolean> condition : conditions) {
                // null should not really happen if TimedCondition contract is observed
                final boolean next = condition.now() != null ? condition.now() : false;
                result |= next;
                if (result) {
                    break;
                }
                if (log.isDebugEnabled()) {
                    log.debug(StringConcat.asString("[Or] Condition <", condition, "> returned false"));
                }
            }
            return result;
        }

        @Override
        public CombinableCondition and(final TimedCondition other) {
            if (other instanceof And) {
                return ((And) other).and(this);
            }
            return new And(this, other);
        }

        @Override
        public CombinableCondition or(final TimedCondition other) {
            if (other.getClass().equals(Or.class)) {
                return new Or(this.conditions, ((Or) other).conditions);
            }
            return new Or(this.conditions, other);
        }

    }

    private static final class DependantCondition extends AbstractConditionDecorator {

        private final Supplier<TimedQuery<Boolean>> dependant;

        DependantCondition(final TimedQuery<Boolean> original, final Supplier<TimedQuery<Boolean>> dependant) {
            super(original);
            this.dependant = requireNonNull(dependant, "dependant");
        }

        @Override
        public Boolean currentValue() {
            return wrapped.now() && dependant.get().now();
        }

        @Override
        public String toString() {
            if (wrapped.now()) {
                final TimedQuery<Boolean> dep = dependant.get();
                return StringConcat.asString("DependantCondition[original=", wrapped, ",dependant=", dep, "]");
            }
            return StringConcat.asString("DependantCondition[original=", wrapped, "]");
        }
    }

    static final class MatchingCondition<T> extends AbstractTimedCondition {

        final TimedQuery<T> query;

        final Matcher<? super T> matcher;

        T lastValue;

        public MatchingCondition(final TimedQuery<T> query, final Matcher<? super T> matcher) {
            super(query);
            this.query = requireNonNull(query);
            this.matcher = requireNonNull(matcher);
        }

        @Override
        protected Boolean currentValue() {
            try {
                lastValue = query.now();
                return matcher.matches(lastValue);
            } catch (final Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("TimedQuery.now() threw an exception. Not a match for %s", matcher), e);
                }
                return false;
            }
        }

        @Override
        public String toString() {
            return super.toString() + new StringDescription().appendText("[query=")
                    .appendValue(query)
                    .appendText("][matcher=")
                    .appendDescriptionOf(matcher)
                    .appendText("]");
        }
    }

}