package com.pmi.tpd.api.crypto;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Christophe Friederich
 * @version 1.0
 */
public class KeyPair {

    /** */
    private final PrivateKey privateKey;

    /** */
    private final List<X509Certificate> certificateChain;

    /**
     * @param privateKey
     *            A private key
     * @param certificateChain
     *            list of certificate associate.
     */
    public KeyPair(final PrivateKey privateKey, final List<X509Certificate> certificateChain) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
    }

    /**
     * @return Returns private key.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * @return Returns a array of {@link X509Certificate certificates}.
     */
    public X509Certificate[] getCertificateChain() {
        return certificateChain.toArray(new X509Certificate[certificateChain.size()]);
    }
}
