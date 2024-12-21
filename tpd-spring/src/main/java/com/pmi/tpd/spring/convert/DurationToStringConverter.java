package com.pmi.tpd.spring.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;

import com.pmi.tpd.api.config.annotation.DurationFormat;
import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.api.config.annotation.DurationUnit;

/**
 * {@link Converter} to convert from a {@link Duration} to a {@link String}.
 *
 * @author Phillip Webb
 * @see DurationFormat
 * @see DurationUnit
 */
final class DurationToStringConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Duration.class, String.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return convert((Duration) source, getDurationStyle(sourceType), getDurationUnit(sourceType));
    }

    private ChronoUnit getDurationUnit(final TypeDescriptor sourceType) {
        final DurationUnit annotation = sourceType.getAnnotation(DurationUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private DurationStyle getDurationStyle(final TypeDescriptor sourceType) {
        final DurationFormat annotation = sourceType.getAnnotation(DurationFormat.class);
        return annotation != null ? annotation.value() : null;
    }

    private String convert(final Duration source, DurationStyle style, final ChronoUnit unit) {
        style = style != null ? style : DurationStyle.ISO8601;
        return style.print(source, unit);
    }

}