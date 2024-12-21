package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link StringToDurationConverter}.
 *
 * @author Phillip Webb
 */
class StringToDurationConverterTest extends TestCase {

    @ConversionServiceTest
    void convertWhenIso8601ShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "PT20.345S"), is(Duration.parse("PT20.345S")));
        assertThat(convert(conversionService, "PT15M"), is(Duration.parse("PT15M")));
        assertThat(convert(conversionService, "+PT15M"), is(Duration.parse("PT15M")));
        assertThat(convert(conversionService, "PT10H"), is(Duration.parse("PT10H")));
        assertThat(convert(conversionService, "P2D"), is(Duration.parse("P2D")));
        assertThat(convert(conversionService, "P2DT3H4M"), is(Duration.parse("P2DT3H4M")));
        assertThat(convert(conversionService, "-PT6H3M"), is(Duration.parse("-PT6H3M")));
        assertThat(convert(conversionService, "-PT-6H+3M"), is(Duration.parse("-PT-6H+3M")));
    }

    @ConversionServiceTest
    void convertWhenSimpleNanosShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10ns").toNanos(), is(10L));
        assertThat(convert(conversionService, "10NS").toNanos(), is(10L));
        assertThat(convert(conversionService, "+10ns").toNanos(), is(10L));
        assertThat(convert(conversionService, "-10ns").toNanos(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleMicrosShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10us").toNanos(), is(10000L));
        assertThat(convert(conversionService, "10US").toNanos(), is(10000L));
        assertThat(convert(conversionService, "+10us").toNanos(), is(10000L));
        assertThat(convert(conversionService, "-10us").toNanos(), is(-10000L));
    }

    @ConversionServiceTest
    void convertWhenSimpleMillisShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10ms").toMillis(), is(10L));
        assertThat(convert(conversionService, "10MS").toMillis(), is(10L));
        assertThat(convert(conversionService, "+10ms").toMillis(), is(10L));
        assertThat(convert(conversionService, "-10ms").toMillis(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleSecondsShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10s").toSeconds(), is(10L));
        assertThat(convert(conversionService, "10S").toSeconds(), is(10L));
        assertThat(convert(conversionService, "+10s").toSeconds(), is(10L));
        assertThat(convert(conversionService, "-10s").toSeconds(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleMinutesShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10m").toMinutes(), is(10L));
        assertThat(convert(conversionService, "10M").toMinutes(), is(10L));
        assertThat(convert(conversionService, "+10m").toMinutes(), is(10L));
        assertThat(convert(conversionService, "-10m").toMinutes(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleHoursShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10h").toHours(), is(10L));
        assertThat(convert(conversionService, "10H").toHours(), is(10L));
        assertThat(convert(conversionService, "+10h").toHours(), is(10L));
        assertThat(convert(conversionService, "-10h").toHours(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleDaysShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10d").toDays(), is(10L));
        assertThat(convert(conversionService, "10D").toDays(), is(10L));
        assertThat(convert(conversionService, "+10d").toDays(), is(10L));
        assertThat(convert(conversionService, "-10d").toDays(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixShouldReturnDuration(final ConversionService conversionService) {
        assertThat(convert(conversionService, "10").toMillis(), is(10L));
        assertThat(convert(conversionService, "+10").toMillis(), is(10L));
        assertThat(convert(conversionService, "-10").toMillis(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenSimpleWithoutSuffixButWithAnnotationShouldReturnDuration(
        final ConversionService conversionService) {
        assertThat(convert(conversionService, "10", ChronoUnit.SECONDS, null).toSeconds(), is(10L));
        assertThat(convert(conversionService, "+10", ChronoUnit.SECONDS, null).toSeconds(), is(10L));
        assertThat(convert(conversionService, "-10", ChronoUnit.SECONDS, null).toSeconds(), is(-10L));
    }

    @ConversionServiceTest
    void convertWhenBadFormatShouldThrowException(final ConversionService conversionService) {
        assertThrows(ConversionFailedException.class,
            () -> convert(conversionService, "10foo"),
            "'10foo' is not a valid duration");
    }

    @ConversionServiceTest
    void convertWhenStyleMismatchShouldThrowException(final ConversionService conversionService) {
        assertThrows(ConversionFailedException.class,
            () -> convert(conversionService, "10s", null, DurationStyle.ISO8601));
    }

    @ConversionServiceTest
    void convertWhenEmptyShouldReturnNull(final ConversionService conversionService) {
        assertThat(convert(conversionService, ""), nullValue());
    }

    private Duration convert(final ConversionService conversionService, final String source) {
        return conversionService.convert(source, Duration.class);
    }

    private Duration convert(final ConversionService conversionService,
        final String source,
        final ChronoUnit unit,
        final DurationStyle style) {
        return (Duration) conversionService
                .convert(source, TypeDescriptor.forObject(source), MockDurationTypeDescriptor.get(unit, style));
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new StringToDurationConverter());
    }

}