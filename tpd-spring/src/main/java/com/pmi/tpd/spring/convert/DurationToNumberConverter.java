package com.pmi.tpd.spring.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ReflectionUtils;

import com.pmi.tpd.api.config.annotation.DurationFormat;
import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.api.config.annotation.DurationUnit;

/**
 * {@link Converter} to convert from a {@link Duration} to a {@link Number}.
 *
 * @author Phillip Webb
 * @see DurationFormat
 * @see DurationUnit
 */
final class DurationToNumberConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Duration.class, Number.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return convert((Duration) source, getDurationUnit(sourceType), targetType.getObjectType());
    }

    private ChronoUnit getDurationUnit(final TypeDescriptor sourceType) {
        final DurationUnit annotation = sourceType.getAnnotation(DurationUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private Object convert(final Duration source, final ChronoUnit unit, final Class<?> type) {
        try {
            return type.getConstructor(String.class)
                    .newInstance(String.valueOf(DurationStyle.Unit.fromChronoUnit(unit).longValue(source)));
        } catch (final Exception ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
            throw new IllegalStateException(ex);
        }
    }

}