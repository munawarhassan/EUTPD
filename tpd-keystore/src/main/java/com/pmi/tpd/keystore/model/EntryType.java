package com.pmi.tpd.keystore.model;

/**
 * Enumeration of type of keys.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public enum EntryType {
    /** key entry. */
    Key,
    /** key pair entry. */
    KeyPair,
    /** trusted certificate entry. */
    TrustedCertificate
}
