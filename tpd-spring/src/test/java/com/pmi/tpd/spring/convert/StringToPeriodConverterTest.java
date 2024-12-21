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
 * Tests for {@link StringToPeriodConverter}.
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 */
class StringToPeriodConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWhenIso8601ShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "P2Y"), is(Period.parse("P2Y")));
        assertThat(convert(conversionService, "P3M"), is(Period.parse("P3M")));
        assertThat(convert(conversionService, "P4W"), is(Period.parse("P4W")));
        assertThat(convert(conversionService, "P5D"), is(Period.parse("P5D")));
        assertThat(convert(conversionService, "P1Y2M3D"), is(Period.parse("P1Y2M3D")));
        assertThat(convert(conversionService, "P1Y2M3W4D"), is(Period.parse("P1Y2M3W4D")));
        assertThat(convert(conversionService, "P-1Y2M"), is(Period.parse("P-1Y2M")));
        assertThat(convert(conversionService, "-P1Y2M"), is(Period.parse("-P1Y2M")));
    }

    @ConversionServiceTest
    void convertWhenSimpleDaysShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10d"), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "10D"), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "+10d"), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "-10D"), is(Period.ofDays(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleWeeksShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10w"), is(Period.ofWeeks(10)));
        assertThat(convert(conversionService, "10W"), is(Period.ofWeeks(10)));
        assertThat(convert(conversionService, "+10w"), is(Period.ofWeeks(10)));
        assertThat(convert(conversionService, "-10W"), is(Period.ofWeeks(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleMonthsShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10m"), is(Period.ofMonths(10)));
        assertThat(convert(conversionService, "10M"), is(Period.ofMonths(10)));
        assertThat(convert(conversionService, "+10m"), is(Period.ofMonths(10)));
        assertThat(convert(conversionService, "-10M"), is(Period.ofMonths(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleYearsShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10y"), is(Period.ofYears(10)));
        assertThat(convert(conversionService, "10Y"), is(Period.ofYears(10)));
        assertThat(convert(conversionService, "+10y"), is(Period.ofYears(10)));
        assertThat(convert(conversionService, "-10Y"), is(Period.ofYears(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10"), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "+10"), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "-10"), is(Period.ofDays(-10)));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixButWithAnnotationShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10", ChronoUnit.DAYS, null), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "+10", ChronoUnit.DAYS, null), is(Period.ofDays(10)));
        assertThat(convert(conversionService, "-10", ChronoUnit.DAYS, null), is(Period.ofDays(-10)));
        assertThat(convert(conversionService, "10", ChronoUnit.WEEKS, null), is(Period.ofWeeks(10)));
        assertThat(convert(conversionService, "+10", ChronoUnit.WEEKS, null), is(Period.ofWeeks(10)));
        assertThat(convert(conversionService, "-10", ChronoUnit.WEEKS, null), is(Period.ofWeeks(-10)));
        assertThat(convert(conversionService, "10", ChronoUnit.MONTHS, null), is(Period.ofMonths(10)));
        assertThat(convert(conversionService, "+10", ChronoUnit.MONTHS, null), is(Period.ofMonths(10)));
        assertThat(convert(conversionService, "-10", ChronoUnit.MONTHS, null), is(Period.ofMonths(-10)));
        assertThat(convert(conversionService, "10", ChronoUnit.YEARS, null), is(Period.ofYears(10)));
        assertThat(convert(conversionService, "+10", ChronoUnit.YEARS, null), is(Period.ofYears(10)));
        assertThat(convert(conversionService, "-10", ChronoUnit.YEARS, null), is(Period.ofYears(-10)));
    }

    private Period convert(final ConversionService conversionService, final String source) {
        return conversionService.convert(source, Period.class);
    }

    private Period convert(final ConversionService conversionService,
        final String source,
        final ChronoUnit unit,
        final PeriodStyle style) {
        return (Period) conversionService
                .convert(source, TypeDescriptor.forObject(source), MockPeriodTypeDescriptor.get(unit, style));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new StringToPeriodConverter());
    }

}