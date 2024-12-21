package com.pmi.tpd.euceg.backend.core.internal;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

/**
 * fix for Exception in thread "main" javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException:
 * PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid
 * certification path to requested target
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class TrustAllManager extends X509ExtendedTrustManager {

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        // trust all no work required
    }

    @Override
    public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
        // trust all no work required
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
            throws CertificateException {

    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
            throws CertificateException {

    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
            throws CertificateException {

    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
            throws CertificateException {

    }
}