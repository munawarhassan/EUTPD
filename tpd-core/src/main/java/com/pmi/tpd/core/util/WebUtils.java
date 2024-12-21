package com.pmi.tpd.core.util;

/**
 * <p>
 * Abstract WebUtils class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class WebUtils {

    /** Constant <code>STRING_EMPTY=""</code>. */
    public static final String STRING_EMPTY = "";

    /**
     *
     */
    private WebUtils() {
    }

    /**
     * <p>
     * add url path separator ('/') to the end of path.
     * </p>
     *
     * @param path
     *            a path to clean.
     * @return Returns new <code>String</code> clean path instance.
     */
    public static String cleanupPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.length() > 0) {
            if (path.charAt(path.length() - 1) != '/') {
                path += "/";
            }
        }

        return path;
    }

    /**
     * remove url path separator ('/') to the end of path.
     *
     * @param baseUrl
     *            a base url
     * @return Returns new <code>String</code> base path instance.
     * @see #normalisedPath(String)
     */
    public static String normalisedBaseUrl(final String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl;
    }

    /**
     * add url path separator ('/') to the begin of path.
     *
     * @param path
     *            a path to normalise.
     * @return Returns new <code>String</code> normalise path instance.
     * @see #normalisedBaseUrl(String)
     */
    public static String normalisedPath(final String path) {
        if (!path.startsWith("/")) {
            return "/" + path;
        }

        return path;
    }
}
