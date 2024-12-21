package com.pmi.tpd.spring.context.bind;

import org.springframework.core.convert.converter.Converter;

/**
 * Converts a String to a Char Array.
 *
 * @author Phillip Webb
 */
class StringToCharArrayConverter implements Converter<String, char[]> {

    @Override
    public char[] convert(final String source) {
        return source.toCharArray();
    }

}
