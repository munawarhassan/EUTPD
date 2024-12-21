package com.pmi.tpd.core.security.util;

import static org.apache.commons.codec.binary.StringUtils.getBytesUtf16;

import java.security.MessageDigest;

/**
 * This class provides some constant time comparison functions. It uses MessageDigest.isEqual, which since java 6u17 has
 * been implemented using a constant time comparison.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class ConstantTimeComparison {

    private ConstantTimeComparison() {
    }

    /**
     * A constant time comparison implementation of isEqual.
     *
     * @param a
     *            a byte [] to compare.
     * @param b
     *            another byte [] to compare.
     * @return true if the two byte arrays are equal, otherwise false.
     * @throws NullPointerException
     *             if either a or b are null.
     */
    public static boolean isEqual(final byte[] a, final byte[] b) throws NullPointerException {
        if (a == null || b == null) {
            throw new NullPointerException("ConstantTimeComparison.isEqual does not accept null values.");
        }
        return MessageDigest.isEqual(a, b);
    }

    /**
     * A constant time comparison implementation of isEqual.
     *
     * @param a
     *            a String to compare.
     * @param b
     *            another String to compare.
     * @return true if the two Strings are equal, otherwise false.
     * @throws NullPointerException
     *             if either a or b are null.
     */
    public static boolean isEqual(final String a, final String b) throws NullPointerException {
        return isEqual(getBytesUtf16(a), getBytesUtf16(b));
    }
}
