package com.pmi.tpd.core.security.provider;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.lifecycle.ConfigurationChangedEvent;
import com.pmi.tpd.api.lifecycle.IShutdown;
import com.pmi.tpd.api.lifecycle.IStartable;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.exception.NoSecurityConfigurationException;
import com.pmi.tpd.core.security.IAuthenticationSynchroniser;
import com.pmi.tpd.core.security.configuration.IAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.SecurityProperties;
import com.pmi.tpd.core.security.provider.ldap.IAuthenticationCheckConnection;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultAuthentificationProviderService
        implements IAuthenticationProviderService, IAuthenticationCheckConnection, IStartable, IShutdown {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthentificationProviderService.class);

    /** */
    @Nonnull
    private List<IAuthenticationProvider> authenticationProviders = Collections.emptyList();

    /** */
    @Nonnull
    private IUserRepository userRepository;

    /** */
    @Nonnull
    private IGroupRepository groupRepository;

    /** */
    private Provider<IAuthenticationSynchroniser> authenticationSynchroniserProvider;

    /** */
    @Nonnull
    private Provider<IPermissionService> permissionServiceProvider;

    /** */
    private PasswordEncoder passwordEncoder;

    /** */
    private ApplicationContext applicationContext;

    /** */
    private SecurityProperties securityConfiguration;

    /** */
    private I18nService i18nService;

    /** indicate if a external provider is configured and started. */
    protected boolean started = false;

    @SuppressWarnings("null")
    @VisibleForTesting
    DefaultAuthentificationProviderService() {
    }

    /**
     * @param userRepository
     *            a {@link IUserRepository} object.
     * @param groupRepository
     *            a {@link IGroupRepository} object.
     * @param authenticationSynchroniserProvider
     *            a authentication synchroniser.
     * @param permissionServiceProvider
     *            provider of permission service.
     * @param passwordEncoder
     *            a specific password encoder.
     * @param applicationContext
     *            a Spring application context.
     * @param applicationProperties
     *            a global application properties.
     * @param i18nService
     *            i18n service.
     */
    @Inject
    public DefaultAuthentificationProviderService(@Nonnull final IUserRepository userRepository,
            @Nonnull final IGroupRepository groupRepository,
            @Nonnull final Provider<IPermissionService> permissionServiceProvider,
            @Nonnull final Provider<IAuthenticationSynchroniser> authenticationSynchroniserProvider,
            @Nonnull final PasswordEncoder passwordEncoder, @Nonnull final ApplicationContext applicationContext,
            final SecurityProperties securityConfiguration, @Nonnull final I18nService i18nService) {
        this.userRepository = checkNotNull(userRepository, "userRepository");
        this.groupRepository = checkNotNull(groupRepository, "groupRepository");
        this.authenticationSynchroniserProvider = checkNotNull(authenticationSynchroniserProvider,
            "authenticationSynchroniserProvider");
        this.permissionServiceProvider = checkNotNull(permissionServiceProvider, "permissionServiceProvider");
        this.passwordEncoder = checkNotNull(passwordEncoder, "passwordEncoder");
        this.applicationContext = checkNotNull(applicationContext, "applicationContext");
        this.securityConfiguration = checkNotNull(securityConfiguration, "securityConfiguration");
        this.i18nService = checkNotNull(i18nService, "i18nService");

    }

    /**
     * Initialize the default authentication provider (internal by default).
     */
    @PostConstruct
    public void init() {
        // add default internal provider
        final IAuthenticationProvider surrogateProvider = createInternalAuthenticationProvider();
        this.authenticationProviders = Collections.unmodifiableList(Lists.newArrayList(surrogateProvider));
    }

    /**
     * Configure with the {@link SecurityProperties configuration}.
     *
     * @param authenticationConfiguration
     *            a configuration to use.
     */
    protected void configure() {
        this.securityConfiguration.applyDefaultValue();
        // add default internal provider
        final IAuthenticationProvider surrogateProvider = createInternalAuthenticationProvider();
        final List<IAuthenticationProvider> providers = Lists.newArrayList(surrogateProvider);
        // create and add external authentication provider.
        try {
            for (final IAuthenticationProperties auth : this.securityConfiguration.authentications()) {
                final Optional<IAuthenticationProvider> provider = createAuthenticationProvider(auth,
                    surrogateProvider);
                if (provider.isPresent()) {
                    providers.add(provider.get());
                }
            }
        } catch (final Throwable ex) {
            // working in internal miminal configuration
            LOGGER.error("error configuration external authentication provider", ex);
        }
        this.authenticationProviders = Collections.unmodifiableList(providers);
    }

    /**
     * @param securityConfiguration
     */
    public void setSecurityConfiguration(final SecurityProperties securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExternalProvider() {
        return from(getAuthenticationProviders())
                .anyMatch(Predicates.not(@Nullable IAuthenticationProvider::isInternal));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Started Authentication provider Service");
        }
        try {
            configure();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return;
        }

        this.started = true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        this.authenticationProviders = Collections.emptyList();
        this.started = false;
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Shut down Authentication provider Service");
        }
    }

    /**
     * @param event
     *            a event indicate the {@link SecurityProperties} has changed.
     * @throws Exception
     *             if start failed.
     */
    @EventListener
    public void onSecurityConfigurationChangedEvent(@Nonnull final ConfigurationChangedEvent<SecurityProperties> event)
            throws Exception {
        if (event == null || !event.isAssignable(SecurityProperties.class)) {
            return;
        }
        this.securityConfiguration = event.getNewConfiguration();
        try {
            shutdown();
        } finally {
            start();
        }

    }

    @Override
    public void checkConnection(final IAuthenticationProperties config) {
        final IAuthenticationProvider provider = createAuthenticationProvider(config,
            getInternalAuthenticationProvider().orElse(null))
                    .orElseThrow(() -> new NoSecurityConfigurationException(
                            i18nService.createKeyedMessage("app.security.configuration.empty")));
        provider.checkConnection();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("null")
    @Override
    public @Nonnull List<IAuthenticationProvider> getAuthenticationProviders() {
        return authenticationProviders;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        if (!isStarted()) {
            throw new BadCredentialsException("authentication service didn't start.");
        }
        IAuthenticationProvider provider = null;
        try {
            // if not external provide exist and no started, use only internal provider.
            provider = hasExternalProvider() ? getProvider(authentication).orElse(null)
                    : getInternalAuthenticationProvider().orElse(null);
        } catch (final UsernameNotFoundException ex) {
            // the user doesn't exist internally but can exist somewhere else.
            // try authentication using external user directory.
            if (hasExternalProvider()) {
                provider = from(getAuthenticationProviders())
                        .firstOrDefault(Predicates.not(@Nullable IAuthenticationProvider::isInternal));
                if (provider == null) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }

        final Authentication auth = provider.authenticate(authentication);

        // skip synchronization if internal user
        if (provider.isInternal()) {
            return auth;
        }

        try {
            // synchronize only external directory active and for authorization
            if (provider.getDirectory().isActive() && !provider.getSupportedDirectory().isAuthenticationOnly()) {
                this.authenticationSynchroniserProvider.get()
                        .synchronise(auth.getName(), toString(auth.getAuthorities()), provider);
            }
        } catch (final Throwable ex) {
            LOGGER.error(ex.getMessage(), ex);
            // the user exists and authenticated but have a problem to retrieve user
            // information
            if (!auth.isAuthenticated()) {
                throw new BadCredentialsException("Bad credentials");
            }
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }

        final IAuthenticationProvider surrogateProvider = getInternalAuthenticationProvider().orElse(null);
        if (surrogateProvider != null) {
            return surrogateProvider.authenticate(auth);
        }
        return auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(final String username) {
        if (!this.isStarted()) {
            throw new UsernameNotFoundException("Authentication service didn't start.");
        }
        IAuthenticationProvider provider = getAuthenticationProvider(username).orElse(null);

        if (provider == null) {
            throw new InternalAuthenticationServiceException("Authentication Provider doesn't exist");
        }
        UserDetails userDetails = null;
        try {
            userDetails = provider.loadUserByUsername(username);
            if (!provider.getSupportedDirectory().isAuthenticationOnly()) {
                this.authenticationSynchroniserProvider.get()
                        .synchronise(userDetails.getUsername(), toString(userDetails.getAuthorities()), provider);
                userDetails = getInternalAuthenticationProvider().orElseThrow()
                        .loadUserByUsername(userDetails.getUsername());
            }
        } catch (final Throwable ex) {
            LOGGER.warn("Loading user detail from " + provider.getDirectory().getName() + " has failed", ex);
            // try with internal
            provider = this.getInternalAuthenticationProvider().orElseThrow();
            userDetails = provider.loadUserByUsername(username);
        }
        return userDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(final Class<?> authentication) {
        try {
            return Iterables.any(this.authenticationProviders, input -> input.supports(authentication));
        } catch (final Exception e) {
            return this.getInternalAuthenticationProvider().map(auth -> auth.supports(authentication)).orElse(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull Optional<IAuthenticationProvider> getAuthenticationProvider(final String username) {
        final IUser user = userRepository.findByName(username);
        if (user == null) {
            throw new UsernameNotFoundException("user name " + username + " doesn't exist");
        }
        return getAuthenticationProvider(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Optional<IAuthenticationProvider> getInternalAuthenticationProvider() {
        return getAuthenticationProvider(UserDirectory.Internal);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Optional<IAuthenticationProvider> getAuthenticationProvider(@Nonnull final IUser user) {
        checkNotNull(user, "user");
        final UserDirectory directory = user.getDirectory() == null ? UserDirectory.defaultDirectory()
                : user.getDirectory();

        return getAuthenticationProvider(directory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Optional<IAuthenticationProvider> getAuthenticationProvider(final UserDirectory directory) {
        return this.authenticationProviders.stream()
                .filter(input -> directory.equals(input.getSupportedDirectory()))
                .findFirst();
    }

    @VisibleForTesting
    IAuthenticationProvider createInternalAuthenticationProvider() {
        return new UserAuthenticationProvider(userRepository, groupRepository, permissionServiceProvider.get(),
                passwordEncoder);
    }

    /**
     * @param authentication
     *            the authentification to use.
     * @return Returns the first appropriate {@link IAuthenticationProvider} for the type of {@link Authentication
     *         authentication}.
     * @see Authentication
     */
    protected Optional<IAuthenticationProvider> getProvider(final @Nonnull Authentication authentication) {
        if (authentication instanceof UserAuthenticationToken) {
            final UserDirectory directory = ((UserAuthenticationToken) authentication).getPrincipal().getDirectory();
            return this.authenticationProviders.stream()
                    .filter(input -> directory.equals(input.getSupportedDirectory()))
                    .findFirst();
        } else {
            final String username = authentication.getName();
            return getAuthenticationProvider(username);
        }
    }

    @VisibleForTesting
    Optional<IAuthenticationProvider> createAuthenticationProvider(
        final @Nonnull IAuthenticationProperties configuration,
        final IAuthenticationProvider surrogateProvider) {
        Assert.checkNotNull(configuration, "configuration");
        final AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        final Optional<IAuthenticationProvider> provider = IAuthenticationProvider.create(configuration);
        if (provider.isPresent()) {
            final IAuthenticationProvider p = provider.get();
            if (p instanceof IDelegateAuthenticationProviderAware) {
                ((IDelegateAuthenticationProviderAware) p).setDelegate(surrogateProvider);
            }

            factory.autowireBean(p);
            factory.initializeBean(p, p.getClass().getName());
        }
        return provider;
    }

    @VisibleForTesting
    static Set<String> toString(final @Nonnull Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(DefaultAuthentificationProviderService::authorityToString)
                .collect(Collectors.toSet());
    }

    private static String authorityToString(final @Nonnull GrantedAuthority authority) {
        return authority.toString();
    }

}
