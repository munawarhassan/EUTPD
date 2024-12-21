package com.pmi.tpd.database.liquibase;

import java.sql.Clob;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import liquibase.change.ColumnConfig.ValueNumeric;
import liquibase.util.ISODateFormat;

/**
 * An enum of the column types that are serialised to XML when a application instance is backed up.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum ColumnSerialisationType {

    BOOLEAN("boolean"),
    DATE("date") {

        @Override
        @Nonnull
        public String toColumnString(@Nonnull final Object value) {
            return new ISODateFormat().format((Date) value);
        }
    },
    INTEGER("integer"),
    BIGINT("bigint"),
    NUMERIC("numeric"),
    CLOB("clob"),
    CHARACTER("character");

    private static final Map<String, ColumnSerialisationType> STRING_TO_COLUMN_TYPE;

    static {
        final ImmutableMap.Builder<String, ColumnSerialisationType> builder = ImmutableMap.builder();
        for (final ColumnSerialisationType columnSerialisationType : values()) {
            builder.put(columnSerialisationType.stringValue, columnSerialisationType);
        }
        STRING_TO_COLUMN_TYPE = builder.build();
    }

    public static Optional<ColumnSerialisationType> fromString(final String s) {
        return Optional.ofNullable(STRING_TO_COLUMN_TYPE.get(s));
    }

    public static ColumnSerialisationType ofValue(final Object value) {
        if (value instanceof Long) {
            return BIGINT;
        }
        if (value instanceof Integer) {
            return INTEGER;
        }
        if (value instanceof Number) {
            return NUMERIC;
        }
        if (value instanceof Boolean) {
            return BOOLEAN;
        }
        if (value instanceof Date) {
            return DATE;
        }
        if (value instanceof Clob) {
            return CLOB;
        }
        return CHARACTER;
    }

    /** */
    private final String stringValue;

    ColumnSerialisationType(final String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    /**
     * @param value
     *              value to convert.
     * @return Returns a string representation of the given value that is suitable for use as the value of a Liquibase
     *         {@link liquibase.change.core.InsertDataChange insert change}.
     */
    @Nonnull
    public String toColumnString(@Nonnull final Object value) {
        return value.toString();
    }

    /**
     * Convert value
     *
     * @param value
     * @return
     */
    public static Object convert(final Object value) {
        if (value instanceof ValueNumeric) {
            return ((ValueNumeric) value).getDelegate();
        }
        return value;
    }
}
