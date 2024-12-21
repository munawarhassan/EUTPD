package com.pmi.tpd.spring.convert;

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * {@link Formatter} for {@code char[]}.
 *
 * @author Phillip Webb
 */
final class CharArrayFormatter implements Formatter<char[]> {

    @Override
    public String print(final char[] object, final Locale locale) {
        return new String(object);
    }

    @Override
    public char[] parse(final String text, final Locale locale) throws ParseException {
        return text.toCharArray();
    }

}