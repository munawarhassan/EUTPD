package com.pmi.tpd.core.security.provider;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.security.spring.RememberMeUserAuthenticationToken;

/**
 * An {@link AuthenticationProvider} implementation that validates {@link RememberMeAuthenticationToken}s.
 * <p>
 * To be successfully validated, the {@link RememberMeAuthenticationToken#getKeyHash()} must match this class'
 * {@link #getKey()}.
 */
public class RememberMeAuthenticationProvider implements AuthenticationProvider, InitializingBean, MessageSourceAware {

    // ~ Instance fields
    // ================================================================================================
    /** */
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    /** */
    private final String key;

    /**
     * Default constructor.
     *
     * @param key
     *            rememberme secret key
     */
    public RememberMeAuthenticationProvider(final String key) {
        Assert.hasLength(key, "key must have a length");
        this.key = key;
    }

    // ~ Methods
    // ========================================================================================================

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.messages, "A message source must be set");
    }

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        if (this.key.hashCode() != ((RememberMeUserAuthenticationToken) authentication).getKeyHash()) {
            throw new BadCredentialsException(messages.getMessage("RememberMeAuthenticationProvider.incorrectKey",
                "The presented RememberMeAuthenticationToken does not contain the expected key"));
        }

        return authentication;
    }

    /**
     * @return Returns the rememberme secret key.
     */
    public String getKey() {
        return key;
    }

    @Override
    public void setMessageSource(final MessageSource messageSource) {
        this.messages = new MessageSourceAccessor(messageSource);
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return RememberMeUserAuthenticationToken.class.isAssignableFrom(authentication);
    }
}