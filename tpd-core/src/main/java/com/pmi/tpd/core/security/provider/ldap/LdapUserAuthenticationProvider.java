package com.pmi.tpd.core.security.provider.ldap;

import static com.pmi.tpd.api.paging.PageUtils.asPageOf;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.ppolicy.PasswordPolicyException;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.provider.AbstractAuthenticationProvider;
import com.pmi.tpd.core.security.provider.DefaultDirectory;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.IGroup;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 * @param <T>
 *            type of ldap configuration.
 */
public class LdapUserAuthenticationProvider<T extends LdapAuthenticationProperties>
        extends AbstractAuthenticationProvider {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUserAuthenticationProvider.class);

    /** */
    protected final T configuration;

    /** */
    protected AuthenticationProvider provider;

    /** */
    protected ILdapGroupRepository groupRepository;

    /** */
    protected ILdapUserRepository userRepository;

    /** */
    protected SpringSecurityLdapTemplate ldapTemplate;

    /** */
    protected ContextSource contextSource;

    /** */
    protected boolean active = false;

    /**
     * <p>
     * Constructor for LdapUserAuthenticationProvider.
     * </p>
     *
     * @param configuration
     *            a ldap configuration.
     */
    public LdapUserAuthenticationProvider(final T configuration) {
        super(null);
        this.configuration = checkNotNull(configuration, "configuration");
    }

    /**
     * Initialise ldap provider.
     *
     * @throws Exception
     *             if the creation of delegate provider failed.
     */
    @PostConstruct
    public void init() throws Exception {
        this.provider = buildProvider();
        this.contextSource = buildContextSource();

        ldapTemplate = new SpringSecurityLdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(true);
        this.userRepository = new LdapUserRepository(ldapTemplate, configuration, getSupportedDirectory());
        this.groupRepository = new LdapGroupRepository(ldapTemplate, configuration);
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

    /**
     * @return Returns new instance of {@link AuthenticationProvider}.
     * @throws Exception
     *             in the event of misconfiguration (such as failure to set an essential property) or if initialization
     *             fails.
     */
    protected AuthenticationProvider buildProvider() throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDirectory getSupportedDirectory() {
        return UserDirectory.Ldap;
    }

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
    public @Nonnull IUser loadUser(final @Nonnull String username)
            throws UsernameNotFoundException, DataAccessException {
        final IUser user = this.userRepository.findByName(username);
        if (user == null) {
            throw new UsernameNotFoundException("The user " + username + " doesn't exists.");
        }
        return user;
    }

    @Override
    protected List<GrantedAuthority> getGrantedAuthorities(@Nonnull final IUser user) {
        final List<GrantedAuthority> authorities = Lists.newArrayList();
        if (user instanceof LdapUser) {
            for (final String memberof : ((LdapUser) user).getMemberOf()) {
                authorities.add(new SimpleGrantedAuthority(memberof));
            }
        }
        return authorities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<String> findGroupsByName(@Nullable final String groupName, final Pageable pageRequest) {
        return this.groupRepository.findByName(groupName, pageRequest).map(LdapGroup::getName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<IUser> findUsersByName(@Nullable final String username, final Pageable pageRequest) {
        return asPageOf(IUser.class, this.userRepository.findByName(username, pageRequest));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IGroup findGroupByName(final String groupName) {
        return this.groupRepository.findByName(groupName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IUser findUserByName(final String username) {
        return this.userRepository.findByName(username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        final String username = authentication.getName();
        final String password = (String) authentication.getCredentials();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Processing authentication request for user: " + username);
        }

        if (!StringUtils.hasLength(username)) {
            throw new BadCredentialsException("Empty Username");
        }

        if (!StringUtils.hasLength(password)) {
            throw new BadCredentialsException("Empty Password");
        }

        Assert.notNull(password, "Null password was supplied in authentication token");

        try {
            final LdapUser user = this.userRepository.authenticate(username, password);
            return new UsernamePasswordAuthenticationToken(user, password, getGrantedAuthorities(user));

        } catch (final PasswordPolicyException ppe) {
            // The only reason a ppolicy exception can occur during a bind is that the
            // account is locked.
            throw new LockedException("ppolicy exception", ppe);
        } catch (final UsernameNotFoundException notFound) {
            throw notFound;
        } catch (final NamingException ldapAccessFailure) {
            throw new InternalAuthenticationServiceException(ldapAccessFailure.getMessage(), ldapAccessFailure);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
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

    @Override
    public void checkConnection() {
        findUsersByName("toto", PageUtils.newRequest(0, 10));
    }

    /**
     * @param user
     *            the user to transform
     * @return Returns new instance of {@link UserDetails} representing the {@code user}.
     */
    protected UserDetails createUserDetails(final LdapUser user) {
        // Populating attributes
        final String username = user.getName();
        final String password = user.getPassword();
        final boolean enabled = user.isActivated();
        final boolean accountNonExpired = true;
        final boolean credentialsNonExpired = true;
        final boolean accountNonLocked = true;

        // Returning new user details
        return new org.springframework.security.core.userdetails.User(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, getGrantedAuthorities(user));
    }

}
