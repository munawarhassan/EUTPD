package com.pmi.tpd.spring.convert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.pmi.tpd.api.config.annotation.Delimiter;

/**
 * Converts a {@link Delimiter delimited} String to a Collection.
 *
 * @author Phillip Webb
 */
final class DelimitedStringToCollectionConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;

    DelimitedStringToCollectionConverter(final ConversionService conversionService) {
        Assert.notNull(conversionService, "ConversionService must not be null");
        this.conversionService = conversionService;
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
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
        final Collection<Object> target = createCollection(targetType, elementDescriptor, elements.length);
        Stream<Object> stream = Arrays.stream(elements).map(String::trim);
        if (elementDescriptor != null) {
            stream = stream.map(element -> this.conversionService.convert(element, sourceType, elementDescriptor));
        }
        stream.forEach(target::add);
        return target;
    }

    private Collection<Object> createCollection(final TypeDescriptor targetType,
        final TypeDescriptor elementDescriptor,
        final int length) {
        return CollectionFactory.createCollection(targetType.getType(),
            elementDescriptor != null ? elementDescriptor.getType() : null,
            length);
    }

    private String[] getElements(final String source, final String delimiter) {
        return StringUtils.delimitedListToStringArray(source, Delimiter.NONE.equals(delimiter) ? null : delimiter);
    }

}