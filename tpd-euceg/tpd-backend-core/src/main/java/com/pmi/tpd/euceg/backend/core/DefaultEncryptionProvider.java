package com.pmi.tpd.euceg.backend.core;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.notNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.eu.ceg.AS4Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.context.IApplicationProperties;
import com.pmi.tpd.api.crypto.IKeyProvider;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.backend.core.spi.AesKeyGenerator;
import com.pmi.tpd.euceg.backend.core.spi.IKeyGenerator;
import com.pmi.tpd.euceg.backend.core.support.ByteArrayDataSource;

/**
 * Provide methods to encrypt and decrypt content associated to {@link IKeyGenerator} key encryption method.
 * <p>
 * the default encryption method used is {@link AesKeyGenerator}.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultEncryptionProvider implements IEncryptionProvider {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEncryptionProvider.class);

    private final IApplicationProperties applicationProperties;

    /** */
    private IKeyGenerator keyGenerator;

    /** */
    private final IKeyProvider securityKeyProvider;

    /** */
    private BackendProperties backendProperties;

    /** */
    private boolean checkHashContent;

    public DefaultEncryptionProvider(@Nonnull final IKeyProvider securityKeyProvider) throws BackendException {
        this(securityKeyProvider, null, null);
    }

    /**
     * @param applicationProperties
     *                              the properties
     * @param securityKeyProvider
     *                              the security key provider.
     * @throws BackendException
     *                          if errors occurs.
     */
    public DefaultEncryptionProvider(@Nonnull final IKeyProvider securityKeyProvider,
            @Nullable final IApplicationProperties applicationProperties) throws BackendException {
        this(securityKeyProvider, applicationProperties, null);
    }

    /**
     * @param applicationProperties
     *                              the application properties
     * @param securityKeyProvider
     *                              the security key provider.
     * @param keyGenerator
     *                              the key generator.
     * @throws BackendException
     *                          if errors occurs.
     */
    public DefaultEncryptionProvider(@Nonnull final IKeyProvider securityKeyProvider,
            @Nullable final IApplicationProperties applicationProperties, @Nullable final IKeyGenerator keyGenerator)
            throws BackendException {
        this.applicationProperties = applicationProperties;
        this.securityKeyProvider = checkNotNull(securityKeyProvider, "securityKeyProvider");
        this.keyGenerator = keyGenerator;
        if (this.keyGenerator == null) {
            this.keyGenerator = new AesKeyGenerator();
        }
        this.checkHashContent = false;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCheckHashContent() {
        return checkHashContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCheckHashContent(final boolean check) {
        this.checkHashContent = check;
    }

    public void setBackendProperties(final BackendProperties backendProperties) {
        this.backendProperties = backendProperties;
    }

    public BackendProperties getBackendProperties() {
        if (this.backendProperties != null) {
            return this.backendProperties;
        }
        notNull(applicationProperties);
        return applicationProperties.getConfiguration(BackendProperties.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull AS4Payload createAs4Payload(@Nonnull final Object payload, @Nullable Path workingPath)
            throws BackendException {
        checkNotNull(payload, "payload");
        if (workingPath == null) {
            final String tmpDirsLocation = System.getProperty("java.io.tmpdir");
            workingPath = Path.of(tmpDirsLocation);
        }
        Assert.state(Files.exists(workingPath), "workingPath should be exist");

        if (payload instanceof String) {
            return encryptPayload(((String) payload).getBytes(Eucegs.getDefaultCharset()));
        } else if (payload instanceof char[]) {

            return encryptPayload(new String((char[]) payload).getBytes(Eucegs.getDefaultCharset()));
        } else if (payload instanceof byte[]) {

            return encryptPayload((byte[]) payload);
        }
        // else it is an euceg object -> need to marshalling
        final AS4Payload encryptedPayload = new AS4Payload();
        String payloadHash = null;

        File payloadFile = null;
        // get digest SHA-512
        InputStream in = null;
        try {
            // store xml payload in file
            payloadFile = Eucegs.marshallInFile(payload, workingPath);
            in = new FileInputStream(payloadFile);
            // generate digest SHA-512
            payloadHash = getHash(in);
        } catch (final IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BackendException(ex.getMessage(), ex);
        } finally {
            Closeables.closeQuietly(in);
        }
        encryptedPayload.setDocumentHash(payloadHash);
        final byte[] clearKey = generateKey();
        final byte[] encryptedKey = encryptKey(clearKey);
        encryptedPayload.setKey(encryptedKey);

        // store encrypted payload in temporary file
        Path encryptedFile = null;
        try {
            in = applyEncryption(new FileInputStream(payloadFile), clearKey);
            encryptedFile = workingPath.resolve(Eucegs.uuid() + ".xml");
            Files.copy(in, encryptedFile);
        } catch (final IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new BackendException(ex.getMessage(), ex);
        } finally {
            Closeables.closeQuietly(in);
        }
        encryptedPayload.setContent(new DataHandler(new FileDataSource(encryptedFile.toFile())));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully encrypted payload with clearkey {} and hash {}.",
                new String(Hex.encodeHex(clearKey)),
                payloadHash);
            LOGGER.debug("Encrypted key is : {}", new String(Hex.encodeHex(encryptedKey)));
        }
        return encryptedPayload;
    }

    @Override
    public DataSource encrypt(final byte[] clearPayload) throws BackendException {
        final byte[] clearKey = generateKey();
        final byte[] encryptedContent = encryptContent(clearPayload, clearKey);
        return new ByteArrayDataSource(encryptedContent, "application/x-binary");
    }

    /**
     * Encrpyt a payload.
     *
     * @param clearPayload
     *                     the payload to encrypt.
     * @return the encrypted payload
     * @throws BackendException
     *                          if error occurs
     */
    @Nonnull
    private AS4Payload encryptPayload(final byte[] clearPayload) throws BackendException {
        final AS4Payload encryptedPayload = new AS4Payload();
        final String payloadHash = getHash(clearPayload);
        encryptedPayload.setDocumentHash(payloadHash);
        final byte[] clearKey = generateKey();
        final byte[] encryptedKey = encryptKey(clearKey);
        encryptedPayload.setKey(encryptedKey);
        final byte[] encryptedContent = encryptContent(clearPayload, clearKey);

        final DataHandler handler = new DataHandler(new ByteArrayDataSource(encryptedContent, "application/x-binary"));
        encryptedPayload.setContent(handler);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully encrypted payload with clearkey {} and hash {}.",
                new String(Hex.encodeHex(clearKey)),
                payloadHash);
            LOGGER.debug("Encrypted key is : {}", new String(Hex.encodeHex(encryptedKey)));
        }
        return encryptedPayload;
    }

    /**
     * Encrypt the symmetric key using public key of the EU.
     *
     * @param clearKey
     * @return encrypted key
     * @throws BackendException
     */
    private byte[] encryptKey(final byte[] clearKey) throws BackendException {
        try {
            final String certificateName = getBackendProperties().getTrustedCertificateAlias();
            final Certificate certificate = securityKeyProvider.getCertificate(certificateName)
                    .orElseThrow(
                        () -> new BackendException("Alias  name of certificate'" + certificateName + "' is unknowm"));
            final PublicKey publicKey = certificate.getPublicKey();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Encrypting key with public key {} from certificate {}.",
                    new String(Hex.encodeHex(publicKey.getEncoded())),
                    certificateName);
            }
            final Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm() + "/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            final byte[] encryptedKey = cipher.doFinal(clearKey);
            return encryptedKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.error("Error while encrypting key", e);
            throw new BackendException("Error while encrypting key", e);
        }
    }

    /**
     * Encrypt given content with the given key, managing any needed cypher initialization.
     *
     * @param clearPayload
     * @param clearKey
     * @return encrypted payload
     * @throws BackendException
     */
    private byte[] encryptContent(final byte[] clearPayload, final byte[] clearKey) throws BackendException {
        final boolean forEncryption = true;
        final PaddedBufferedBlockCipher aesCypher = initAesCypher(clearKey, forEncryption);
        final ByteArrayInputStream clearPayloadStream = new ByteArrayInputStream(clearPayload);
        try (CipherInputStream cipherInputStream = new CipherInputStream(clearPayloadStream, aesCypher)) {
            final byte[] encryptedPayload = ByteStreams.toByteArray(cipherInputStream);
            return encryptedPayload;
        } catch (final IOException e) {
            LOGGER.error("Error while encrypting content", e);
            throw new BackendException("Error while encrypting content", e);
        }
    }

    /**
     * Encrypt given content with the given key, managing any needed cypher initialization.
     *
     * @param clearPayloadStream
     * @param clearKey
     * @return encrypted payload
     * @throws BackendException
     */
    private InputStream applyEncryption(final InputStream clearPayloadStream, final byte[] clearKey)
            throws BackendException {
        final boolean forEncryption = true;
        final PaddedBufferedBlockCipher aesCypher = initAesCypher(clearKey, forEncryption);
        return new CipherInputStream(clearPayloadStream, aesCypher);
    }

    /**
     * Init a fast buffered AES engine, in CBC mode, with PKCS7 padding.
     *
     * @param clearKey
     * @param forEncryption
     *                      : True if encrypt, false if decrypt
     * @return initialized cypher
     */
    private PaddedBufferedBlockCipher initAesCypher(final byte[] clearKey, final boolean forEncryption) {
        final CBCBlockCipher cbcBlockCipher = new CBCBlockCipher(new AESEngine());
        final PKCS7Padding pkcs7Padding = new PKCS7Padding();
        final PaddedBufferedBlockCipher paddedBufferedBlockCipher = new PaddedBufferedBlockCipher(cbcBlockCipher,
                pkcs7Padding);
        final KeyParameter keyParameter = new KeyParameter(clearKey);
        paddedBufferedBlockCipher.init(forEncryption, keyParameter);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized AES cypher with key {} for {}",
                new String(Hex.encodeHex(clearKey)),
                forEncryption ? "encryption" : "decryption");
        }
        return paddedBufferedBlockCipher;
    }

    /**
     * Generate a cryptographicaly random AES-256 Key for uniq use.
     *
     * @return Returns a specific generated random AES-256 Key for uniq use.
     */
    protected byte[] generateKey() {
        final byte[] encodedKey = keyGenerator.getEncodedKey();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generated key {}", new String(Hex.encodeHex(encodedKey)));
        }
        return encodedKey;
    }

    /**
     * @param clearPayload
     * @return SHA-512 of the payload, as UTF-8, unsalted, as hex string
     */
    private String getHash(final byte[] clearPayload) throws BackendException {
        return DigestUtils.sha512Hex(clearPayload);
    }

    /**
     * @param stream
     * @return SHA-512 of the payload, as UTF-8, unsalted, as hex string
     * @throws BackendException
     *                          On error reading from the stream.
     */
    private String getHash(final InputStream stream) throws BackendException {
        try {
            return DigestUtils.sha512Hex(stream);
        } catch (final IOException e) {
            // Shouldn't happen
            LOGGER.error("Error while calculating hash", e);
            throw new BackendException("Error while calculating hash", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull byte[] decryptContent(final @Nonnull DataSource encryptedPayload) throws BackendException {

        String calculatedHash;
        String documentHash;
        byte[] clearKey;
        byte[] encryptedKey;
        byte[] clearContent;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("DecryptContent As4Payload");
            LOGGER.debug("-------------------------------------------");
        }
        try (InputStream in = encryptedPayload.getInputStream()) {
            final AS4Payload as4payload = Eucegs.unmarshal(in);

            try (InputStream content = as4payload.getContent().getInputStream()) {
                final byte[] encryptedContent = ByteStreams.toByteArray(content);
                documentHash = as4payload.getDocumentHash();
                encryptedKey = as4payload.getKey();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Encrypted key is : {}", new String(Hex.encodeHex(encryptedKey)));
                }
                clearKey = decryptKey(encryptedKey);
                clearContent = decryptContent(encryptedContent, clearKey);
                calculatedHash = getHash(clearContent);
            } catch (final IOException ex) {
                throw ex;
            }
        } catch (final IOException e) {
            LOGGER.error("Error while decrypting content", e);
            throw new BackendException("Error while decrypting content", e);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Document Hash {} and calculated hash {}", documentHash, calculatedHash);
            LOGGER.debug("Decrypting content with clear key {}", new String(Hex.encodeHex(clearKey)));
            LOGGER.debug("Encrypted key was : ", new String(encryptedKey));
        }
        if (checkHashContent && !documentHash.equalsIgnoreCase(calculatedHash)) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Calculated hash {} does not match the associated hash {}.", calculatedHash, documentHash);
            }
            throw new InvalidParameterException("Calculated hash does not match the associated hash. "
                    + "This means the message can have been altered and is invalid.");
        }
        return clearContent;
    }

    /**
     * Decrypt content of the message using provided clear key.
     *
     * @param encryptedContent
     * @param clearKey
     * @return
     * @throws IOException
     *                     on configuration error
     */
    @SuppressWarnings("null")
    @Nonnull
    private byte[] decryptContent(final byte[] encryptedContent, final byte[] clearKey) throws BackendException {
        final boolean forEncryption = false;
        final PaddedBufferedBlockCipher aesCypher = initAesCypher(clearKey, forEncryption);
        final ByteArrayInputStream encryptedPayloadStream = new ByteArrayInputStream(encryptedContent);
        try (CipherInputStream cipherInputStream = new CipherInputStream(encryptedPayloadStream, aesCypher)) {
            final byte[] decryptedPayload = ByteStreams.toByteArray(cipherInputStream);
            return decryptedPayload;
        } catch (final IOException e) {
            LOGGER.error("Error while decrypting content", e);
            throw new BackendException("Error while decrypting content", e);
        }
    }

    /**
     * Decrypt the one time key used for the message, using the private PKI key.
     *
     * @param encryptedKey
     * @return
     * @throws BackendException
     */
    @Nonnull
    private byte[] decryptKey(final @Nonnull byte[] encryptedKey) throws BackendException {
        try {
            final String privateKeyAlias = getBackendProperties().getKeyPairAlias();
            final Key privateKey = securityKeyProvider.getKey(privateKeyAlias)
                    .orElseThrow(
                        () -> new BackendException("Alias  name of private key'" + privateKeyAlias + "' is unknowm"));
            final Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm() + "/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            final byte[] decryptedKey = cipher.doFinal(encryptedKey);
            return decryptedKey;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            LOGGER.error("Error while decrypting key", e);
            throw new BackendException("Error while decrypting key", e);
        }
    }

}
