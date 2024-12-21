package com.pmi.tpd.core.security.provider.ldap;

import java.util.Collections;

import org.springframework.ldap.core.ContextSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.ActiveDirectoryAuthenticationProperties;
import com.pmi.tpd.core.security.provider.DefaultDirectory;
import com.pmi.tpd.core.security.provider.IDirectory;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 */
public class ActiveDirectoryUserAuthenticationProvider
        extends LdapUserAuthenticationProvider<ActiveDirectoryAuthenticationProperties> {

    /**
     * @param configuration
     *            a ldap configuration
     */
    public ActiveDirectoryUserAuthenticationProvider(final ActiveDirectoryAuthenticationProperties configuration) {
        super(configuration);
    }

    @Override
    protected ContextSource buildContextSource() {
        final ContextSource contextSource = ((ActiveDirectoryLdapAuthenticationProvider) provider)
                .createSourceContext(configuration.getUsername(), configuration.getPassword());
        return contextSource;
    }

    @Override
    protected AuthenticationProvider buildProvider() throws Exception {
        final ActiveDirectoryLdapAuthenticationProvider ad = new ActiveDirectoryLdapAuthenticationProvider(
                configuration.getDomain(), getProviderUrl(), configuration.getLdapSchema().getBaseDn());
        ad.setConvertSubErrorCodesToExceptions(true);
        ad.setUseAuthenticationRequestCredentials(true);
        return ad;
    }

    /**
     * {@inheritDoc}
     * <p>
     * use specific authentication of provider.
     * </p>
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final Authentication auth = provider.authenticate(authentication);
        return auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDirectory getSupportedDirectory() {
        return UserDirectory.ActiveDirectory;
    }

    @Override
    public IDirectory getDirectory() {
        return new DefaultDirectory(getSupportedDirectory().getDescription(), Collections.emptyList(), active);
    }

}
