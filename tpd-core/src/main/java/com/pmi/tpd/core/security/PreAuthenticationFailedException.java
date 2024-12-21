package com.pmi.tpd.core.security;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.security.AuthenticationException;

/**
 * A specialisation of {@link AuthenticationException}, thrown when the {@link ISecurityService} fails to authenticate
 * the do-as user.
 *
 * @author Christophe Friederich
 * @since 2.0
 * @see SecurityService#doAsUser(String, String,com.pmi.tpd.util.IOperation)
 */
public class PreAuthenticationFailedException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     * 
     * @param message
     *            e exception message key.
     */
    public PreAuthenticationFailedException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
