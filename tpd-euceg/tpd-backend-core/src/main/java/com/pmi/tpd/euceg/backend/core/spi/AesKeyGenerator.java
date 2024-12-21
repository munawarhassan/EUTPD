package com.pmi.tpd.euceg.backend.core.spi;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide crypto-random AES key generation.
 *
 * @author kfontain
 * @since 1.0
 */
public class AesKeyGenerator implements IKeyGenerator {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(AesKeyGenerator.class);

    /** */
    private final KeyGenerator keyGenerator;

    /**
     * Default Constructor.
     */
    public AesKeyGenerator() {
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
        } catch (final NoSuchAlgorithmException e) {
            // Shouldn't happen, AES is default
            LOGGER.error("Error while generating key", e);
            throw new RuntimeException("Error while generating key", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getEncodedKey() {
        final SecretKey generatedKey = keyGenerator.generateKey();
        final byte[] encodedKey = generatedKey.getEncoded();
        return encodedKey;
    }

}
