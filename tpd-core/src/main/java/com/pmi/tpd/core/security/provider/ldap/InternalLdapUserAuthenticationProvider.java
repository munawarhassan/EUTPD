package com.pmi.tpd.core.security.provider.ldap;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.provider.AbstractAuthenticationProvider;
import com.pmi.tpd.core.security.provider.DefaultDirectory;
import com.pmi.tpd.core.security.provider.IAuthenticationProvider;
import com.pmi.tpd.core.security.provider.IDelegateAuthenticationProviderAware;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.IGroup;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 * @param <T>
 *            type of ldap configuration.
 */
public class InternalLdapUserAuthenticationProvider<T extends LdapAuthenticationProperties>
        extends AbstractAuthenticationProvider implements IDelegateAuthenticationProviderAware {

    /** */
    protected final T configuration;

    /** */
    protected AuthenticationProvider provider;

    /** */
    protected ContextSource contextSource;

    /** */
    protected boolean active = false;

    /** */
    private IAuthenticationProvider delegate;

    /**
     * <p>
     * Constructor for {@link InternalLdapUserAuthenticationProvider}.
     * </p>
     *
     * @param configuration
     *            a ldap configuration.
     */
    public InternalLdapUserAuthenticationProvider(@Nonnull final T configuration) {
        super(null);
        this.configuration = checkNotNull(configuration, "configuration");
    }

    /**
     * @param delegate
     *            the delegate authentication provider.
     */
    @Override
    public void setDelegate(final IAuthenticationProvider delegate) {
        this.delegate = delegate;
    }

    /**
     * Initialise ldap provider.
     *
     * @throws Exception
     *             if the creation of delegate provider failed.
     */
    @PostConstruct
    public void init() throws Exception {
        checkNotNull(delegate, "delegate");
        this.provider = buildProvider();
        this.contextSource = buildContextSource();
        active = true;
    }

    /**
     * Destroy LDAP context source.
     *
     * @throws Exception
     *             if destroy failed.
     */
    @PreDestroy
    public void shutdown() throws Exception {
        if (contextSource != null && contextSource instanceof DisposableBean) {
            ((DisposableBean) contextSource).destroy();
        }
        active = false;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    /**
     * @return Returns a new instance of {@link ContextSource}.
     */
    protected ContextSource buildContextSource() {
        final DefaultSpringSecurityContextSource contextSource = new DefaultSpringSecurityContextSource(
                getProviderUrl());
        contextSource.setBaseEnvironmentProperties(
            ImmutableMap.<String, Object> builder().put("java.naming.ldap.attributes.binary", "objectGUID").build());
        final String managerDn = configuration.getUsername();
        if (managerDn != null) {
            final String managerPassword = configuration.getPassword();
            contextSource.setUserDn(managerDn);
            if (managerPassword == null) {
                throw new IllegalStateException("managerPassword is required if managerDn is supplied");
            }
            contextSource.setPassword(managerPassword);
        }
        contextSource.setDirObjectFactory(DefaultDirObjectFactory.class);
        contextSource.setPooled(false);
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Override
    public void checkConnection() {
        this.authenticate(
            new UsernamePasswordAuthenticationToken(configuration.getUsername(), configuration.getPassword()));
    }

    /**
     * @return Returns new instance of {@link AuthenticationProvider}.
     * @throws Exception
     *             in the event of misconfiguration (such as failure to set an essential property) or if initialization
     *             fails.
     */
    protected AuthenticationProvider buildProvider() throws Exception {
        final BindAuthenticator ldapAuthenticator = new BindAuthenticator((BaseLdapPathContextSource) contextSource);
        ldapAuthenticator.afterPropertiesSet();
        return new LdapAuthenticationProvider(ldapAuthenticator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDirectory getSupportedDirectory() {
        return UserDirectory.InternalLdap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDirectory getDirectory() {
        return new DefaultDirectory(getSupportedDirectory().getDescription(), Collections.emptyList(), active);
    }

    /**
     * @return Returns the current configuration.
     */
    protected T getConfiguration() {
        return configuration;
    }

    /**
     * @return Returns a {@link String} representing a url used by provider.
     */
    protected String getProviderUrl() {
        final String schema = configuration.getPort() == 636 ? "ldaps://" : "ldap://";
        return schema + configuration.getHostname() + ":" + configuration.getPort();

    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return delegate.loadUserByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<String> findGroupsByName(@Nullable final String groupName, final Pageable pageRequest) {
        return delegate.findGroupsByName(groupName, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<IUser> findUsersByName(@Nullable final String username, final Pageable pageRequest) {
        return delegate.findUsersByName(username, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IGroup findGroupByName(final String groupName) {
        return delegate.findGroupByName(groupName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IUser findUserByName(final String username) {
        return delegate.findUserByName(username);
    }

    /**
     * {@inheritDoc}
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
    public boolean supports(final Class<?> authentication) {
        return provider.supports(authentication);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.springframework.security.crypto.password.PasswordEncoder getPasswordEncoder() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note:</b>Not supported
     * </p>
     */
    @Override
    protected void setPasswordEncoder(final PasswordEncoder passwordEncoder) {
        // not use passwordencoder
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note:</b>Not supported
     * </p>
     */
    @Override
    protected @Nonnull IUser loadUser(final String username) throws UsernameNotFoundException, DataAccessException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Note:</b>Not supported
     * </p>
     */
    @Override
    protected List<GrantedAuthority> getGrantedAuthorities(final IUser user) {
        throw new UnsupportedOperationException();
    }

}
