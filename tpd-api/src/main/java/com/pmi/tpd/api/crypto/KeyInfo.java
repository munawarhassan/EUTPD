package com.pmi.tpd.api.crypto;

/**
 * Holds information about a key.
 */
public class KeyInfo {

    /** */
    private final KeyType keyType;

    /** */
    private final String algorithm;

    /** */
    private final Integer size;

    public KeyInfo(final KeyType keyType, final String algorithm) {
        this(keyType, algorithm, null);
    }

    public KeyInfo(final KeyType keyType, final String algorithm, final Integer size) {
        this.keyType = keyType;
        this.algorithm = algorithm;
        this.size = size;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Get key size in bits.
     *
     * @return Key size or null if size unknown
     */
    public Integer getSize() {
        return size;
    }
}
