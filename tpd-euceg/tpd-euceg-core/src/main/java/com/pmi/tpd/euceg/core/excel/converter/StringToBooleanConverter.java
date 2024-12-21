package com.pmi.tpd.euceg.core.excel.converter;

import java.util.Set;

import org.springframework.core.convert.converter.Converter;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.euceg.core.excel.ExcelHelper;

/**
 * Boolean converter used in {@link ExcelHelper} to convert value contained in cell of excel file representing a
 * {@link Boolean}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class StringToBooleanConverter implements Converter<String, Boolean> {

    /** */
    private static final Set<String> TRUE_VALUES = ImmutableSet.of("true", "on", "yes", "y", "1");

    /** */
    private static final Set<String> FALSE_VALUES = ImmutableSet.of("false", "off", "no", "n", "0");

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean convert(final String source) {
        String value = source.trim();
        if ("".equals(value)) {
            return Boolean.FALSE;
        }
        value = value.toLowerCase();
        if (TRUE_VALUES.contains(value)) {
            return Boolean.TRUE;
        } else if (FALSE_VALUES.contains(value)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalArgumentException("Invalid boolean value '" + source + "'");
        }
    }

}
