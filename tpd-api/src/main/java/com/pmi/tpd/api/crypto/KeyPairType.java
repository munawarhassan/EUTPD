package com.pmi.tpd.api.crypto;

/**
 * Enumeration of Key Pair Types supported by the KeyPairUtil class.
 */
public enum KeyPairType {
    RSA("RSA", "1.2.840.113549.1.1.1", 512, 16384, 8),
    DSA("DSA", "1.2.840.10040.4.1", 512, 1024, 64),
    EC("EC", "1.2.840.10045.2.1", 160, 571, 32);

    private String value;

    private String oid;

    private int minSize;

    private int maxSize;

    private int stepSize;

    KeyPairType(final String value, final String oid, final int minSize, final int maxSize, final int stepSize) {
        this.value = value;
        this.oid = oid;
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.stepSize = stepSize;
    }

    /**
     * Get key pair type JCE name.
     *
     * @return JCE name
     */
    public String value() {
        return value;
    }

    /**
     * Get key pair type Object Identifier.
     *
     * @return Object Identifier
     */
    public String oid() {
        return oid;
    }

    /**
     * Get key pair minimum size.
     *
     * @return Minimum size
     */
    public int minSize() {
        return minSize;
    }

    /**
     * Get key pair maximum size.
     *
     * @return Maximum size
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Get key pair step size.
     *
     * @return Step size
     */
    public int stepSize() {
        return stepSize;
    }

    /**
     * Resolve the supplied JCE name to a matching KeyPair type.
     *
     * @param value
     *            JCE name
     * @return KeyPair type or null if none
     */
    public static KeyPairType from(final String value) {
        for (final KeyPairType keyPairType : values()) {
            if (value.equals(keyPairType.value())) {
                return keyPairType;
            }
        }

        return null;
    }

    /**
     * Returns JCE name.
     *
     * @return JCE name
     */
    @Override
    public String toString() {
        return value();
    }
}
