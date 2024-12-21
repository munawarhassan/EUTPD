package com.pmi.tpd.core.security;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;

import com.google.common.collect.FluentIterable;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.exception.ExpiredPasswordAuthenticationException;
import com.pmi.tpd.core.exception.InactiveUserAuthenticationException;
import com.pmi.tpd.core.exception.IncorrectPasswordAuthenticationException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.security.provider.IAuthenticationProvider;
import com.pmi.tpd.core.security.provider.IAuthenticationProviderService;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.IGroup;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.security.AuthenticationException;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 1.0
 */
public class DefaultAuthenticationService implements IAuthenticationService {

    /** */
    private final IAuthenticationProviderService delegate;

    /** */
    private final IUserRepository userRepository;

    /** */
    private final I18nService i18nService;

    /** */
    private final CachingUserDetailsService cache;

    /** **/
    private final AuthenticationEventPublisher eventPublisher;

    /**
     * @param applicationEventPublisher
     *            spring application event publisher.
     * @param userRepository
     *            the user repository (can <b>not</b> {@code null}).
     * @param delegate
     *            provider strategy on type of directory (can <b>not</b> {@code null}).
     * @param i18nService
     *            the localisation service (can <b>not</b> {@code null}).
     * @param userCache
     *            the user cache for {@link UserDetails} (can <b>not</b> {@code null}).
     */
    @Inject
    public DefaultAuthenticationService(@Nonnull final ApplicationEventPublisher applicationEventPublisher,
            @Nonnull final IUserRepository userRepository, @Nonnull final IAuthenticationProviderService delegate,
            @Nonnull final I18nService i18nService, @Nonnull final UserCache userCache) {
        this.eventPublisher = new DefaultAuthenticationEventPublisher(
                checkNotNull(applicationEventPublisher, "applicationEventPublisher"));
        this.delegate = checkNotNull(delegate, "delegate");
        this.userRepository = checkNotNull(userRepository, "userRepository");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.cache = new CachingUserDetailsService(this.delegate);
        this.cache.setUserCache(checkNotNull(userCache, "userCache"));

    }

    /** {@inheritDoc} */
    @Override
    public Authentication authenticate(@Nonnull final Authentication authentication) throws AuthenticationException {
        org.springframework.security.core.AuthenticationException lastException = null;
        Authentication result = null;
        try {
            result = delegate.authenticate(authentication);
        } catch (final org.springframework.security.core.AuthenticationException e) {
            lastException = e;
        }
        if (result != null) {
            eventPublisher.publishAuthenticationSuccess(result);
            return result;
        }

        prepareException(lastException, authentication);

        throw lastException;
    }

    /** {@inheritDoc} */
    @Override
    public boolean supports(final Class<?> authentication) {
        return delegate.supports(authentication);
    }

    /** {@inheritDoc} */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return cache.loadUserByUsername(username);
    }

    /**
     * Gets the authentication provider used.
     *
     * @return Returns a {@link com.pmi.tpd.core.security.provider.IAuthenticationProviderService} delagate provider.
     */
    public IAuthenticationProviderService getDelegate() {
        return delegate;
    }

    /**
     * Allows verify whether the security context is initialised.
     */
    protected void checkContext() {
        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new AuthenticationServiceException("Context spring is not initialize");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IDirectory> listDirectories() {
        final List<IAuthenticationProvider> providers = this.delegate.getAuthenticationProviders();
        return FluentIterable.from(providers)
                .transform(@org.checkerframework.checker.nullness.qual.Nullable IAuthenticationProvider::getDirectory)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDirectory findDirectoryFor(@Nonnull final IUser user) {
        checkNotNull(user, "user");
        final IAuthenticationProvider provider = this.delegate.getAuthenticationProvider(user).orElse(null);
        if (provider == null) {
            return null;
        }
        return provider.getDirectory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IDirectory findDirectoryFor(final UserDirectory directory) {
        return this.delegate.getAuthenticationProvider(directory)
                .map(IAuthenticationProvider::getDirectory)
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canResetPassword(@Nonnull final String username) {
        final IAuthenticationProvider provider = delegate.getAuthenticationProvider(username).orElseThrow();
        return provider.getDirectory().isUserUpdatable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<IUser> findUsers(final @Nonnull Pageable pageRequest) {
        return PageUtils.asPageOf(IUser.class, userRepository.findUsers(pageRequest));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUser findUser(final @Nonnull String username, final boolean inactive) {
        return this.userRepository
                .findOne(QUserEntity.userEntity.username.equalsIgnoreCase(username)
                        .and(QUserEntity.userEntity.activated.eq(!inactive)))
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public IGroup findGroup(@Nonnull final UserDirectory directory, final String groupName) {
        return this.getDelegate()
                .getAuthenticationProvider(checkNotNull(directory, "directory"))
                .map(p -> p.findGroupByName(groupName))
                .orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull Page<String> findGroups(@Nonnull final UserDirectory directory,
        @Nonnull final String groupName,
        @Nonnull final Pageable pageRequest) {
        return this.getDelegate()
                .getAuthenticationProvider(directory)
                .map(p -> p.findGroupsByName(groupName, pageRequest))
                .orElse(Page.empty());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsUser(final String login) {
        final IAuthenticationProvider provider = this.getDelegate().getAuthenticationProvider(login).orElseThrow();
        return provider.findUserByName(login) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUser authenticate(final String name, final String currentPassword)
            throws IncorrectPasswordAuthenticationException {
        try {
            final Authentication authentication = delegate
                    .authenticate(new UsernamePasswordAuthenticationToken(name, currentPassword));
            if (authentication instanceof UserAuthenticationToken) {
                return ((UserAuthenticationToken) authentication).getPrincipal();
            } else {
                throw new RuntimeException(
                        "this '" + authentication.getClass().getName() + "'type of authentification is not suppoted");
            }
        } catch (final UsernameNotFoundException e) {
            throw unknownUser(name);
        } catch (final AccountExpiredException e) {
            throw new ExpiredPasswordAuthenticationException(expiredCredentials());
        } catch (final DisabledException e) {
            throw new InactiveUserAuthenticationException(inactiveAccount());
        } catch (final BadCredentialsException ex) {
            throw new IncorrectPasswordAuthenticationException(authenticationFailed());
        }
    }

    private KeyedMessage expiredCredentials() {
        return i18nService.createKeyedMessage("app.service.user.expiredcredentials");
    }

    private KeyedMessage inactiveAccount() {
        return i18nService.createKeyedMessage("app.service.user.inactive");
    }

    private NoSuchUserException unknownUser(final String username) {
        throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.unknown", username), username);
    }

    private KeyedMessage authenticationFailed() {
        return i18nService.createKeyedMessage("app.service.user.authenticationfailed");
    }

    private void prepareException(final org.springframework.security.core.AuthenticationException ex,
        final Authentication auth) {
        eventPublisher.publishAuthenticationFailure(ex, auth);
    }

    /**
     * @author devacfr<christophefriederich@mac.com>
     */
    public static class CachingUserDetailsService implements UserDetailsService {

        /** */
        private UserCache userCache = new NullUserCache();

        /** */
        private final UserDetailsService delegate;

        CachingUserDetailsService(final UserDetailsService delegate) {
            this.delegate = delegate;
        }

        /**
         * @return Returns the {@link UserCache} used to cache created {@link UserDetails}.
         * @see UserDetails
         */
        public UserCache getUserCache() {
            return userCache;
        }

        /**
         * @param userCache
         *            set user cache allowing to store {@link UserDetails}.
         */
        public void setUserCache(final UserCache userCache) {
            this.userCache = userCache;
        }

        /**
         * Removes the specified user from the cache.
         *
         * @param username
         *            the username.
         */
        public void evict(final String username) {
            this.userCache.removeUserFromCache(username);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public UserDetails loadUserByUsername(final String username) {
            UserDetails user = userCache.getUserFromCache(username);

            if (user == null) {
                user = delegate.loadUserByUsername(username);
            }

            checkNotNull(user,
                "UserDetailsService " + delegate + " returned null for username " + username + ". "
                        + "This is an interface contract violation");

            userCache.putUserInCache(user);

            return user;
        }
    }

}
