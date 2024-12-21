package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.pmi.tpd.api.config.annotation.DurationUnit;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests for {@link NumberToDurationConverter}.
 *
 * @author Phillip Webb
 */
class NumberToDurationConverterTest extends MockitoTestCase {

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, 10).toMillis(), is(10L));
        assertThat(convert(conversionService, +10).toMillis(), is(10L));
        assertThat(convert(conversionService, -10).toMillis(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixButWithAnnotationShouldReturnDuration(
        final ConversionService conversionService) {
        assertThat(convert(conversionService, 10, ChronoUnit.SECONDS).toSeconds(), is(10L));
        assertThat(convert(conversionService, +10, ChronoUnit.SECONDS).toSeconds(), is(10L));
        assertThat(convert(conversionService, -10, ChronoUnit.SECONDS).toSeconds(), is(-10L));
    }

    private Duration convert(final ConversionService conversionService, final Integer source) {
        return conversionService.convert(source, Duration.class);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Duration convert(final ConversionService conversionService,
        final Integer source,
        final ChronoUnit defaultUnit) {
        final TypeDescriptor targetType = mock(TypeDescriptor.class, withSettings().lenient());
        if (defaultUnit != null) {
            final DurationUnit unitAnnotation = AnnotationUtils
                    .synthesizeAnnotation(Collections.singletonMap("value", defaultUnit), DurationUnit.class, null);
            when(targetType.getAnnotation(DurationUnit.class)).thenReturn(unitAnnotation);
        }
        when(targetType.getType()).thenReturn((Class) Duration.class);
        return (Duration) conversionService.convert(source, TypeDescriptor.forObject(source), targetType);
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new NumberToDurationConverter());
    }

}