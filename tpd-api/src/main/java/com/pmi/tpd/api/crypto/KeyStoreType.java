package com.pmi.tpd.api.crypto;

/**
 * Enumeration of KeyStore Types supported by the DefaultKeyStoreService class.
 */
public enum KeyStoreType {

    /** JKS type. */
    JKS("JKS"),
    /** JCEKS type. */
    JCEKS("JCEKS"),
    /** PKCS12 type. */
    PKCS12("PKCS12"),
    /** BKS_V1 type. */
    BKS_V1("BKS-V1"),
    /** BKS type. */
    BKS("BKS"),
    /** UBER type. */
    UBER("UBER");

    /** */
    private String value;

    KeyStoreType(final String value) {
        this.value = value;
    }

    /**
     * Get KeyStore type JCE name.
     *
     * @return JCE name
     */
    public String value() {
        return value;
    }

    /**
     * Does this KeyStore type support secret key entries?
     *
     * @return True, if secret key entries are supported by this KeyStore type
     */
    public boolean supportsKeyEntries() {
        return this == JCEKS || this == BKS || this == BKS_V1 || this == UBER;
    }

    /**
     * Resolve the supplied JCE name to a matching KeyStore type.
     *
     * @param value
     *            JCE name
     * @return KeyStore type or null if none
     */
    public static KeyStoreType from(final String value) {
        for (final KeyStoreType keyStoreType : values()) {
            if (value.equals(keyStoreType.value())) {
                return keyStoreType;
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
