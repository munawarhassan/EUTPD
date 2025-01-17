package com.pmi.tpd.metrics;

import java.util.Date;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Immutable class that can be used to hold any arbitrary system measurement value (a named numeric value with a
 * timestamp). For example a metric might record the number of active connections to a server, or the temperature of a
 * meeting room.
 *
 * @param <T>
 *            the value type
 * @author Christophe Friederich
 * @since 1.0
 */
public class Metric<T extends Number> {

    /** */
    private final String name;

    /** */
    private final T value;

    /** */
    private final Date timestamp;

    /**
     * Create a new {@link Metric} instance for the current time.
     *
     * @param name
     *            the name of the metric
     * @param value
     *            the value of the metric
     */
    public Metric(final String name, final T value) {
        this(name, value, new Date());
    }

    /**
     * Create a new {@link Metric} instance.
     *
     * @param name
     *            the name of the metric
     * @param value
     *            the value of the metric
     * @param timestamp
     *            the timestamp for the metric
     */
    public Metric(final String name, final T value, final Date timestamp) {
        Assert.notNull(name, "Name must not be null");
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    /**
     * Returns the name of the metric.
     *
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of the metric.
     *
     * @return the value
     */
    public T getValue() {
        return this.value;
    }

    /**
     * @return
     */
    public Date getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return "Metric [name=" + this.name + ", value=" + this.value + ", timestamp=" + this.timestamp + "]";
    }

    /**
     * Create a new {@link Metric} with an incremented value.
     *
     * @param amount
     *            the amount that the new metric will differ from this one
     * @return a new {@link Metric} instance
     */
    public Metric<Long> increment(final int amount) {
        return new Metric<Long>(this.getName(), Long.valueOf(this.getValue().longValue() + amount));
    }

    /**
     * Create a new {@link Metric} with a different value.
     *
     * @param <S>
     *            the metric value type
     * @param value
     *            the value of the new metric
     * @return a new {@link Metric} instance
     */
    public <S extends Number> Metric<S> set(final S value) {
        return new Metric<S>(this.getName(), value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ObjectUtils.nullSafeHashCode(this.name);
        result = (prime * result) + ObjectUtils.nullSafeHashCode(this.timestamp);
        result = (prime * result) + ObjectUtils.nullSafeHashCode(this.value);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Metric) {
            final Metric<?> other = (Metric<?>) obj;
            boolean rtn = true;
            rtn &= ObjectUtils.nullSafeEquals(this.name, other.name);
            rtn &= ObjectUtils.nullSafeEquals(this.timestamp, other.timestamp);
            rtn &= ObjectUtils.nullSafeEquals(this.value, other.value);
            return rtn;
        }
        return super.equals(obj);
    }

}
