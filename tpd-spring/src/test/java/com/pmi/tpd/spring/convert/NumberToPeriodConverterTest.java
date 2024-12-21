package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.pmi.tpd.api.config.annotation.PeriodUnit;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests for {@link NumberToPeriodConverter}.
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 */
class NumberToPeriodConverterTest extends MockitoTestCase {

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, 10).getDays(), is(10));
        assertThat(convert(conversionService, +10).getDays(), is(10));
        assertThat(convert(conversionService, -10).getDays(), is(-10));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixButWithAnnotationShouldReturnPeriod(final ConversionService conversionService) {
        assertThat(convert(conversionService, 10, ChronoUnit.DAYS).getDays(), is(10));
        assertThat(convert(conversionService, -10, ChronoUnit.DAYS).getDays(), is(-10));
        assertThat(convert(conversionService, 10, ChronoUnit.WEEKS), is(Period.ofWeeks(10)));
        assertThat(convert(conversionService, -10, ChronoUnit.WEEKS), is(Period.ofWeeks(-10)));
        assertThat(convert(conversionService, 10, ChronoUnit.MONTHS).getMonths(), is(10));
        assertThat(convert(conversionService, -10, ChronoUnit.MONTHS).getMonths(), is(-10));
        assertThat(convert(conversionService, 10, ChronoUnit.YEARS).getYears(), is(10));
        assertThat(convert(conversionService, -10, ChronoUnit.YEARS).getYears(), is(-10));
    }

    private Period convert(final ConversionService conversionService, final Integer source) {
        return conversionService.convert(source, Period.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Period convert(final ConversionService conversionService,
        final Integer source,
        final ChronoUnit defaultUnit) {
        final TypeDescriptor targetType = mock(TypeDescriptor.class, withSettings().lenient());
        if (defaultUnit != null) {
            final PeriodUnit unitAnnotation = AnnotationUtils
                    .synthesizeAnnotation(Collections.singletonMap("value", defaultUnit), PeriodUnit.class, null);
            when(targetType.getAnnotation(PeriodUnit.class)).thenReturn(unitAnnotation);
        }
        when(targetType.getType()).thenReturn((Class) Period.class);
        return (Period) conversionService.convert(source, TypeDescriptor.forObject(source), targetType);
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new NumberToPeriodConverter());
    }

}