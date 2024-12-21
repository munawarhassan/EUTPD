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
 * Tests for {@link DurationToNumberConverter}.
 *
 * @author Phillip Webb
 */
class DurationToNumberConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWithoutStyleShouldReturnMs(final ConversionService conversionService) {
        final Long converted = conversionService.convert(Duration.ofSeconds(1), Long.class);
        assertThat(converted, equalTo(1000L));
    }

    @ConversionServiceTest
    void convertWithFormatShouldUseIgnoreFormat(final ConversionService conversionService) {
        final Integer converted = (Integer) conversionService.convert(Duration.ofSeconds(1),
            MockDurationTypeDescriptor.get(null, DurationStyle.ISO8601),
            TypeDescriptor.valueOf(Integer.class));
        assertThat(converted, equalTo(1000));
    }

    @ConversionServiceTest
    void convertWithFormatAndUnitShouldUseFormatAndUnit(final ConversionService conversionService) {
        final Byte converted = (Byte) conversionService.convert(Duration.ofSeconds(1),
            MockDurationTypeDescriptor.get(ChronoUnit.SECONDS, null),
            TypeDescriptor.valueOf(Byte.class));
        assertThat(converted, equalTo((byte) 1));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new DurationToNumberConverter());
    }

}