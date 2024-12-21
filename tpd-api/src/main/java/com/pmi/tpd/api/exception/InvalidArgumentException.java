package com.pmi.tpd.api.exception;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * {@code InvalidArgumentException} is the exception class representing a action using a invalid value.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.1
 */
public class InvalidArgumentException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -5361043841482871502L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public InvalidArgumentException(final KeyedMessage message) {
        super(message);
    }
}
