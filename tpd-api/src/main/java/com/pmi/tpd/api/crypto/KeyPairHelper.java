package com.pmi.tpd.api.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;

import com.google.common.collect.Lists;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class KeyPairHelper {

    private KeyPairHelper() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * @param pkcs12File
     * @param password
     * @return
     * @throws KeystoreException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchProviderException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static KeyPair extractKeyPairPkcs12(final File pkcs12File, final @Nonnull String password)
            throws FileNotFoundException, IOException, KeystoreException, UnrecoverableKeyException,
            NoSuchAlgorithmException, CertificateException, KeyStoreException, NoSuchProviderException {
        try (InputStream in = new FileInputStream(pkcs12File)) {
            return extractKeyPairPkcs12(in, password);
        }
    }

    /**
     * @param pkcs12File
     * @param password
     * @return
     * @throws KeystoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws NoSuchProviderException
     */
    public static KeyPair extractKeyPairPkcs12(final @Nonnull InputStream pkcs12File, @Nonnull final String password)
            throws KeystoreException, NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException,
            UnrecoverableKeyException, NoSuchProviderException {
        final String pkcs12Password = password;

        final KeyStore pkcs12 = KeyStoreHelper.load(pkcs12File, pkcs12Password, "PKCS12");

        // Find a key pair in the PKCS #12 KeyStore
        PrivateKey privKey = null;
        final ArrayList<Certificate> certsList = Lists.newArrayList();

        // Look for key pair entries first
        for (final Enumeration<String> aliases = pkcs12.aliases(); aliases.hasMoreElements();) {
            final String alias = aliases.nextElement();

            if (pkcs12.isKeyEntry(alias)) {
                privKey = (PrivateKey) pkcs12.getKey(alias, pkcs12Password.toCharArray());
                final Certificate[] certs = pkcs12.getCertificateChain(alias);
                if (certs != null && certs.length > 0) {
                    Collections.addAll(certsList, certs);
                    break;
                }
            }
        }

        // No key pair entries found - look for a key entry and certificate
        // entries
        if (privKey == null || certsList.size() == 0) {
            for (final Enumeration<String> aliases = pkcs12.aliases(); aliases.hasMoreElements();) {
                final String alias = aliases.nextElement();

                certsList.add(pkcs12.getCertificate(alias));
            }
        }

        final List<X509Certificate> certs = X509CertHelper.convertCertificates(certsList);
        return new KeyPair(privKey, certs);

    }

    /**
     * Get the information about the supplied public key.
     *
     * @param publicKey
     *                  The public key
     * @return Key information
     */
    public static KeyInfo getKeyInfo(final PublicKey publicKey) {
        try {
            final String algorithm = publicKey.getAlgorithm();

            if (algorithm.equals(KeyPairType.RSA.value())) {
                final KeyFactory keyFact = KeyFactory.getInstance(algorithm);
                final RSAPublicKeySpec keySpec = keyFact.getKeySpec(publicKey, RSAPublicKeySpec.class);
                final BigInteger modulus = keySpec.getModulus();
                return new KeyInfo(KeyType.ASYMMETRIC, algorithm, modulus.toString(2).length());
            } else if (algorithm.equals(KeyPairType.DSA.value())) {
                final KeyFactory keyFact = KeyFactory.getInstance(algorithm);
                final DSAPublicKeySpec keySpec = keyFact.getKeySpec(publicKey, DSAPublicKeySpec.class);
                final BigInteger prime = keySpec.getP();
                return new KeyInfo(KeyType.ASYMMETRIC, algorithm, prime.toString(2).length());
            } else if (algorithm.equals(KeyPairType.EC.value())) {
                final ECPublicKey pubk = (ECPublicKey) publicKey;
                final int size = pubk.getParams().getOrder().bitLength();
                return new KeyInfo(KeyType.ASYMMETRIC, algorithm, size);
            }

            return new KeyInfo(KeyType.ASYMMETRIC, algorithm); // size unknown
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get the information about the supplied private key.
     *
     * @param privateKey
     *                   The private key
     * @return Key information
     */
    public static KeyInfo getKeyInfo(final PrivateKey privateKey) {
        try {
            final String algorithm = privateKey.getAlgorithm();

            if (algorithm.equals(KeyPairType.RSA.value())) {
                if (privateKey instanceof RSAPrivateKey) {
                    // Using default provider does not work for BKS and UBER resident private keys
                    final KeyFactory keyFact = KeyFactory.getInstance(algorithm);
                    final RSAPrivateKeySpec keySpec = keyFact.getKeySpec(privateKey, RSAPrivateKeySpec.class);
                    final BigInteger modulus = keySpec.getModulus();
                    return new KeyInfo(KeyType.ASYMMETRIC, algorithm, modulus.toString(2).length());
                } else {
                    return new KeyInfo(KeyType.ASYMMETRIC, algorithm, 0);
                }
            } else if (algorithm.equals(KeyPairType.DSA.value())) {
                // Use SUN (DSA key spec not implemented for BC)
                final KeyFactory keyFact = KeyFactory.getInstance(algorithm);
                final DSAPrivateKeySpec keySpec = keyFact.getKeySpec(privateKey, DSAPrivateKeySpec.class);
                final BigInteger prime = keySpec.getP();
                return new KeyInfo(KeyType.ASYMMETRIC, algorithm, prime.toString(2).length());
            } else if (algorithm.equals(KeyPairType.EC.value())) {
                final ECPrivateKey pubk = (ECPrivateKey) privateKey;
                final int size = pubk.getParams().getOrder().bitLength();
                return new KeyInfo(KeyType.ASYMMETRIC, algorithm, size);
            }

            return new KeyInfo(KeyType.ASYMMETRIC, algorithm); // size unknown
        } catch (final GeneralSecurityException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Get the information about the supplied secret key.
     *
     * @param secretKey
     *                  The secret key
     * @return Key information
     */
    public static KeyInfo getKeyInfo(final SecretKey secretKey) {
        String algorithm = secretKey.getAlgorithm();

        if (algorithm.equals("RC4")) {
            algorithm = "ARC4"; // RC4 is trademarked can not be displayed
        }

        if (secretKey.getFormat().equals("RAW")) {
            final int keySize = secretKey.getEncoded().length * 8;
            return new KeyInfo(KeyType.SYMMETRIC, algorithm, keySize);
        } else {
            // Key size unknown
            return new KeyInfo(KeyType.SYMMETRIC, algorithm);
        }
    }

}
