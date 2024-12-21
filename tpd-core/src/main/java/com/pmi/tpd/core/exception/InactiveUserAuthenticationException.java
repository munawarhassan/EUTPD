package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.security.AuthenticationException;

/**
 * A specialisation of {@link AuthenticationException}, thrown to indicate the user cannot be authenticated because
 * their account is no longer active.
 * 
 * @author Christophe Friederich
 * @since 2.0
 */
public class InactiveUserAuthenticationException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InactiveUserAuthenticationException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
