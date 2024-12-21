package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;
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
 * Tests for {@link CollectionToDelimitedStringConverter}.
 *
 * @author Phillip Webb
 */
class CollectionToDelimitedStringConverterTest extends TestCase {

    @ConversionServiceTest
    void convertListToStringShouldConvert(final ConversionService conversionService) {
        final List<String> list = Arrays.asList("a", "b", "c");
        final String converted = conversionService.convert(list, String.class);
        assertThat(converted, is("a,b,c"));
    }

    @ConversionServiceTest
    void convertWhenHasDelimiterNoneShouldConvert(final ConversionService conversionService) {
        final Data data = new Data();
        data.none = Arrays.asList("1", "2", "3");
        final String converted = (String) conversionService.convert(data.none,
            TypeDescriptor.nested(ReflectionUtils.findField(Data.class, "none"), 0),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("123"));
    }

    @ConversionServiceTest
    void convertWhenHasDelimiterDashShouldConvert(final ConversionService conversionService) {
        final Data data = new Data();
        data.dash = Arrays.asList("1", "2", "3");
        final String converted = (String) conversionService.convert(data.dash,
            TypeDescriptor.nested(ReflectionUtils.findField(Data.class, "dash"), 0),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("1-2-3"));
    }

    @ConversionServiceTest
    void convertShouldConvertNull(final ConversionService conversionService) {
        final List<String> list = null;
        final String converted = conversionService.convert(list, String.class);
        assertThat(converted, nullValue());
    }

    @Test
    void convertShouldConvertElements() {
        final Data data = new Data();
        data.type = Arrays.asList(1, 2, 3);
        final String converted = (String) new ApplicationConversionService().convert(data.type,
            TypeDescriptor.nested(ReflectionUtils.findField(Data.class, "type"), 0),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("1.2.3"));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments
                .with(service -> service.addConverter(new CollectionToDelimitedStringConverter(service)));
    }

    static class Data {

        @Delimiter(Delimiter.NONE)
        List<String> none;

        @Delimiter("-")
        List<String> dash;

        @Delimiter(".")
        List<Integer> type;

    }

}