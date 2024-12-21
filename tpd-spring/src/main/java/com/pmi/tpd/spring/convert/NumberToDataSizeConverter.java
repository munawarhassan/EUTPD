package com.pmi.tpd.spring.convert;

import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.unit.DataSize;

import com.pmi.tpd.api.config.annotation.DataSizeUnit;

/**
 * {@link Converter} to convert from a {@link Number} to a {@link DataSize}.
 *
 * @author Stephane Nicoll
 * @see DataSizeUnit
 */
final class NumberToDataSizeConverter implements GenericConverter {

    private final StringToDataSizeConverter delegate = new StringToDataSizeConverter();

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Number.class, DataSize.class));
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        return this.delegate
                .convert(source != null ? source.toString() : null, TypeDescriptor.valueOf(String.class), targetType);
    }

}