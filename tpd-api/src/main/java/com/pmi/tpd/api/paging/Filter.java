package com.pmi.tpd.api.paging;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * Filter class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class Filter<T> {

    public enum Cardinality {
        unary,
        binary,
        nary
    }

    /**
     * @author Christophe Friederich
     */
    public enum Operator {

        /** */
        eq(Cardinality.unary),
        /** */
        noteq(Cardinality.unary),
        /** */
        contains(Cardinality.unary),
        /** */
        start(Cardinality.unary),
        /** */
        end(Cardinality.unary),
        /** */
        lt(Cardinality.unary),
        /** */
        lte(Cardinality.unary),
        /** */
        gt(Cardinality.unary),
        /** */
        gte(Cardinality.unary),
        /** */
        exists(Cardinality.unary),
        /** */
        between(Cardinality.binary),
        /** */
        before(Cardinality.unary),
        /** */
        after(Cardinality.unary),
        /** */
        in(Cardinality.nary),
        /** */
        notin(Cardinality.nary);

        private final Cardinality cardinality;

        private Operator(final Cardinality cardinality) {
            this.cardinality = cardinality;
        }

        public Cardinality getCardinality() {
            return cardinality;
        }

        @JsonValue
        public String getOperator() {
            return this.name();
        }

        @JsonCreator
        public static Operator from(final String value) {
            return Operator.valueOf(value);
        }

    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> eq(final String property, final T value) {
        return new Filter<>(property, value, Operator.eq);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> notEq(final String property, final T value) {
        return new Filter<>(property, value, Operator.noteq);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> in(final String property, final T[] values) {
        return new Filter<>(property, Arrays.asList(values), Operator.in);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> in(final String property, final Collection<T> values) {
        return new Filter<>(property, values, Operator.in);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> notIn(final String property, final T[] values) {
        return new Filter<>(property, Arrays.asList(values), Operator.notin);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> notIn(final String property, final Collection<T> values) {
        return new Filter<>(property, values, Operator.notin);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> contains(final String property, final T value) {
        return new Filter<>(property, value, Operator.contains);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> start(final String property, final T value) {
        return new Filter<>(property, value, Operator.start);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> end(final String property, final T value) {
        return new Filter<>(property, value, Operator.end);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> lte(final String property, final T value) {
        return new Filter<>(property, value, Operator.lte);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> gte(final String property, final T value) {
        return new Filter<>(property, value, Operator.gte);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> lt(final String property, final T value) {
        return new Filter<>(property, value, Operator.lt);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> gt(final String property, final T value) {
        return new Filter<>(property, value, Operator.gt);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> exists(final String property, final T value) {
        return new Filter<>(property, value, Operator.exists);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> before(final String property, final T value) {
        return new Filter<>(property, value, Operator.before);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> after(final String property, final T value) {
        return new Filter<>(property, value, Operator.after);
    }

    /**
     * @param property
     * @param value
     * @return
     */
    public static <T> Filter<T> between(@Nonnull final String property, @Nullable final T from, @Nullable final T to) {
        return new Filter<>(property, Arrays.asList(from, to), Operator.between);
    }

    /** */
    private String property;

    /** */
    private final Operator operator;

    /** */
    private List<T> values;

    /**
     * <p>
     * Constructor for Filter.
     * </p>
     *
     * @param property
     *                 a {@link java.lang.String} object.
     * @param value
     *                 a {@link java.lang.Object} object.
     */
    public Filter(@Nonnull final String property, @Nullable final T value) {
        this(property, value, null);
    }

    public Filter(@Nonnull final String property, @Nullable final T value, @Nullable final Operator operator) {
        this(property, value == null ? null : Arrays.asList(value), operator);
    }

    /**
     * @param property
     * @param value
     * @param operator
     */
    public Filter(@Nonnull final String property, @Nullable final Collection<T> values,
            @Nullable final Operator operator) {
        this.property = Assert.hasText(property);
        this.values = values == null ? Collections.emptyList() : Lists.newArrayList(values);
        this.operator = operator == null ? Operator.eq : operator;
    }

    /**
     * <p>
     * Getter for the field <code>property</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Nonnull
    public String getProperty() {
        return property;
    }

    /**
     * <p>
     * Getter for the field <code>value</code>.
     * </p>
     *
     * @return a {@link java.lang.Object} object.
     */
    @Nonnull
    public Optional<T> getValue() {
        if (size() >= 1) {
            return Optional.ofNullable(values.get(0));
        }
        return Optional.empty();
    }

    public Optional<T> from() {
        return getValue();
    }

    public Optional<T> to() {
        if (values.size() >= 2) {
            return Optional.ofNullable(values.get(1));
        }
        return Optional.empty();
    }

    public List<T> getValues() {
        return values;
    }

    public int size() {
        return values.size();
    }

    public boolean isEmptyOrNull() {
        if (values.isEmpty()) {
            return true;
        }
        return values.stream().allMatch(v -> v == null);
    }

    public Operator getOperator() {
        return operator;
    }

    /**
     * <p>
     * Setter for the field <code>property</code>.
     * </p>
     *
     * @param property
     *                 a {@link java.lang.String} object.
     */
    public void setProperty(final String property) {
        this.property = property;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
