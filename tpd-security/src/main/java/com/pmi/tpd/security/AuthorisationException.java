package com.pmi.tpd.security;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class AuthorisationException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public AuthorisationException(final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public AuthorisationException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }
}
