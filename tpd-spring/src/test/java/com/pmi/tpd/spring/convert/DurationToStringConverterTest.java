package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link DurationToStringConverter}.
 *
 * @author Phillip Webb
 */
class DurationToStringConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWithoutStyleShouldReturnIso8601(final ConversionService conversionService) {
        final String converted = conversionService.convert(Duration.ofSeconds(1), String.class);
        assertThat(converted, equalTo("PT1S"));
    }

    @ConversionServiceTest
    void convertWithFormatShouldUseFormatAndMs(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Duration.ofSeconds(1),
            MockDurationTypeDescriptor.get(null, DurationStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, equalTo("1000ms"));
    }

    @ConversionServiceTest
    void convertWithFormatAndUnitShouldUseFormatAndUnit(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Duration.ofSeconds(1),
            MockDurationTypeDescriptor.get(ChronoUnit.SECONDS, DurationStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, equalTo("1s"));
    }

    static Stream<? extends Arguments> conversionServices() throws Exception {
        return ConversionServiceArguments.with(new DurationToStringConverter());
    }

}