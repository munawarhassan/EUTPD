package com.pmi.tpd.spring.convert;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import com.pmi.tpd.api.config.annotation.DataSizeUnit;

/**
 * {@link Converter} to convert from a {@link String} to a {@link DataSize}. Supports
 * {@link DataSize#parse(CharSequence)}.
 *
 * @author Stephane Nicoll
 * @see DataSizeUnit
 */
final class StringToDataSizeConverter implements GenericConverter {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, DataSize.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (ObjectUtils.isEmpty(source)) {
            return null;
        }
        return convert(source.toString(), getDataUnit(targetType));
    }

    private DataUnit getDataUnit(final TypeDescriptor targetType) {
        final DataSizeUnit annotation = targetType.getAnnotation(DataSizeUnit.class);
        return annotation != null ? annotation.value() : null;
    }

    private DataSize convert(final String source, final DataUnit unit) {
        return DataSize.parse(source, unit);
    }

}