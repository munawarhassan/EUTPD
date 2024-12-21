package com.pmi.tpd.spring.convert;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.pmi.tpd.api.config.annotation.Delimiter;

/**
 * Converts a {@link Delimiter delimited} String to an Array.
 *
 * @author Phillip Webb
 */
final class DelimitedStringToArrayConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;

    DelimitedStringToArrayConverter(final ConversionService conversionService) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        this.conversionService = conversionService;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Object[].class));
    }

    @Override
    public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        return targetType.getElementTypeDescriptor() == null
                || this.conversionService.canConvert(sourceType, targetType.getElementTypeDescriptor());
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        return convert((String) source, sourceType, targetType);
    }

    private Object convert(final String source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        final Delimiter delimiter = targetType.getAnnotation(Delimiter.class);
        final String[] elements = getElements(source, delimiter != null ? delimiter.value() : ",");
        final TypeDescriptor elementDescriptor = targetType.getElementTypeDescriptor();
        final Object target = Array.newInstance(elementDescriptor.getType(), elements.length);
        for (int i = 0; i < elements.length; i++) {
            final String sourceElement = elements[i];
            final Object targetElement = this.conversionService
                    .convert(sourceElement.trim(), sourceType, elementDescriptor);
            Array.set(target, i, targetElement);
        }
        return target;
    }

    private String[] getElements(final String source, final String delimiter) {
        return StringUtils.delimitedListToStringArray(source, Delimiter.NONE.equals(delimiter) ? null : delimiter);
    }

}