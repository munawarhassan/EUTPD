package com.pmi.tpd.spring.convert;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

import com.pmi.tpd.api.config.annotation.DurationFormat;
import com.pmi.tpd.api.config.annotation.DurationUnit;

/**
 * {@link Converter} to convert from a {@link Number} to a {@link Duration}. Supports
 * {@link Duration#parse(CharSequence)} as well a more readable {@code 10s} form.
 *
 * @author Phillip Webb
 * @see DurationFormat
 * @see DurationUnit
 */
final class NumberToDurationConverter implements GenericConverter {

    private final StringToDurationConverter delegate = new StringToDurationConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Number.class, Duration.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        return this.delegate
                .convert(source != null ? source.toString() : null, TypeDescriptor.valueOf(String.class), targetType);
    }

}