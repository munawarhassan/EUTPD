package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.ReflectionUtils;

import com.pmi.tpd.api.config.annotation.Delimiter;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link DelimitedStringToCollectionConverter}.
 *
 * @author Phillip Webb
 */
class DelimitedStringToCollectionConverterTest extends TestCase {

    @ConversionServiceTest
    void canConvertFromStringToCollectionShouldReturnTrue(final ConversionService conversionService) {
        assertThat(conversionService.canConvert(String.class, Collection.class), is(true));
    }

    @ConversionServiceTest
    void matchesWhenTargetIsNotAnnotatedShouldReturnTrue(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor.nested(ReflectionUtils.findField(Values.class, "noAnnotation"),
            0);
        assertThat(new DelimitedStringToCollectionConverter(conversionService).matches(sourceType, targetType),
            is(true));
    }

    @ConversionServiceTest
    void matchesWhenHasAnnotationAndNoElementTypeShouldReturnTrue(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "noElementType"), 0);
        assertThat(new DelimitedStringToCollectionConverter(conversionService).matches(sourceType, targetType),
            is(true));
    }

    @ConversionServiceTest
    void matchesWhenHasAnnotationAndNonConvertibleElementTypeShouldReturnFalse(
        final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "nonConvertibleElementType"), 0);
        assertThat(new DelimitedStringToCollectionConverter(conversionService).matches(sourceType, targetType),
            is(false));
    }

    @ConversionServiceTest
    @SuppressWarnings("unchecked")
    void convertWhenHasNoElementTypeShouldReturnTrimmedString(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "noElementType"), 0);
        final Collection<String> converted = (Collection<String>) conversionService
                .convert(" a |  b| c  ", sourceType, targetType);
        assertThat(converted, contains("a", "b", "c"));
    }

    @ConversionServiceTest
    @SuppressWarnings("unchecked")
    void convertWhenHasDelimiterOfNoneShouldReturnWholeString(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "delimiterNone"), 0);
        final List<String> converted = (List<String>) conversionService.convert("a,b,c", sourceType, targetType);
        assertThat(converted, contains("a,b,c"));
    }

    @SuppressWarnings("unchecked")
    @ConversionServiceTest
    void convertWhenHasCollectionObjectTypeShouldUseCollectionObjectType(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor.nested(ReflectionUtils.findField(Values.class, "specificType"),
            0);
        final MyCustomList<String> converted = (MyCustomList<String>) conversionService
                .convert("a*b", sourceType, targetType);
        assertThat(converted, contains("a", "b"));
    }

    @Test
    void matchesWhenHasAnnotationAndConvertibleElementTypeShouldReturnTrue() {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "convertibleElementType"), 0);
        assertThat(
            new DelimitedStringToCollectionConverter(new ApplicationConversionService()).matches(sourceType,
                targetType),
            is(true));
    }

    @Test
    @SuppressWarnings("unchecked")
    void convertWhenHasConvertibleElementTypeShouldReturnConvertedType() {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "convertibleElementType"), 0);
        final List<Integer> converted = (List<Integer>) new ApplicationConversionService()
                .convert(" 1 |  2| 3  ", sourceType, targetType);
        assertThat(converted, contains(1, 2, 3));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments
                .with(service -> service.addConverter(new DelimitedStringToCollectionConverter(service)));
    }

    static class Values {

        List<String> noAnnotation;

        @SuppressWarnings("rawtypes")
        @Delimiter("|")
        List noElementType;

        @Delimiter("|")
        List<Integer> convertibleElementType;

        @Delimiter("|")
        List<NonConvertible> nonConvertibleElementType;

        @Delimiter(Delimiter.NONE)
        List<String> delimiterNone;

        @Delimiter("*")
        MyCustomList<String> specificType;

    }

    static class NonConvertible {

    }

    static class MyCustomList<E> extends LinkedList<E> {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

    }

}