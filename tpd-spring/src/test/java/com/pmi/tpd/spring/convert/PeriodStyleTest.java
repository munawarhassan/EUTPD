package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.is;

import java.time.Period;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.config.annotation.PeriodStyle;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link PeriodStyle}.
 *
 * @author Eddú Meléndez
 * @author Edson Chávez
 */
class PeriodStyleTest extends TestCase {

    @Test
    void detectAndParseWhenValueIsNullShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> PeriodStyle.detectAndParse(null), "Value must not be null");
    }

    @Test
    void detectAndParseWhenIso8601ShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("P15M"), is(Period.parse("P15M")));
        assertThat(PeriodStyle.detectAndParse("-P15M"), is(Period.parse("P-15M")));
        assertThat(PeriodStyle.detectAndParse("+P15M"), is(Period.parse("P15M")));
        assertThat(PeriodStyle.detectAndParse("P2D"), is(Period.parse("P2D")));
        assertThat(PeriodStyle.detectAndParse("-P20Y"), is(Period.parse("P-20Y")));

    }

    @Test
    void detectAndParseWhenSimpleDaysShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("10d").getDays(), is(10));
        assertThat(PeriodStyle.detectAndParse("10D").getDays(), is(10));
        assertThat(PeriodStyle.detectAndParse("+10d").getDays(), is(10));
        assertThat(PeriodStyle.detectAndParse("-10D").getDays(), is(-10));
    }

    @Test
    void detectAndParseWhenSimpleWeeksShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("10w"), is(Period.ofWeeks(10)));
        assertThat(PeriodStyle.detectAndParse("10W"), is(Period.ofWeeks(10)));
        assertThat(PeriodStyle.detectAndParse("+10w"), is(Period.ofWeeks(10)));
        assertThat(PeriodStyle.detectAndParse("-10W"), is(Period.ofWeeks(-10)));
    }

    @Test
    void detectAndParseWhenSimpleMonthsShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("10m").getMonths(), is(10));
        assertThat(PeriodStyle.detectAndParse("10M").getMonths(), is(10));
        assertThat(PeriodStyle.detectAndParse("+10m").getMonths(), is(10));
        assertThat(PeriodStyle.detectAndParse("-10M").getMonths(), is(-10));
    }

    @Test
    void detectAndParseWhenSimpleYearsShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("10y").getYears(), is(10));
        assertThat(PeriodStyle.detectAndParse("10Y").getYears(), is(10));
        assertThat(PeriodStyle.detectAndParse("+10y").getYears(), is(10));
        assertThat(PeriodStyle.detectAndParse("-10Y").getYears(), is(-10));
    }

    @Test
    void detectAndParseWhenSimpleWithoutSuffixShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("10").getDays(), is(10));
        assertThat(PeriodStyle.detectAndParse("+10").getDays(), is(10));
        assertThat(PeriodStyle.detectAndParse("-10").getDays(), is(-10));
    }

    @Test
    void detectAndParseWhenSimpleWithoutSuffixButWithChronoUnitShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("10", ChronoUnit.MONTHS).getMonths(), is(10));
        assertThat(PeriodStyle.detectAndParse("+10", ChronoUnit.MONTHS).getMonths(), is(10));
        assertThat(PeriodStyle.detectAndParse("-10", ChronoUnit.MONTHS).getMonths(), is(-10));
    }

    @Test
    void detectAndParseWhenComplexShouldReturnPeriod() {
        assertThat(PeriodStyle.detectAndParse("1y2m"), is(Period.of(1, 2, 0)));
        assertThat(PeriodStyle.detectAndParse("1y2m3d"), is(Period.of(1, 2, 3)));
        assertThat(PeriodStyle.detectAndParse("2m3d"), is(Period.of(0, 2, 3)));
        assertThat(PeriodStyle.detectAndParse("1y3d"), is(Period.of(1, 0, 3)));
        assertThat(PeriodStyle.detectAndParse("-1y3d"), is(Period.of(-1, 0, 3)));
        assertThat(PeriodStyle.detectAndParse("-1y-3d"), is(Period.of(-1, 0, -3)));
    }

    @Test
    void detectAndParseWhenBadFormatShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> PeriodStyle.detectAndParse("10foo"),
            "'10foo' is not a valid period");
    }

    @Test
    void detectWhenSimpleShouldReturnSimple() {
        assertThat(PeriodStyle.detect("10"), is(PeriodStyle.SIMPLE));
        assertThat(PeriodStyle.detect("+10"), is(PeriodStyle.SIMPLE));
        assertThat(PeriodStyle.detect("-10"), is(PeriodStyle.SIMPLE));
        assertThat(PeriodStyle.detect("10m"), is(PeriodStyle.SIMPLE));
        assertThat(PeriodStyle.detect("10y"), is(PeriodStyle.SIMPLE));
        assertThat(PeriodStyle.detect("10d"), is(PeriodStyle.SIMPLE));
        assertThat(PeriodStyle.detect("10D"), is(PeriodStyle.SIMPLE));
    }

    @Test
    void detectWhenIso8601ShouldReturnIso8601() {
        assertThat(PeriodStyle.detect("P20"), is(PeriodStyle.ISO8601));
        assertThat(PeriodStyle.detect("-P15M"), is(PeriodStyle.ISO8601));
        assertThat(PeriodStyle.detect("+P15M"), is(PeriodStyle.ISO8601));
        assertThat(PeriodStyle.detect("P10Y"), is(PeriodStyle.ISO8601));
        assertThat(PeriodStyle.detect("P2D"), is(PeriodStyle.ISO8601));
        assertThat(PeriodStyle.detect("-P6"), is(PeriodStyle.ISO8601));
        assertThat(PeriodStyle.detect("-P-6M"), is(PeriodStyle.ISO8601));
    }

    @Test
    void detectWhenUnknownShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> PeriodStyle.detect("bad"), "'bad' is not a valid period");
    }

    @Test
    void parseIso8601ShouldParse() {
        assertThat(PeriodStyle.ISO8601.parse("P20D"), is(Period.parse("P20D")));
        assertThat(PeriodStyle.ISO8601.parse("P15M"), is(Period.parse("P15M")));
        assertThat(PeriodStyle.ISO8601.parse("+P15M"), is(Period.parse("P15M")));
        assertThat(PeriodStyle.ISO8601.parse("P10Y"), is(Period.parse("P10Y")));
        assertThat(PeriodStyle.ISO8601.parse("P2D"), is(Period.parse("P2D")));
        assertThat(PeriodStyle.ISO8601.parse("-P6D"), is(Period.parse("-P6D")));
        assertThat(PeriodStyle.ISO8601.parse("-P-6Y+3M"), is(Period.parse("-P-6Y+3M")));
    }

    @Test
    void parseIso8601WithUnitShouldIgnoreUnit() {
        assertThat(PeriodStyle.ISO8601.parse("P20D", ChronoUnit.SECONDS), is(Period.parse("P20D")));
        assertThat(PeriodStyle.ISO8601.parse("P15M", ChronoUnit.SECONDS), is(Period.parse("P15M")));
        assertThat(PeriodStyle.ISO8601.parse("+P15M", ChronoUnit.SECONDS), is(Period.parse("P15M")));
        assertThat(PeriodStyle.ISO8601.parse("P10Y", ChronoUnit.SECONDS), is(Period.parse("P10Y")));
        assertThat(PeriodStyle.ISO8601.parse("P2D", ChronoUnit.SECONDS), is(Period.parse("P2D")));
        assertThat(PeriodStyle.ISO8601.parse("-P6D", ChronoUnit.SECONDS), is(Period.parse("-P6D")));
        assertThat(PeriodStyle.ISO8601.parse("-P-6Y+3M", ChronoUnit.SECONDS), is(Period.parse("-P-6Y+3M")));
    }

    @Test
    void parseIso8601WhenSimpleShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> PeriodStyle.ISO8601.parse("10d"),
            "'10d' is not a valid ISO-8601 period");
    }

    @Test
    void parseSimpleShouldParse() {
        assertThat(PeriodStyle.SIMPLE.parse("10m").getMonths(), is(10));
    }

    @Test
    void parseSimpleWithUnitShouldUseUnitAsFallback() {
        assertThat(PeriodStyle.SIMPLE.parse("10m", ChronoUnit.DAYS).getMonths(), is(10));
        assertThat(PeriodStyle.SIMPLE.parse("10", ChronoUnit.MONTHS).getMonths(), is(10));
    }

    @Test
    void parseSimpleWhenUnknownUnitShouldThrowException() {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> PeriodStyle.SIMPLE.parse("10x"));
        assertThat(ex.getCause().getMessage(), is("Does not match simple period pattern"));
    }

    @Test
    void parseSimpleWhenIso8601ShouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> PeriodStyle.SIMPLE.parse("PT10H"),
            "'PT10H' is not a valid simple period");
    }

    @Test
    void printIso8601ShouldPrint() {
        final Period period = Period.parse("-P-6M+3D");
        assertThat(PeriodStyle.ISO8601.print(period), is("P6M-3D"));
    }

    @Test
    void printIso8601ShouldIgnoreUnit() {
        final Period period = Period.parse("-P3Y");
        assertThat(PeriodStyle.ISO8601.print(period, ChronoUnit.DAYS), is("P-3Y"));
    }

    @Test
    void printSimpleWhenZeroWithoutUnitShouldPrintInDays() {
        final Period period = Period.ofMonths(0);
        assertThat(PeriodStyle.SIMPLE.print(period), is("0d"));
    }

    @Test
    void printSimpleWhenZeroWithUnitShouldPrintInUnit() {
        final Period period = Period.ofYears(0);
        assertThat(PeriodStyle.SIMPLE.print(period, ChronoUnit.YEARS), is("0y"));
    }

    @Test
    void printSimpleWhenNonZeroShouldIgnoreUnit() {
        final Period period = Period.of(1, 2, 3);
        assertThat(PeriodStyle.SIMPLE.print(period, ChronoUnit.YEARS), is("1y2m3d"));
    }

}