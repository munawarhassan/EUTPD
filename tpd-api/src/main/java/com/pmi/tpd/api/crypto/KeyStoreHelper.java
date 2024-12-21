package com.pmi.tpd.api.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.io.Closeables;

/**
 * Class utility for {@link KeyStore}.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 */
public final class KeyStoreHelper {

    private KeyStoreHelper() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * Loads this KeyStore from the given a {@link File file}.
     *
     * @param keyStoreFile
     *                     a File to load KeyStore from
     * @param password
     *                     the password used to check the integrity of the keystore, the password used to unlock the
     *                     keystore, or {@code null}
     * @param keyStoreType
     *                     The type of the KeyStore to open.
     * @return Returns a keystore object of the specified type.
     */
    public static KeyStore load(final File keyStoreFile, @Nullable final String password, final String keyStoreType) {
        try (InputStream in = new FileInputStream(keyStoreFile)) {
            return load(in, password, keyStoreType);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Load a KeyStore from a file accessed by a password.
     *
     * @param keyStoreFile
     *                     File to load KeyStore from
     * @param password
     *                     a password of the KeyStore
     * @param keyStoreType
     *                     The type of the KeyStore to open
     * @return Returns a keystore object of the specified type.
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */

    @SuppressWarnings("null")
    @Nonnull
    public static KeyStore load(@Nonnull final InputStream keyStoreFile,
        @Nullable final String password,
        final String keyStoreType) throws NoSuchAlgorithmException, CertificateException, IOException {

        final InputStream fis = keyStoreFile;
        KeyStore keyStore = null;
        try {
            keyStore = getKeyStoreInstance(keyStoreType);
            keyStore.load(fis, Strings.isNullOrEmpty(password) ? null : password.toCharArray());
        } finally {
            Closeables.closeQuietly(fis);
        }

        return keyStore;
    }

    /**
     * Gets a keystore object of the specified type.
     * <p>
     * This method traverses the list of registered security Providers, starting with the most preferred Provider. A new
     * KeyStore object encapsulating the KeyStoreSpi implementation from the first Provider that supports the specified
     * type is returned.
     * </p>
     * <p>
     * Note that the list of registered providers may be retrieved via the {@link java.security.Security#getProviders()
     * Security.getProviders()} method.
     * </p>
     *
     * @param keyStoreType
     *                     the type of keystore (can <b>not</b> be null).
     * @return Returns a keystore object of the specified type.
     * @see KeyStore#getInstance(String)
     */
    public static KeyStore getKeyStoreInstance(final String keyStoreType) {
        try {
            return KeyStore.getInstance(keyStoreType);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets the date from the validity period of the certificate.
     *
     * @param alias
     *                 the alias name (can <b>not</b> be null).
     * @param keyStore
     *                 the keystore used (can <b>not</b> be null).
     * @return Returns {@link Date} representing the end date of the validity period.
     */
    @Nullable
    public static Date getCertificateExpiry(@Nonnull final String alias, @Nonnull final KeyStore keyStore) {
        try {
            if (isTrustedCertificateEntry(alias, keyStore)) {
                return X509CertHelper.convertCertificate(keyStore.getCertificate(alias)).getNotAfter();
            } else {
                final Certificate[] chain = keyStore.getCertificateChain(alias);

                if (chain == null) {
                    // Key entry - no expiry date
                    return null;
                }

                // Key pair - first certificate in chain will be for the private key
                final List<X509Certificate> x509Chain = X509CertHelper
                        .orderX509CertChain(X509CertHelper.convertCertificates(Arrays.asList(chain)));
                return Iterables.getFirst(x509Chain, null).getNotAfter();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean getValidity(@Nonnull final String alias, @Nonnull final KeyStore keytore) {
        X509Certificate certificate = null;
        try {
            if (isTrustedCertificateEntry(alias, keytore)) {
                certificate = X509CertHelper.convertCertificate(keytore.getCertificate(alias));
            } else {
                final Certificate[] chain = keytore.getCertificateChain(alias);
                if (chain == null) {
                    // Key entry - no expiry date
                    return true;
                }

                // Key pair - first certificate in chain will be for the private key
                final List<X509Certificate> x509Chain = X509CertHelper
                        .orderX509CertChain(X509CertHelper.convertCertificates(Arrays.asList(chain)));
                certificate = Iterables.getFirst(x509Chain, null);

            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            certificate.checkValidity();
            return true;
        } catch (final CertificateException ex) {
        }
        return false;
    }

    public static boolean getExpired(@Nonnull final String alias, @Nonnull final KeyStore keystore) {
        X509Certificate certificate = null;
        try {
            if (isTrustedCertificateEntry(alias, keystore)) {
                certificate = X509CertHelper.convertCertificate(keystore.getCertificate(alias));
            } else {
                final Certificate[] chain = keystore.getCertificateChain(alias);
                if (chain == null) {
                    // Key entry - no expiry date
                    return true;
                }

                // Key pair - first certificate in chain will be for the private key
                final List<X509Certificate> x509Chain = X509CertHelper
                        .orderX509CertChain(X509CertHelper.convertCertificates(Arrays.asList(chain)));
                certificate = Iterables.getFirst(x509Chain, null);

            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try {
            certificate.checkValidity();
        } catch (final CertificateExpiredException ex) {
            return true;
        } catch (final CertificateNotYetValidException e) {
        }
        return false;

    }

    /**
     * Get the information about the supplied public key.
     *
     * @param alias
     *                 the alias name (can <b>not</b> be null).
     * @param keyStore
     *                 the keystore used (can <b>not</b> be null).
     * @param password
     *                 the password for recovering the key (can be null).
     * @return Returns key information.
     */
    @Nullable
    @CheckReturnValue
    public static KeyInfo getKeyInfo(@Nonnull final String alias,
        @Nonnull final KeyStore keyStore,
        @Nullable final String password) {
        try {
            if (isTrustedCertificateEntry(alias, keyStore)) {
                // Get key info from certificate
                final X509Certificate cert = X509CertHelper.convertCertificate(keyStore.getCertificate(alias));
                return KeyPairHelper.getKeyInfo(cert.getPublicKey());
            } else {
                final Certificate[] chain = keyStore.getCertificateChain(alias);

                if (chain != null) {
                    // Key pair - first certificate in chain will be for the private key
                    final List<X509Certificate> x509Chain = X509CertHelper
                            .orderX509CertChain(X509CertHelper.convertCertificates(Arrays.asList(chain)));

                    return KeyPairHelper.getKeyInfo(Iterables.getFirst(x509Chain, null).getPublicKey());
                } else {
                    // need password of key to continue
                    // final Key entry - get final key info if final entry is unlocked
                    @SuppressWarnings("null")
                    final char[] keyPassword = Strings.isNullOrEmpty(password) ? null : password.toCharArray();

                    final Key key = keyStore.getKey(alias, keyPassword);
                    if (key instanceof SecretKey) {
                        return KeyPairHelper.getKeyInfo((SecretKey) key);
                    } else if (key instanceof PrivateKey) {
                        return KeyPairHelper.getKeyInfo((PrivateKey) key);
                    } else if (key instanceof PublicKey) {
                        return KeyPairHelper.getKeyInfo((PublicKey) key);
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets indicating whether is the named entry in the KeyStore a key pair entry.
     *
     * @param alias
     *                 the alias name (can <b>not</b> be null).
     * @param keyStore
     *                 the keystore used (can <b>not</b> be null).
     * @return {@code true} if it is, {@code false} otherwise
     */
    public static boolean isKeyPairEntry(@Nonnull final String alias, @Nonnull final KeyStore keyStore) {
        try {
            return keyStore.isKeyEntry(alias) && keyStore.getCertificateChain(alias) != null
                    && keyStore.getCertificateChain(alias).length != 0;
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets indicating whether is the named entry in the KeyStore a key entry?
     *
     * @param alias
     *                 the alias name (can <b>not</b> be null).
     * @param keyStore
     *                 the keystore used (can <b>not</b> be null).
     * @return {@code true} if it is, {@code false} otherwise
     */
    public static boolean isKeyEntry(@Nonnull final String alias, @Nonnull final KeyStore keyStore) {
        try {
            return keyStore.isKeyEntry(alias)
                    && (keyStore.getCertificateChain(alias) == null || keyStore.getCertificateChain(alias).length == 0);
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Is the named entry in the KeyStore a trusted certificate entry?
     *
     * @param alias
     *                 the alias name (can <b>not</b> be null).
     * @param keyStore
     *                 the keystore used (can <b>not</b> be null).
     * @return True if it is, false otherwise
     */
    public static boolean isTrustedCertificateEntry(@Nonnull final String alias, @Nonnull final KeyStore keyStore) {
        try {
            return keyStore.isCertificateEntry(alias);
        } catch (final KeyStoreException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
