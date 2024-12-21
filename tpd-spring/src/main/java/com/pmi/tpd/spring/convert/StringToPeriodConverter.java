package com.pmi.tpd.spring.convert;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ObjectUtils;

import com.pmi.tpd.api.config.annotation.PeriodFormat;
import com.pmi.tpd.api.config.annotation.PeriodStyle;
import com.pmi.tpd.api.config.annotation.PeriodUnit;

/**
 * {@link Converter} to convert from a {@link String} to a {@link Period}. Supports {@link Period#parse(CharSequence)}
 * as well a more readable form.
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 * @see PeriodFormat
 * @see PeriodUnit
 */
final class StringToPeriodConverter implements GenericConverter {

    @Override
    public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new GenericConverter.ConvertiblePair(String.class, Period.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        return convert(source.toString(), getStyle(targetType), getPeriodUnit(targetType));
    }

    private PeriodStyle getStyle(final TypeDescriptor targetType) {
        final PeriodFormat annotation = targetType.getAnnotation(PeriodFormat.class);
        return annotation != null ? annotation.value() : null;
    }

    private ChronoUnit getPeriodUnit(final TypeDescriptor targetType) {
        final PeriodUnit annotation = targetType.getAnnotation(PeriodUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private Period convert(final String source, PeriodStyle style, final ChronoUnit unit) {
        style = style != null ? style : PeriodStyle.detect(source);
        return style.parse(source, unit);
    }

}