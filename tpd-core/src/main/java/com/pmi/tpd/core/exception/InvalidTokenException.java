package com.pmi.tpd.core.exception;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

public class InvalidTokenException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 8718974590825193186L;

    public InvalidTokenException(final KeyedMessage message, final String token) {
        super(message);
    }

}
