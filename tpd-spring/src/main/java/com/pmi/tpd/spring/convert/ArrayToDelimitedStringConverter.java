package com.pmi.tpd.spring.convert;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.ObjectUtils;

/**
 * Converts an array to a delimited String.
 *
 * @author Phillip Webb
 */
final class ArrayToDelimitedStringConverter implements ConditionalGenericConverter {

    private final CollectionToDelimitedStringConverter delegate;

    ArrayToDelimitedStringConverter(final ConversionService conversionService) {
        this.delegate = new CollectionToDelimitedStringConverter(conversionService);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Object[].class, String.class));
    }

    @Override
    public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        return this.delegate.matches(sourceType, targetType);
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        final List<Object> list = Arrays.asList(ObjectUtils.toObjectArray(source));
        return this.delegate.convert(list, sourceType, targetType);
    }

}
