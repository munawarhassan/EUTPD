package com.pmi.tpd.spring.convert;

import java.time.Period;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

import com.pmi.tpd.api.config.annotation.PeriodFormat;
import com.pmi.tpd.api.config.annotation.PeriodUnit;

/**
 * {@link Converter} to convert from a {@link Number} to a {@link Period}. Supports {@link Period#parse(CharSequence)}
 * as well a more readable {@code 10m} form.
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 * @see PeriodFormat
 * @see PeriodUnit
 */
final class NumberToPeriodConverter implements GenericConverter {

    private final StringToPeriodConverter delegate = new StringToPeriodConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Number.class, Period.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        return this.delegate
                .convert(source != null ? source.toString() : null, TypeDescriptor.valueOf(String.class), targetType);
    }

}