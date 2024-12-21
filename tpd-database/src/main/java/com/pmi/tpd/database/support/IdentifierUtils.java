package com.pmi.tpd.database.support;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import com.google.common.base.Function;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public final class IdentifierUtils {

    /**
     * Not for instantiation.
     */
    private IdentifierUtils() {
    }

    /** */
    private static Locale identiferCompareLocale;

    static {
        prepareIdentifierCompareLocale();
    }

    private static void prepareIdentifierCompareLocale() {
        // This system property defines the language rule
        // by which all the identifiers in crowd are normalized for comparison.
        final String preferredLang = System.getProperty("app.identifier.language");
        identiferCompareLocale = StringUtils.isNotBlank(preferredLang) ? new Locale(preferredLang) : Locale.ENGLISH;
    }

    /**
     * Converts the given identifier string to lowercase. The rule of conversion is subject to the language defined in
     * crowd.identifier.language system property.
     *
     * @param identifier
     *            the identifier string, null allowed.
     * @return lowercase identifier.
     */
    public static String toLowerCase(final String identifier) {
        return identifier == null ? null : identifier.toLowerCase(identiferCompareLocale);
    }

    /**
     * Converts the two given identifier strings to lowercase and compare them. The rule of conversion is subject to the
     * language defined in crowd.identifier.language system property.
     *
     * @param identifier1
     *            identifier. Must not be null
     * @param identifier2
     *            identifier. Must not be null
     * @return comparison result similar to as {@link java.util.Comparator#compare(Object, Object)}}
     */
    public static int compareToInLowerCase(final String identifier1, final String identifier2) {
        return toLowerCase(identifier1).compareTo(toLowerCase(identifier2));
    }

    /**
     * Converts the two given identifier strings to lowercase and check for equality. The rule of conversion is subject
     * to the language defined in crowd.identifier.language system property.
     *
     * @param identifier1
     *            identifier. Can be null
     * @param identifier2
     *            identifier. Can be null
     * @return true if equal, otherwise false.
     */
    public static boolean equalsInLowerCase(final String identifier1, final String identifier2) {
        if (identifier1 == null) {
            return identifier2 == null;
        } else {
            return identifier2 != null && compareToInLowerCase(identifier1, identifier2) == 0;
        }
    }

    /**
     * Function of {@link #toLowerCase(String)} method.
     */
    public static final Function<String, String> TO_LOWER_CASE = from -> from.toLowerCase(identiferCompareLocale);

    public static final Converter<String, String> TO_LOWER_CASE_CONVERTER = from -> from
            .toLowerCase(identiferCompareLocale);

    /**
     * @param s
     *            a non-<code>null</code> identifier
     * @return <code>true</code> if this identifier starts or ends with white space
     */
    public static boolean hasLeadingOrTrailingWhitespace(final String s) {
        return !s.equals(s.trim());
    }
}
