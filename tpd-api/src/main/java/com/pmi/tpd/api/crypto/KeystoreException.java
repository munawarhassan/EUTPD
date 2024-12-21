package com.pmi.tpd.api.crypto;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/***
 * thrown when error occurred during key store operations
 *
 * @author christophe Friederich
 */
public class KeystoreException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public KeystoreException(final @Nonnull KeyedMessage message) {
        super(message);

    }

    public KeystoreException(final @Nonnull KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }

}
