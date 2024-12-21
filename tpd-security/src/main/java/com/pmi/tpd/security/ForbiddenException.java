package com.pmi.tpd.security;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Indicates that a particular action can not be performed by the current user.
 *
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class ForbiddenException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -3402433313542949676L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public ForbiddenException(final KeyedMessage message) {
        super(message);
    }

}
