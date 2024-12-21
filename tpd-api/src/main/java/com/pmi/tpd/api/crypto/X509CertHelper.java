package com.pmi.tpd.api.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.util.encoders.Base64;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

/**
 * Provides utility methods relating to X509 Certificates and CRLs.
 */
public final class X509CertHelper {

    private static final String X509_CERT_TYPE = "X.509";

    private static final String PKCS7_ENCODING = "PKCS7";

    private static final String PKI_PATH_ENCODING = "PkiPath";

    @SuppressWarnings("unused")
    private static final String CERT_PEM_TYPE = "CERTIFICATE";

    @SuppressWarnings("unused")
    private static final String PKCS7_PEM_TYPE = "PKCS7";

    public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";

    public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    private X509CertHelper() {
    }

    /**
     * Load one or more certificates from the specified stream.
     *
     * @param is
     *            Stream to load certificates from
     * @return The certificates
     */
    public static List<X509Certificate> loadCertificates(InputStream is) {
        byte[] certsBytes = null;

        try {
            certsBytes = ByteStreams.toByteArray(is);

            // fix common input certificate problems by converting PEM/B64 to DER
            certsBytes = fixCommonInputCertProblems(certsBytes);

            is = new ByteArrayInputStream(certsBytes);

            final CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);
            final Collection<? extends Certificate> certs = cf.generateCertificates(is);
            final ArrayList<X509Certificate> loadedCerts = Lists.newArrayList();

            for (final Certificate certificate : certs) {
                final X509Certificate cert = (X509Certificate) certificate;

                if (cert != null) {
                    loadedCerts.add(cert);
                }
            }

            return loadedCerts;
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (final CertificateException ex) {
            return loadCertificatesPkiPath(new ByteArrayInputStream(certsBytes));
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private static List<X509Certificate> loadCertificatesPkiPath(final InputStream is) {
        try {
            final CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);
            final CertPath certPath = cf.generateCertPath(is, PKI_PATH_ENCODING);

            final List<? extends Certificate> certs = certPath.getCertificates();

            final ArrayList<X509Certificate> loadedCerts = Lists.newArrayList();

            for (final Certificate certificate : certs) {
                final X509Certificate cert = (X509Certificate) certificate;

                if (cert != null) {
                    loadedCerts.add(cert);
                }
            }

            return loadedCerts;
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private static byte[] fixCommonInputCertProblems(final byte[] certs) throws IOException {

        // remove PEM header/footer
        String certsStr = new String(certs);
        if (certsStr.startsWith(BEGIN_CERTIFICATE)) {
            certsStr = certsStr.replaceAll(BEGIN_CERTIFICATE, "");
            certsStr = certsStr.replaceAll(END_CERTIFICATE, "!");
        }

        // If one or more base 64 encoded certs then decode
        final String[] splitCertsStr = certsStr.split("!");
        byte[] allDecoded = null;
        for (final String singleCertB64 : splitCertsStr) {
            final byte[] decoded = attemptBase64Decode(singleCertB64.trim());
            if (decoded != null) {
                allDecoded = addAll(allDecoded, decoded);
            }
        }
        if (allDecoded != null) {
            return allDecoded;
        }

        return certs;
    }

    private static byte[] attemptBase64Decode(String toTest) {

        // Attempt to decode the supplied byte array as a base 64 encoded SPC.
        // Character set may be UTF-16 big endian or ASCII.

        final char[] base64 = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
                'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
                '7', '8', '9', '+', '/', '=' };

        // remove all non visible characters (like newlines) and whitespace
        toTest = toTest.replaceAll("\\s", "");

        // Check all characters are base 64. Discard any zero bytes that be
        // present if UTF-16 encoding is used but will mess up a base 64 decode
        final StringBuffer sb = new StringBuffer();

        nextChar: for (int i = 0; i < toTest.length(); i++) {
            final char c = toTest.charAt(i);

            for (int j = 0; j < base64.length; j++) {
                // append base 64 byte
                if (c == base64[j]) {
                    sb.append(c);
                    continue nextChar;
                } else if (c == 0) {
                    // discard zero byte
                    continue nextChar;
                }
            }

            // not base 64
            return null;
        }

        // use BC for actual decoding
        try {
            return Base64.decode(sb.toString());
        } catch (final Exception e) {
            // not base 64
        }

        return null;
    }

    /**
     * Load a CRL from the specified stream.
     *
     * @param is
     *            Stream to load CRL from
     * @return The CRL
     */
    public static X509CRL loadCRL(final InputStream is) {
        try {
            final CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);
            final X509CRL crl = (X509CRL) cf.generateCRL(is);
            return crl;
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (final CRLException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    /**
     * Convert the supplied array of certificate objects into X509Certificate objects.
     *
     * @param certsIn
     *            The Certificate objects
     * @return The converted X509Certificate objects
     */
    public static List<X509Certificate> convertCertificates(final List<Certificate> certificates)
            throws NoSuchProviderException {

        if (certificates == null) {
            return Collections.emptyList();
        }

        final List<X509Certificate> x509Certificates = Lists.newArrayList();

        for (final Certificate certificate : certificates) {
            x509Certificates.add(convertCertificate(certificate));
        }
        return x509Certificates;
    }

    /**
     * Convert the supplied certificate object into an X509Certificate object.
     *
     * @param certIn
     *            The Certificate object
     * @return The converted X509Certificate object
     */
    public static X509Certificate convertCertificate(final Certificate certIn) {
        try {
            final CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);
            final ByteArrayInputStream bais = new ByteArrayInputStream(certIn.getEncoded());
            return (X509Certificate) cf.generateCertificate(bais);
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Order the supplied array of X.509 certificates in issued to issuer order.
     *
     * @param certs
     *            X.509 certificates
     * @return The ordered X.509 certificates
     */
    public static List<X509Certificate> orderX509CertChain(final List<X509Certificate> certs) {

        if (certs == null) {
            return Collections.emptyList();
        }

        if (certs.size() <= 1) {
            return certs;
        }

        // Put together each possible certificate path...
        final ArrayList<ArrayList<X509Certificate>> paths = new ArrayList<>();

        // For each possible path...
        for (X509Certificate issuerCert : certs) {
            // Each possible path assumes a different certificate is the root issuer
            final ArrayList<X509Certificate> path = new ArrayList<>();
            path.add(issuerCert);

            X509Certificate newIssuer = null;

            // Recursively build that path by finding the next issued certificate
            while ((newIssuer = findIssuedCert(issuerCert, certs)) != null) {
                // Found an issued cert, now attempt to find its issued certificate
                issuerCert = newIssuer;
                path.add(0, newIssuer);
            }

            // Path complete
            paths.add(path);
        }

        // Get longest path - this will be the ordered path
        ArrayList<X509Certificate> longestPath = paths.get(0);
        for (int i = 1; i < paths.size(); i++) {
            final ArrayList<X509Certificate> path = paths.get(i);
            if (path.size() > longestPath.size()) {
                longestPath = path;
            }
        }

        // Return longest path
        return longestPath;
    }

    private static X509Certificate findIssuedCert(final X509Certificate issuerCert, final List<X509Certificate> certs) {
        // Find a certificate issued by the supplied certificate based on distiguished name
        for (final X509Certificate cert : certs) {
            if (issuerCert.getSubjectX500Principal().equals(cert.getSubjectX500Principal())
                    && issuerCert.getIssuerX500Principal().equals(cert.getIssuerX500Principal())) {
                // Checked certificate is issuer - ignore it
                continue;
            }

            if (issuerCert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
                return cert;
            }
        }

        return null;
    }

    /**
     * X.509 encode a certificate.
     *
     * @return The encoding
     * @param cert
     *            The certificate
     */
    public static byte[] getCertEncodedX509(final X509Certificate cert) {
        try {
            return cert.getEncoded();
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * PKCS #7 encode a certificate.
     *
     * @return The encoding
     * @param cert
     *            The certificate
     */
    public static byte[] getCertEncodedPkcs7(final X509Certificate cert) {
        return getCertsEncodedPkcs7(new X509Certificate[] { cert });
    }

    /**
     * PKCS #7 encode a number of certificates.
     *
     * @return The encoding
     * @param certs
     *            The certificates
     */
    public static byte[] getCertsEncodedPkcs7(final X509Certificate[] certs) {
        try {
            final ArrayList<Certificate> encodedCerts = new ArrayList<>();

            Collections.addAll(encodedCerts, certs);

            final CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);

            final CertPath cp = cf.generateCertPath(encodedCerts);

            return cp.getEncoded(PKCS7_ENCODING);
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * PKI Path encode a certificate.
     *
     * @return The encoding
     * @param cert
     *            The certificate
     */
    public static byte[] getCertEncodedPkiPath(final X509Certificate cert) {
        return getCertsEncodedPkiPath(new X509Certificate[] { cert });
    }

    /**
     * PKI Path encode a number of certificates.
     *
     * @return The encoding
     * @param certs
     *            The certificates
     */
    public static byte[] getCertsEncodedPkiPath(final X509Certificate[] certs) {
        try {
            final ArrayList<Certificate> encodedCerts = new ArrayList<>();

            Collections.addAll(encodedCerts, certs);

            final CertificateFactory cf = CertificateFactory.getInstance(X509_CERT_TYPE);

            final CertPath cp = cf.generateCertPath(encodedCerts);

            return cp.getEncoded(PKI_PATH_ENCODING);
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Verify that one X.509 certificate was signed using the private key that corresponds to the public key of a second
     * certificate.
     *
     * @return True if the first certificate was signed by private key corresponding to the second signature
     * @param signedCert
     *            The signed certificate
     * @param signingCert
     *            The signing certificate
     */
    public static boolean verifyCertificate(final X509Certificate signedCert, final X509Certificate signingCert) {
        try {
            signedCert.verify(signingCert.getPublicKey());
            return true;
        }
        // Verification failed
        catch (final InvalidKeyException ex) {
            return false;
        } catch (final SignatureException ex) {
            return false;
        }
        // Problem verifying
        catch (final NoSuchProviderException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (final CertificateException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Check whether or not a trust path exists between the supplied X.509 certificate and and the supplied keystores
     * based on the trusted certificates contained therein, ie that a chain of trust exists between the supplied
     * certificate and a self-signed trusted certificate in the KeyStores.
     *
     * @return The trust chain, or null if trust could not be established
     * @param cert
     *            The certificate
     * @param keyStores
     *            The KeyStores
     */
    public static X509Certificate[] establishTrust(final X509Certificate cert, final KeyStore[] keyStores) {
        final ArrayList<X509Certificate> ksCerts = new ArrayList<>();

        for (final KeyStore keyStore : keyStores) {
            ksCerts.addAll(extractCertificates(keyStore));
        }

        return establishTrust(cert, ksCerts);
    }

    private static X509Certificate[] establishTrust(final X509Certificate cert, final List<X509Certificate> compCerts) {
        /*
         * Check whether or not a trust path exists between the supplied X.509 certificate and and the supplied
         * comparison certificates , ie that a chain of trust exists between the certificate and a self-signed trusted
         * certificate in the comparison set
         */

        for (int i = 0; i < compCerts.size(); i++) {
            final X509Certificate compCert = compCerts.get(i);

            // Verify of certificate issuer is sam as comparison certificate's subject
            if (cert.getIssuerX500Principal().equals(compCert.getSubjectX500Principal())) {
                // Verify if the comparison certificate's private key was used to sign the certificate
                if (X509CertHelper.verifyCertificate(cert, compCert)) {
                    // If the comparision certificate is self-signed then a chain of trust exists
                    if (compCert.getSubjectX500Principal().equals(compCert.getIssuerX500Principal())) {
                        return new X509Certificate[] { cert, compCert };
                    }

                    /*
                     * Otherwise try and establish a chain of trust from the comparison certificate against the other
                     * comparison certificates
                     */
                    final X509Certificate[] tmpChain = establishTrust(compCert, compCerts);
                    if (tmpChain != null) {
                        final X509Certificate[] trustChain = new X509Certificate[tmpChain.length + 1];

                        trustChain[0] = cert;

                        System.arraycopy(tmpChain, 0, trustChain, 1, tmpChain.length);

                        return trustChain;
                    }
                }
            }
        }

        return null; // No chain of trust
    }

    private static List<X509Certificate> extractCertificates(final KeyStore keyStore) {
        try {
            final List<X509Certificate> certs = new ArrayList<>();

            for (final Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                final String alias = aliases.nextElement();

                if (keyStore.isCertificateEntry(alias)) {
                    certs.add(X509CertHelper.convertCertificate(keyStore.getCertificate(alias)));
                }
            }

            return certs;
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Check whether or not a trusted certificate in the supplied KeyStore matches the supplied X.509 certificate.
     *
     * @param cert
     *            The certificate
     * @param keyStore
     *            The KeyStore
     * @return The alias of the matching certificate in the KeyStore or null if there is no match
     */
    public static String matchCertificate(final KeyStore keyStore, final X509Certificate cert) {
        try {
            for (final Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                final String alias = aliases.nextElement();
                if (keyStore.isCertificateEntry(alias)) {
                    final X509Certificate compCert = X509CertHelper.convertCertificate(keyStore.getCertificate(alias));

                    if (cert.equals(compCert)) {
                        return alias;
                    }
                }
            }
            return null;
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * For a given X.509 certificate get a representative alias for it in a KeyStore. For a self-signed certificate this
     * will be the subject's common name (if any). For a non-self-signed certificate it will be the subject's common
     * name followed by the issuer's common name in brackets. Aliases will always be in lower case.
     *
     * @param cert
     *            The certificate
     * @return The alias or a blank string if none could be worked out
     */
    public static String getCertificateAlias(final X509Certificate cert) {
        final X500Principal subject = cert.getSubjectX500Principal();
        final X500Principal issuer = cert.getIssuerX500Principal();

        final String subjectCn = extractCommonName(X500NameHelper.x500PrincipalToX500Name(subject));
        final String issuerCn = extractCommonName(X500NameHelper.x500PrincipalToX500Name(issuer));

        if (subjectCn == null) {
            return "";
        }

        if (issuerCn == null || subjectCn.equals(issuerCn)) {
            return subjectCn;
        }

        return MessageFormat.format("{0} ({1})", subjectCn, issuerCn);
    }

    private static String extractCommonName(final X500Name name) {
        for (final RDN rdn : name.getRDNs()) {
            final AttributeTypeAndValue atav = rdn.getFirst();

            if (atav.getType().equals(BCStyle.CN)) {
                return atav.getValue().toString();
            }
        }

        return null;
    }

    /**
     * Get short name for certificate. Common name if available, otherwise use entire distinguished name.
     *
     * @param cert
     *            Certificate
     * @return Short name
     */
    public static String getShortName(final X509Certificate cert) {
        final X500Name subject = X500NameHelper.x500PrincipalToX500Name(cert.getSubjectX500Principal());

        String shortName = extractCommonName(subject);

        if (shortName == null) {
            shortName = subject.toString();
        }

        return shortName;
    }

    /**
     * Is the supplied X.509 certificate self-signed?
     *
     * @param cert
     *            The certificate
     * @return True if it is
     */
    public static boolean isCertificateSelfSigned(final X509Certificate cert) {
        return cert.getIssuerX500Principal().equals(cert.getSubjectX500Principal());
    }

    /**
     * <p>
     * Adds all the elements of the given arrays into a new array.
     * <p>
     * The new array contains all of the element of {@code array1} followed by all of the elements {@code array2}. When
     * an array is returned, it is always a new array.
     *
     * <pre>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * </pre>
     *
     * @param array1
     *            the first array whose elements are added to the new array.
     * @param array2
     *            the second array whose elements are added to the new array.
     * @return The new byte[] array.
     */
    private static byte[] addAll(final byte[] array1, final byte... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final byte[] joinedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    /**
     * <p>
     * Clones an array returning a typecast result and handling {@code null}.
     * <p>
     * This method returns {@code null} for a {@code null} input array.
     *
     * @param array
     *            the array to clone, may be {@code null}
     * @return the cloned array, {@code null} if {@code null} input
     */
    private static byte[] clone(final byte[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

}
