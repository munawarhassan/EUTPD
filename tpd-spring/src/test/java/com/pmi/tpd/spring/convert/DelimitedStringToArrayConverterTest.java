package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;

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
 * Tests for {@link DelimitedStringToArrayConverter}.
 *
 * @author Phillip Webb
 */
class DelimitedStringToArrayConverterTest extends TestCase {

    @ConversionServiceTest
    void canConvertFromStringToArrayShouldReturnTrue(final ConversionService conversionService) {
        assertThat(conversionService.canConvert(String.class, String[].class), is(true));
    }

    @ConversionServiceTest
    void matchesWhenTargetIsNotAnnotatedShouldReturnTrue(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor.nested(ReflectionUtils.findField(Values.class, "noAnnotation"),
            0);
        assertThat(new DelimitedStringToArrayConverter(conversionService).matches(sourceType, targetType), is(true));
    }

    @ConversionServiceTest
    void matchesWhenHasAnnotationAndNonConvertibleElementTypeShouldReturnFalse(
        final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "nonConvertibleElementType"), 0);
        assertThat(new DelimitedStringToArrayConverter(conversionService).matches(sourceType, targetType), is(false));
    }

    @ConversionServiceTest
    void convertWhenHasDelimiterOfNoneShouldReturnWholeString(final ConversionService conversionService) {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "delimiterNone"), 0);
        final String[] converted = (String[]) conversionService.convert("a,b,c", sourceType, targetType);
        assertThat(converted, arrayContaining("a,b,c"));
    }

    @Test
    void matchesWhenHasAnnotationAndConvertibleElementTypeShouldReturnTrue() {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "convertibleElementType"), 0);
        assertThat(
            new DelimitedStringToArrayConverter(new ApplicationConversionService()).matches(sourceType, targetType),
            is(true));
    }

    @Test
    void convertWhenHasConvertibleElementTypeShouldReturnConvertedType() {
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(String.class);
        final TypeDescriptor targetType = TypeDescriptor
                .nested(ReflectionUtils.findField(Values.class, "convertibleElementType"), 0);
        final Integer[] converted = (Integer[]) new ApplicationConversionService()
                .convert(" 1 |  2| 3  ", sourceType, targetType);
        assertThat(converted, arrayContaining(1, 2, 3));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments
                .with(service -> service.addConverter(new DelimitedStringToArrayConverter(service)));
    }

    static class Values {

        List<String> noAnnotation;

        @Delimiter("|")
        Integer[] convertibleElementType;

        @Delimiter("|")
        NonConvertible[] nonConvertibleElementType;

        @Delimiter(Delimiter.NONE)
        String[] delimiterNone;

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