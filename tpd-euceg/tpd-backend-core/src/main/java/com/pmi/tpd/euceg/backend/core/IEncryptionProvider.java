package com.pmi.tpd.euceg.backend.core;

import java.nio.file.Path;

import javax.activation.DataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AS4Payload;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IEncryptionProvider {

    /**
     * Decrypt an response.
     *
     * @param encryptedPayload
     *                         the payload to decrypt.
     * @return the clear response.
     * @throws BackendException
     *                          if error occurs
     */
    @Nonnull
    byte[] decryptContent(@Nonnull DataSource encryptedPayload) throws BackendException;

    /**
     * Encrpyt a payload.
     *
     * @param payload
     *                    the payload stream to encrypt.
     * @param workingPath
     *                    the directory where create temporary file.
     * @return the encrypted payload
     * @throws BackendException
     *                          if error occurs
     */
    @Nonnull
    AS4Payload createAs4Payload(@Nonnull Object payload, @Nullable Path workingPath) throws BackendException;

    /**
     * @param clearPayload
     * @return
     * @throws BackendException
     */
    DataSource encrypt(final byte[] clearPayload) throws BackendException;

    /**
     * Gets indicating whether need check the hash of {@link AS4Payload#getContent() payload content}.
     *
     * @return Returns {@code true} whether need check the hash of {@link AS4Payload#getContent() payload content},
     *         otherwise {@code false}.
     */
    boolean isCheckHashContent();

    /**
     * Sets {@code true} need check the hash of {@link AS4Payload#getContent() payload content}, otherwise
     * {@code false}.
     *
     * @param check
     *              the status check.
     */
    void setCheckHashContent(boolean check);
}
