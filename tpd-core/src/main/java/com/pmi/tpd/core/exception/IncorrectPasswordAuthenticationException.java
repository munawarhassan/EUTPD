package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.security.AuthenticationException;

/**
 * A specialisation of {@link AuthenticationException}, thrown to indicate the password supplied during an
 * authentication attempt does not match the password on record. It is also thrown when a user attempts to change their
 * password but the current password they provide does not match the password on record.
 * 
 * @author Christophe Friederich
 * @since 2.0
 */
public class IncorrectPasswordAuthenticationException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = -4363935800275301483L;

    public IncorrectPasswordAuthenticationException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}