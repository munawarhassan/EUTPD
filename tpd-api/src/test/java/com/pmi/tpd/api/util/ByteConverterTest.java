package com.pmi.tpd.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ByteConverterTest {

    @Test
    public void toByTest() {

        assertEquals(toByte(50, 2), ByteConverter.toByte("50 Mib"));
        assertEquals(toByte(50, 2), ByteConverter.toByte("50Mib"));

        assertEquals(110600, ByteConverter.toByte("110.6 kB"));
        assertEquals(110592, ByteConverter.toByte("108 kiB"));

        assertEquals(0, ByteConverter.toByte("0 B"));
        assertEquals(27, ByteConverter.toByte("27 B"));

        assertEquals(1000, ByteConverter.toByte("1.0 kB"));
        assertEquals(1000, ByteConverter.toByte("1000 B"));

        assertEquals(toSi(1, 1), ByteConverter.toByte("1.0 kB"));
        assertEquals(toByte(1, 1), ByteConverter.toByte("1.0 KiB"));

        assertEquals(toSi(27, 3), ByteConverter.toByte("27.0 GB"));
        assertEquals(toByte(27, 3), ByteConverter.toByte("27.0 GiB"));

        assertEquals(toSi(19, 4), ByteConverter.toByte("19 TB"));
        assertEquals(toByte(17, 4), ByteConverter.toByte("17 TiB"));

        assertEquals(toSi(9, 5), ByteConverter.toByte("9 PB"));
        assertEquals(toByte(8, 5), ByteConverter.toByte("8.0 PiB"));

    }

    private static long toSi(final long value, final int exp) {
        return value * (long) Math.pow(1000, exp);
    }

    private static long toByte(final long value, final int exp) {
        return value * (long) Math.pow(1024, exp);
    }
}
