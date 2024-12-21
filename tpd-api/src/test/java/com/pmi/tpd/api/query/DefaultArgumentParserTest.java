package com.pmi.tpd.api.query;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.testing.junit5.TestCase;

public class DefaultArgumentParserTest extends TestCase {

    @Test
    public void shouldParseJaxbEnum() {
        final IArgumentParser parser = new DefaultArgumentParser();
        assertThat(parser.parse("1", EnumInteger.class), Matchers.is(EnumInteger.VALUE1));
    }

    @Test
    public void shouldParseJodaDateTime() {
        final IArgumentParser parser = new DefaultArgumentParser();
        assertThat(parser.parse("2022-12-31T23:00:00.000Z", DateTime.class),
            Matchers.is(ISODateTimeFormat.dateTime().parseDateTime("2022-12-31T23:00:00.000Z")));
    }

    public static enum EnumInteger {

        VALUE1(1),
        VALUE2(2),
        VALUE3(3),
        VALUE4(4),
        VALUE5(5);

        private final int value;

        EnumInteger(final int v) {
            value = v;
        }

        public int value() {
            return value;
        }

        public static EnumInteger fromValue(final int v) {
            for (final EnumInteger c : EnumInteger.values()) {
                if (c.value == v) {
                    return c;
                }
            }
            throw new IllegalArgumentException(String.valueOf(v));
        }
    }
}
