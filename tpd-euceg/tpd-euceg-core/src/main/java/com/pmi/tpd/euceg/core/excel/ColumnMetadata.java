package com.pmi.tpd.euceg.core.excel;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

/**
 * Additional information for {@link ColumnDescriptor}.
 *
 * @author Christophe Friederich
 * @param <T>
 *            type of column.
 * @since 2.5
 */
@Getter
@Builder()
@Immutable
public final class ColumnMetadata<T> {

    /** */
    private final String xpath;

    /** */
    @JsonProperty(required = false)
    private final T defaultValue;

    /** */
    @JsonProperty(required = false)
    private final boolean nullable;

    /** */
    private final Class<T> targetType;

    /** */
    private final String format;

    public static <T> ColumnMetadataBuilder<T> builder(@Nonnull final Class<T> targetType) {
        return new ColumnMetadataBuilder<T>().targetType(checkNotNull(targetType, "targetType"));
    }
}
