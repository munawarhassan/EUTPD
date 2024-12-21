package com.pmi.tpd.scheduler.exec;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class IncorrectTokenException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String token;

    public IncorrectTokenException(@Nonnull final KeyedMessage message, final String token) {
        super(message);

        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
