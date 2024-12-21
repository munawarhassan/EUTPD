package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.security.AuthenticationException;

/**
 * A specialization of {@link AuthenticationException} that should be thrown when an existing authentication has
 * expired.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public class ExpiredAuthenticationException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = 7153074253053567634L;

    public ExpiredAuthenticationException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    public ExpiredAuthenticationException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
