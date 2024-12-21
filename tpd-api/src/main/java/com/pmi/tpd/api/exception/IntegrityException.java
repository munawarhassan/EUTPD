package com.pmi.tpd.api.exception;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * {@code IntegrityException} is the exception class representing a unpermitted action due to a integrity constraint.
 * 
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class IntegrityException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -5361043841482871502L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public IntegrityException(final KeyedMessage message) {
        super(message);
    }
}
