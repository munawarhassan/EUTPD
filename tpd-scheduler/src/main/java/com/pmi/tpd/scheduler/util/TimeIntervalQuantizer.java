package com.pmi.tpd.scheduler.util;

import static com.pmi.tpd.api.util.Assert.isTrue;

/**
 * Tool for quantizing millisecond time intervals to a broader resolution while guarding against sign overflows.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class TimeIntervalQuantizer {

    /** */
    private static final int MILLIS_PER_MINUTE = 60000;

    private TimeIntervalQuantizer() {
    }

    /**
     * Quantizes millisecond time intervals to the specified resolution, rounding up. Examples:
     * <ul>
     * <li>{@code quantize(0,50)} &rarr; {@code 0}</li>
     * <li>{@code quantize(123,10)} &rarr; {@code 130}</li>
     * <li>{@code quantize(11,15000)} &rarr; {@code 15000}</li>
     * <li>{@code quantize(127249,60000)} &rarr; {@code 180000}</li>
     * </ul>
     *
     * @param intervalInMilliseconds
     *            the time interval to be quantized
     * @param resolution
     *            the time resolution to honour, in milliseconds.
     * @return the quantized time interval, in milliseconds with the specified resolution
     */
    public static long quantize(final long intervalInMilliseconds, final int resolution) {
        isTrue(intervalInMilliseconds >= 0L, "intervalInMilliseconds cannot be negative");
        if (resolution <= 1) {
            return intervalInMilliseconds;
        }

        final long remainder = intervalInMilliseconds % resolution;
        if (remainder == 0L) {
            return intervalInMilliseconds;
        }

        return roundUpWithBoundsCheck(intervalInMilliseconds - remainder, resolution);
    }

    private static long roundUpWithBoundsCheck(final long floor, final int resolution) {
        if (floor >= Long.MAX_VALUE - resolution) {
            return Long.MAX_VALUE;
        }

        return floor + resolution;
    }

    /**
     * Quantizes millisecond time intervals to a one minute resolution, rounding up. Examples:
     * <ul>
     * <li>{@code quantizeToMinutes(0)} &rarr; {@code 0}</li>
     * <li>{@code quantizeToMinutes(123)} &rarr; {@code 60000}</li>
     * <li>{@code quantizeToMinutes(127249)} &rarr; {@code 180000}</li>
     * </ul>
     *
     * @param intervalInMilliseconds
     *            the time interval to be quantized
     * @return the quantized time interval, in milliseconds with one minute resolution
     */
    public static long quantizeToMinutes(final long intervalInMilliseconds) {
        return quantize(intervalInMilliseconds, MILLIS_PER_MINUTE);
    }
}
