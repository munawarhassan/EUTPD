package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.security.AuthenticationException;

/**
 * A specialisation of {@link AuthenticationException}, thrown to indicate the user cannot be authenticated because
 * their password has expired and must be changed.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class ExpiredPasswordAuthenticationException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ExpiredPasswordAuthenticationException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}