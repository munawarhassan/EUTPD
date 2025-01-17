package com.pmi.tpd.web.core.servlet;

import java.util.Locale;

/**
 * An enumeration defining the places plugin filters can appear in an applications filter stack. The locations are
 * defined by what operations they immediate precede or follow.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public enum FilterLocation {
    /** */
    AFTER_ENCODING,

    /** */
    BEFORE_LOGIN,

    /** */
    BEFORE_DECORATION,

    /** */
    BEFORE_DISPATCH;

    /**
     * Parses a filter location from a string. Characters are converted to uppercase, and dashes into underscores.
     *
     * @param value
     *            The filter location as a string
     * @return The The matching filter location. Will never be null.
     * @throws java.lang.IllegalArgumentException
     *             If the filter string is null or can't be matched to a filter location enum
     */
    public static FilterLocation parse(final String value) throws IllegalArgumentException {
        if (value != null) {
            return FilterLocation.valueOf(value.toUpperCase(Locale.ENGLISH).replace('-', '_'));
        } else {
            throw new IllegalArgumentException("Invalid filter location: " + value);
        }
    }
}
