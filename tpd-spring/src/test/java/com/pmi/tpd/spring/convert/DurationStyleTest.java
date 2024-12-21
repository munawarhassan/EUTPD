package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.equalTo;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.config.annotation.DurationStyle;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link DurationStyle}.
 *
 * @author Phillip Webb
 */
class DurationStyleTest extends TestCase {

    @Test
    void detectAndParseWhenValueIsNullShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> DurationStyle.detectAndParse(null),
            "Value must not be null");
    }

    @Test
    void detectAndParseWhenIso8601ShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("PT20.345S"), equalTo(Duration.parse("PT20.345S")));
        assertThat(DurationStyle.detectAndParse("PT15M"), equalTo(Duration.parse("PT15M")));
        assertThat(DurationStyle.detectAndParse("+PT15M"), equalTo(Duration.parse("PT15M")));
        assertThat(DurationStyle.detectAndParse("PT10H"), equalTo(Duration.parse("PT10H")));
        assertThat(DurationStyle.detectAndParse("P2D"), equalTo(Duration.parse("P2D")));
        assertThat(DurationStyle.detectAndParse("P2DT3H4M"), equalTo(Duration.parse("P2DT3H4M")));
        assertThat(DurationStyle.detectAndParse("-PT6H3M"), equalTo(Duration.parse("-PT6H3M")));
        assertThat(DurationStyle.detectAndParse("-PT-6H+3M"), equalTo(Duration.parse("-PT-6H+3M")));
    }

    @Test
    void detectAndParseWhenSimpleNanosShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10ns").toNanos(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("10NS").toNanos(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10ns").toNanos(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10ns").toNanos(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleMicrosShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10us").toNanos(), equalTo(10000L));
        assertThat(DurationStyle.detectAndParse("10US").toNanos(), equalTo(10000L));
        assertThat(DurationStyle.detectAndParse("+10us").toNanos(), equalTo(10000L));
        assertThat(DurationStyle.detectAndParse("-10us").toNanos(), equalTo(-10000L));
    }

    @Test
    void detectAndParseWhenSimpleMillisShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10ms").toMillis(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("10MS").toMillis(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10ms").toMillis(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10ms").toMillis(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleSecondsShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10s").toSeconds(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("10S").toSeconds(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10s").toSeconds(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10s").toSeconds(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleMinutesShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10m").toMinutes(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("10M").toMinutes(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10m").toMinutes(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10m").toMinutes(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleHoursShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10h").toHours(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("10H").toHours(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10h").toHours(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10h").toHours(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleDaysShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10d").toDays(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("10D").toDays(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10d").toDays(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10d").toDays(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleWithoutSuffixShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10").toMillis(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10").toMillis(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10").toMillis(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenSimpleWithoutSuffixButWithChronoUnitShouldReturnDuration() {
        assertThat(DurationStyle.detectAndParse("10", ChronoUnit.SECONDS).toSeconds(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("+10", ChronoUnit.SECONDS).toSeconds(), equalTo(10L));
        assertThat(DurationStyle.detectAndParse("-10", ChronoUnit.SECONDS).toSeconds(), equalTo(-10L));
    }

    @Test
    void detectAndParseWhenBadFormatShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> DurationStyle.detectAndParse("10foo"),
            "'10foo' is not a valid duration");
    }

    @Test
    void detectWhenSimpleShouldReturnSimple() {
        assertThat(DurationStyle.detect("10"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("+10"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("-10"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10ns"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10ms"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10s"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10m"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10h"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10d"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("-10ms"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("-10ms"), equalTo(DurationStyle.SIMPLE));
        assertThat(DurationStyle.detect("10D"), equalTo(DurationStyle.SIMPLE));
    }

    @Test
    void detectWhenIso8601ShouldReturnIso8601() {
        assertThat(DurationStyle.detect("PT20.345S"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("PT15M"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("+PT15M"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("PT10H"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("P2D"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("P2DT3H4M"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("-PT6H3M"), equalTo(DurationStyle.ISO8601));
        assertThat(DurationStyle.detect("-PT-6H+3M"), equalTo(DurationStyle.ISO8601));
    }

    @Test
    void detectWhenUnknownShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> DurationStyle.detect("bad"),
            "'bad' is not a valid duration");
    }

    @Test
    void parseIso8601ShouldParse() {
        assertThat(DurationStyle.ISO8601.parse("PT20.345S"), equalTo(Duration.parse("PT20.345S")));
        assertThat(DurationStyle.ISO8601.parse("PT15M"), equalTo(Duration.parse("PT15M")));
        assertThat(DurationStyle.ISO8601.parse("+PT15M"), equalTo(Duration.parse("PT15M")));
        assertThat(DurationStyle.ISO8601.parse("PT10H"), equalTo(Duration.parse("PT10H")));
        assertThat(DurationStyle.ISO8601.parse("P2D"), equalTo(Duration.parse("P2D")));
        assertThat(DurationStyle.ISO8601.parse("P2DT3H4M"), equalTo(Duration.parse("P2DT3H4M")));
        assertThat(DurationStyle.ISO8601.parse("-PT6H3M"), equalTo(Duration.parse("-PT6H3M")));
        assertThat(DurationStyle.ISO8601.parse("-PT-6H+3M"), equalTo(Duration.parse("-PT-6H+3M")));
    }

    @Test
    void parseIso8601WithUnitShouldIgnoreUnit() {
        assertThat(DurationStyle.ISO8601.parse("PT20.345S", ChronoUnit.SECONDS), equalTo(Duration.parse("PT20.345S")));
        assertThat(DurationStyle.ISO8601.parse("PT15M", ChronoUnit.SECONDS), equalTo(Duration.parse("PT15M")));
        assertThat(DurationStyle.ISO8601.parse("+PT15M", ChronoUnit.SECONDS), equalTo(Duration.parse("PT15M")));
        assertThat(DurationStyle.ISO8601.parse("PT10H", ChronoUnit.SECONDS), equalTo(Duration.parse("PT10H")));
        assertThat(DurationStyle.ISO8601.parse("P2D"), equalTo(Duration.parse("P2D")));
        assertThat(DurationStyle.ISO8601.parse("P2DT3H4M", ChronoUnit.SECONDS), equalTo(Duration.parse("P2DT3H4M")));
        assertThat(DurationStyle.ISO8601.parse("-PT6H3M", ChronoUnit.SECONDS), equalTo(Duration.parse("-PT6H3M")));
        assertThat(DurationStyle.ISO8601.parse("-PT-6H+3M", ChronoUnit.SECONDS), equalTo(Duration.parse("-PT-6H+3M")));
    }

    @Test
    void parseIso8601WhenSimpleShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> DurationStyle.ISO8601.parse("10d"),
            "'10d' is not a valid ISO-8601 duration");
    }

    @Test
    void parseSimpleShouldParse() {
        assertThat(DurationStyle.SIMPLE.parse("10m").toMinutes(), equalTo(10L));
    }

    @Test
    void parseSimpleWithUnitShouldUseUnitAsFallback() {
        assertThat(DurationStyle.SIMPLE.parse("10m", ChronoUnit.SECONDS).toMinutes(), equalTo(10L));
        assertThat(DurationStyle.SIMPLE.parse("10", ChronoUnit.MINUTES).toMinutes(), equalTo(10L));
    }

    @Test
    void parseSimpleWhenUnknownUnitShouldThrowException() {

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> DurationStyle.SIMPLE.parse("10mb"));
        assertThat(ex.getCause().getMessage(), equalTo("Unknown unit 'mb'"));
    }

    @Test
    void parseSimpleWhenIso8601ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> DurationStyle.SIMPLE.parse("PT10H"),
            "'PT10H' is not a valid simple duration");

    }

    @Test
    void printIso8601ShouldPrint() {
        final Duration duration = Duration.parse("-PT-6H+3M");
        assertThat(DurationStyle.ISO8601.print(duration), equalTo("PT5H57M"));
    }

    @Test
    void printIso8601ShouldIgnoreUnit() {
        final Duration duration = Duration.parse("-PT-6H+3M");
        assertThat(DurationStyle.ISO8601.print(duration, ChronoUnit.DAYS), equalTo("PT5H57M"));
    }

    @Test
    void printSimpleWithoutUnitShouldPrintInMs() {
        final Duration duration = Duration.ofSeconds(1);
        assertThat(DurationStyle.SIMPLE.print(duration), equalTo("1000ms"));
    }

    @Test
    void printSimpleWithUnitShouldPrintInUnit() {
        final Duration duration = Duration.ofMillis(1000);
        assertThat(DurationStyle.SIMPLE.print(duration, ChronoUnit.SECONDS), equalTo("1s"));
    }

}