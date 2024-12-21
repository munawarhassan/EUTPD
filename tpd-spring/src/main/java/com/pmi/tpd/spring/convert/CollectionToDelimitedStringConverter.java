package com.pmi.tpd.spring.convert;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

import com.pmi.tpd.api.config.annotation.Delimiter;

/**
 * Converts a Collection to a delimited String.
 *
 * @author Phillip Webb
 */
final class CollectionToDelimitedStringConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;

    CollectionToDelimitedStringConverter(final ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(Collection.class, String.class));
    }

    @Override
    public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        final TypeDescriptor sourceElementType = sourceType.getElementTypeDescriptor();
        if (targetType == null || sourceElementType == null) {
            return true;
        }
        return this.conversionService.canConvert(sourceElementType, targetType)
                || sourceElementType.getType().isAssignableFrom(targetType.getType());
    }

    @Override
    public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        final Collection<?> sourceCollection = (Collection<?>) source;
        return convert(sourceCollection, sourceType, targetType);
    }

    private Object convert(final Collection<?> source,
        final TypeDescriptor sourceType,
        final TypeDescriptor targetType) {
        if (source.isEmpty()) {
            return "";
        }
        return source.stream()
                .map(element -> convertElement(element, sourceType, targetType))
                .collect(Collectors.joining(getDelimiter(sourceType)));
    }

    private CharSequence getDelimiter(final TypeDescriptor sourceType) {
        final Delimiter annotation = sourceType.getAnnotation(Delimiter.class);
        return annotation != null ? annotation.value() : ",";
    }

    private String convertElement(final Object element,
        final TypeDescriptor sourceType,
        final TypeDescriptor targetType) {
        return String.valueOf(
            this.conversionService.convert(element, sourceType.elementTypeDescriptor(element), targetType));
    }

}