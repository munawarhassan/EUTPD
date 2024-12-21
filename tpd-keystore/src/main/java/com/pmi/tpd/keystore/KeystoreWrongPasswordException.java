package com.pmi.tpd.keystore;

import com.pmi.tpd.api.crypto.KeystoreException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/***
 * thrown when key store operation failed because of a wrong password
 *
 * @author christophe friederich
 */
public class KeystoreWrongPasswordException extends KeystoreException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public KeystoreWrongPasswordException(final KeyedMessage message) {
        super(message);

    }

    public KeystoreWrongPasswordException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }
}
