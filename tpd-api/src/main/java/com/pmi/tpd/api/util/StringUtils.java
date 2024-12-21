package com.pmi.tpd.api.util;

/**
 * <p>
 * StringUtils class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Test to see if a given string ends with some suffixes. Avoids the cost of concatenating the suffixes together
     *
     * @param src
     *            the source string to be tested
     * @param suffixes
     *            the set of suffixes
     * @return true if src ends with the suffixes concatenated together
     */
    public static boolean endsWith(final String src, final String... suffixes) {
        int pos = src.length();

        for (int i = suffixes.length - 1; i >= 0; i--) {
            final String suffix = suffixes[i];
            pos -= suffix.length();
            if (!src.startsWith(suffix, pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>
     * pluralise.
     * </p>
     *
     * @param word
     *            a {@link java.lang.String} object.
     * @param n
     *            a long.
     * @return a {@link java.lang.String} object.
     */
    public static String pluralise(final String word, final long n) {
        return pluralise(word, word + 's', n);
    }

    /**
     * <p>
     * pluralise.
     * </p>
     *
     * @param singular
     *            a {@link java.lang.String} object.
     * @param plural
     *            a {@link java.lang.String} object.
     * @param n
     *            a long.
     * @return a {@link java.lang.String} object.
     */
    public static String pluralise(final String singular, final String plural, final long n) {
        Assert.isTrue(n >= 0);
        return n > 1 ? plural : singular;
    }
}
