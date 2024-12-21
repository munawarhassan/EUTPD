package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link CharSequenceToObjectConverter}
 *
 * @author Phillip Webb
 */
class CharSequenceToObjectConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWhenCanConvertViaToString(final ConversionService conversionService) {
        assertThat(conversionService.convert(new StringBuilder("1"), Integer.class), equalTo(1));
    }

    @ConversionServiceTest
    void convertWhenCanConvertDirectlySkipsStringConversion(final ConversionService conversionService) {
        assertThat(conversionService.convert(new String("1"), Long.class), equalTo(1L));
        if (!ConversionServiceArguments.isApplicationConversionService(conversionService)) {
            assertThat(conversionService.convert(new StringBuilder("1"), Long.class), equalTo(2L));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void convertWhenTargetIsList() {
        final ConversionService conversionService = new ApplicationConversionService();
        final StringBuilder source = new StringBuilder("1,2,3");
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(StringBuilder.class);
        final TypeDescriptor targetType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
        final List<String> conveted = (List<String>) conversionService.convert(source, sourceType, targetType);
        assertThat(conveted, contains("1", "2", "3"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void convertWhenTargetIsListAndNotUsingApplicationConversionService() {
        final FormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new CharSequenceToObjectConverter(conversionService));
        final StringBuilder source = new StringBuilder("1,2,3");
        final TypeDescriptor sourceType = TypeDescriptor.valueOf(StringBuilder.class);
        final TypeDescriptor targetType = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class));
        final List<String> conveted = (List<String>) conversionService.convert(source, sourceType, targetType);
        assertThat(conveted, contains("1", "2", "3"));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(conversionService -> {
            conversionService.addConverter(new StringToIntegerConverter());
            conversionService.addConverter(new StringToLongConverter());
            conversionService.addConverter(new CharSequenceToLongConverter());
            conversionService.addConverter(new CharSequenceToObjectConverter(conversionService));
        });
    }

    static class StringToIntegerConverter implements Converter<String, Integer> {

        @Override
        public Integer convert(final String source) {
            return Integer.valueOf(source);
        }

    }

    static class StringToLongConverter implements Converter<String, Long> {

        @Override
        public Long convert(final String source) {
            return Long.valueOf(source);
        }

    }

    static class CharSequenceToLongConverter implements Converter<CharSequence, Long> {

        @Override
        public Long convert(final CharSequence source) {
            return Long.valueOf(source.toString()) + 1;
        }

    }

}