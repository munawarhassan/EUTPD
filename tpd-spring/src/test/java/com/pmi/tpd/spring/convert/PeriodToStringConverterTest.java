package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.pmi.tpd.api.config.annotation.PeriodStyle;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link PeriodToStringConverter}.
 *
 * @author Eddú Melendez
 * @author Edson Chávez
 */
class PeriodToStringConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWithoutStyleShouldReturnIso8601(final ConversionService conversionService) {
        final String converted = conversionService.convert(Period.ofDays(1), String.class);
        assertThat(converted, is(Period.ofDays(1).toString()));
    }

    @ConversionServiceTest
    void convertWithFormatWhenZeroShouldUseFormatAndDays(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Period.ofMonths(0),
            MockPeriodTypeDescriptor.get(null, PeriodStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("0d"));
    }

    @ConversionServiceTest
    void convertWithFormatShouldUseFormat(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Period.of(1, 2, 3),
            MockPeriodTypeDescriptor.get(null, PeriodStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("1y2m3d"));
    }

    @ConversionServiceTest
    void convertWithFormatAndUnitWhenZeroShouldUseFormatAndUnit(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Period.ofYears(0),
            MockPeriodTypeDescriptor.get(ChronoUnit.YEARS, PeriodStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("0y"));
    }

    @ConversionServiceTest
    void convertWithFormatAndUnitWhenNonZeroShouldUseFormatAndIgnoreUnit(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Period.of(1, 0, 3),
            MockPeriodTypeDescriptor.get(ChronoUnit.YEARS, PeriodStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("1y3d"));
    }

    @ConversionServiceTest
    void convertWithWeekUnitShouldConvertToStringInDays(final ConversionService conversionService) {
        final String converted = (String) conversionService.convert(Period.ofWeeks(53),
            MockPeriodTypeDescriptor.get(null, PeriodStyle.SIMPLE),
            TypeDescriptor.valueOf(String.class));
        assertThat(converted, is("371d"));
    }

    static Stream<? extends Arguments> conversionServices() throws Exception {
        return ConversionServiceArguments.with(new PeriodToStringConverter());
    }

}