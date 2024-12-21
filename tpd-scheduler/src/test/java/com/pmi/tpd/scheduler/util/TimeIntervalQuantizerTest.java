package com.pmi.tpd.scheduler.util;

import static com.pmi.tpd.scheduler.util.TimeIntervalQuantizer.quantize;
import static com.pmi.tpd.scheduler.util.TimeIntervalQuantizer.quantizeToMinutes;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class TimeIntervalQuantizerTest extends TestCase {

    @Test
    public void testNegativeInterval() {
        assertThrows(IllegalArgumentException.class, () -> {
            quantize(-1L, 5);
        });
    }

    @Test
    public void testIgnoredResolutions() {
        assertEquals(5L, quantize(5L, -42));
        assertEquals(5L, quantize(5L, -1));
        assertEquals(5L, quantize(5L, 0));
        assertEquals(5L, quantize(5L, 1));
    }

    @Test
    public void testQuantizationBoundaries() {
        assertEquals(0L, quantize(0L, 100));
        assertEquals(100L, quantize(1L, 100));
        assertEquals(100L, quantize(99L, 100));
        assertEquals(100L, quantize(100L, 100));
        assertEquals(200L, quantize(101L, 100));
        assertEquals(200L, quantize(199L, 100));
        assertEquals(200L, quantize(200L, 100));
        assertEquals(300L, quantize(201L, 100));

        assertEquals(0L, quantize(0L, 1000));
        assertEquals(1000L, quantize(10L, 1000));
        assertEquals(1000L, quantize(999L, 1000));
        assertEquals(1000L, quantize(1000L, 1000));
        assertEquals(2000L, quantize(1001L, 1000));
        assertEquals(2000L, quantize(1999L, 1000));
        assertEquals(2000L, quantize(2000L, 1000));
        assertEquals(3000L, quantize(2001L, 1000));
    }

    @Test
    public void testSignOverflowBoundary() {
        // Long.MAX_VALUE and 4 are relatively prime
        assertEquals(9223372036854775804L, quantize(9223372036854775801L, 4));
        assertEquals(9223372036854775804L, quantize(9223372036854775804L, 4));
        assertEquals(9223372036854775807L, quantize(9223372036854775805L, 4));
        assertEquals(9223372036854775807L, quantize(9223372036854775807L, 4));

        // Long.MAX_VALUE and 7 are not relatively prime
        assertEquals(9223372036854775800L, quantize(9223372036854775799L, 7));
        assertEquals(9223372036854775800L, quantize(9223372036854775800L, 7));
        assertEquals(9223372036854775807L, quantize(9223372036854775801L, 7));
        assertEquals(9223372036854775807L, quantize(9223372036854775806L, 7));
        assertEquals(9223372036854775807L, quantize(9223372036854775807L, 7));
    }

    @Test
    public void testQuantizeExamples() {
        assertEquals(0L, quantize(0L, 50));
        assertEquals(130L, quantize(123L, 10));
        assertEquals(15000L, quantize(11L, 15000));
        assertEquals(180000L, quantize(127249L, 60000));
    }

    @Test
    public void testQuantizeToMinutesExamples() {
        assertEquals(0L, quantizeToMinutes(0));
        assertEquals(60000L, quantizeToMinutes(123));
        assertEquals(180000L, quantizeToMinutes(127249));
    }
}
