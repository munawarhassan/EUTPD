package com.pmi.tpd.euceg.api;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Class utility containing convert methods for JAXB generated class.
 * 
 * @author Christophe Friederich
 * @since 1.0
 */
public final class Parsers {

    /** */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    /** */
    private static final String EUCEG_DATE_PATTERN = "yyyy-MM-dd";

    /** */
    private static final String OLD_DATE_PATTERN = "dd/MM/yyyy";

    /** */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(EUCEG_DATE_PATTERN);

    /** */
    private static final DateTimeFormatter OLD_DATE_FORMATTER = DateTimeFormat.forPattern(OLD_DATE_PATTERN);

    /** */
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern(DATE_TIME_PATTERN);

    private Parsers() {
        throw new UnsupportedOperationException();
    }

    /**
     * <p>
     * Converts the string argument into a {@link DateTime} value.
     *
     * @param lexicalXSDDate
     *            A string containing lexical representation of xsd:Date.
     * @return A Date value represented by the string argument.
     * @throws IllegalArgumentException
     *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes
     *             for xsd:datetime.
     */
    public static DateTime parseDateTime(final String lexicalXSDDate) {
        if (lexicalXSDDate != null) {
            return DATETIME_FORMATTER.parseDateTime(lexicalXSDDate);
        }
        return null;
    }

    /**
     * <p>
     * Converts a {@link DateTime} value into a string.
     *
     * @param val
     *            A {@link DateTime} value
     * @return A string containing a lexical representation of xsd:datetime
     * @throws IllegalArgumentException
     *             if <tt>val</tt> is null.
     */
    public static String printDateTime(final DateTime val) {
        if (val == null) {
            return null;
        }
        return DATETIME_FORMATTER.print(val);
    }

    /**
     * <p>
     * Converts the string argument into a {@link LocalDate} value.
     *
     * @param lexicalXSDDate
     *            A string containing lexical representation of xsd:Date.
     * @return A Date value represented by the string argument.
     * @throws IllegalArgumentException
     *             if string parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes
     *             for xsd:Date.
     */
    public static LocalDate parseLocalDate(final String lexicalXSDDate) {
        if (lexicalXSDDate != null) {
            try {
                return DATE_FORMATTER.parseLocalDate(lexicalXSDDate);
            } catch (final IllegalArgumentException e) {
                // This might be an old date format
                return OLD_DATE_FORMATTER.parseLocalDate(lexicalXSDDate);
            }
        }
        return null;
    }

    /**
     * <p>
     * Converts a {@link LocalDate} value into a string.
     *
     * @param val
     *            A {@link LocalDate} value
     * @return A string containing a lexical representation of xsd:date
     * @throws IllegalArgumentException
     *             if <tt>val</tt> is null.
     */
    public static String printLocalDate(final LocalDate val) {
        if (val == null) {
            return null;
        }
        return DATE_FORMATTER.print(val);
    }

}
