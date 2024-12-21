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
 * {@link Converter} to convert from a {@link Period} to a {@link String}.
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 * @see PeriodFormat
 * @see PeriodUnit
 */
final class PeriodToStringConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Period.class, String.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        return convert((Period) source, getPeriodStyle(sourceType), getPeriodUnit(sourceType));
    }

    private PeriodStyle getPeriodStyle(final TypeDescriptor sourceType) {
        final PeriodFormat annotation = sourceType.getAnnotation(PeriodFormat.class);
        return annotation != null ? annotation.value() : null;
    }

    private String convert(final Period source, PeriodStyle style, final ChronoUnit unit) {
        style = style != null ? style : PeriodStyle.ISO8601;
        return style.print(source, unit);
    }

    private ChronoUnit getPeriodUnit(final TypeDescriptor sourceType) {
        final PeriodUnit annotation = sourceType.getAnnotation(PeriodUnit.class);
        return annotation != null ? annotation.value() : null;
    }

}