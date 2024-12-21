package com.pmi.tpd.spring.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ObjectUtils;

import com.pmi.tpd.api.config.annotation.DurationFormat;
import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.api.config.annotation.DurationUnit;

/**
 * {@link Converter} to convert from a {@link String} to a {@link Duration}. Supports
 * {@link Duration#parse(CharSequence)} as well a more readable {@code 10s} form.
 *
 * @author Phillip Webb
 * @see DurationFormat
 * @see DurationUnit
 */
final class StringToDurationConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Duration.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        return convert(source.toString(), getStyle(targetType), getDurationUnit(targetType));
    }

    private DurationStyle getStyle(final TypeDescriptor targetType) {
        final DurationFormat annotation = targetType.getAnnotation(DurationFormat.class);
        return annotation != null ? annotation.value() : null;
    }

    private ChronoUnit getDurationUnit(final TypeDescriptor targetType) {
        final DurationUnit annotation = targetType.getAnnotation(DurationUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private Duration convert(final String source, DurationStyle style, final ChronoUnit unit) {
        style = style != null ? style : DurationStyle.detect(source);
        return style.parse(source, unit);
    }

}