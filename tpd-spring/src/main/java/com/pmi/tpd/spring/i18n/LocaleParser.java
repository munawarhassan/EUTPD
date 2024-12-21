package com.pmi.tpd.spring.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utility to parse locale Strings into Locale objects.
 * <p/>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class LocaleParser {

    /**
     * Cache for default locale objects that are expensive to create an excessive number of times. Note: we tried the
     * google Computing Map but it was too slow for this use case. This method can be called hundreds of times per
     * request.
     */
    private static final Map<String, Locale> LOCALE_CACHE = new ConcurrentHashMap<String, Locale>(10, 0.75f, 1);

    private LocaleParser() {
    }

    /**
     * Creates a locale from the given string. Similar to LocaleUtils, but this one is static
     *
     * @param localeString
     *            locale String
     * @return new locale based on the parameter, or null if parameter not set
     */
    public static Locale parseLocale(final String localeString) {
        if (localeString == null || localeString.length() == 0) {
            return null;
        }

        Locale locale = LOCALE_CACHE.get(localeString);
        if (locale != null) {
            return locale;
        }

        // No locale in the map then we need to get the locale, store and return it.
        locale = computeLocale(localeString);
        LOCALE_CACHE.put(localeString, locale);
        return locale;
    }

    private static Locale computeLocale(final String localeString) {
        final int pos = localeString.indexOf("_");
        Locale locale;
        if (pos != -1) {
            locale = new Locale(localeString.substring(0, pos), localeString.substring(pos + 1));
        } else {
            locale = new Locale(localeString);
        }
        return locale;
    }

}
