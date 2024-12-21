package com.pmi.tpd.api.util;

import java.math.BigDecimal;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public final class ByteConverter {

    private ByteConverter() {
        throw new UnsupportedOperationException("static class");
    }

    /**
     * Convert a human-readable version in IEC binary format of the {@code bytes}. See
     * <a href="https://en.wikipedia.org/wiki/ISO/IEC_80000">IEC_80000</a> and
     * <a href="https://en.wikipedia.org/wiki/Byte">Byte</a> for more information
     *
     * @param bytes
     *            The number of bytes.
     * @return Returns A human-readable display value in IEC binary format (includes units).
     */
    public static String toStringByte(final long bytes) {
        final int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Convert a human-readable version in SI decimal format of the {@code bytes}. See
     * <a href="https://en.wikipedia.org/wiki/Metric_prefix">metric prefix</a> and
     * <a href="https://en.wikipedia.org/wiki/Byte">Byte</a> for more information
     *
     * @param bytes
     *            The number of bytes.
     * @return Returns A human-readable display value in SI decimal format (includes units).
     */
    public static String toStringDecimal(final long bytes) {
        final int unit = 1000;
        if (bytes < unit) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Convert a string representation of byte in IEC binary format to number representation. See
     * <a href="https://en.wikipedia.org/wiki/ISO/IEC_80000">IEC_80000</a> and
     * <a href="https://en.wikipedia.org/wiki/Byte">Byte</a> for more information
     *
     * @param bytes
     *            The number of bytes.
     * @return Returns a number representing the {@code bytes} in IEC binary format.
     */
    public static long toByte(final String bytes) {
        final Pattern pattern = Pattern.compile("^([\\d.,]*)\\s*([a-zA-Z]*)$");
        final Matcher matcher = pattern.matcher(bytes);
        if (!matcher.find()) {
            throw new UnknownFormatConversionException(bytes);
        }
        final String number = matcher.group(1);
        String unit = matcher.group(2).trim();
        int exp = 0;
        int base = 1024;
        final BigDecimal value = new BigDecimal(number);
        if (unit != null && unit.length() > 1) {
            unit = unit.toUpperCase();
            exp = "KMGTPE".indexOf(unit.charAt(0)) + 1;
            if (unit.charAt(1) == 'B') {
                base = 1000;
            }
        }
        final BigDecimal d = value.multiply(BigDecimal.valueOf(Math.pow(base, exp)));
        return d.longValue();
    }

}
