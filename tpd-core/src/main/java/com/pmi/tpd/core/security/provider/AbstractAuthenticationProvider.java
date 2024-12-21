package com.pmi.tpd.core.security.provider;

import java.util.List;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.security.spring.UserAuthenticationToken;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public abstract class AbstractAuthenticationProvider implements IAuthenticationProvider {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationProvider.class);

    /** */
    @Nonnull
    private PasswordEncoder passwordEncoder;

    /**
     * <p>
     * Constructor for AbstractAuthenticationProvider.
     * </p>
     *
     * @param passwordEncoder
     *            a {@link PasswordEncoder} used to encode and validate passwords.
     */
    @SuppressWarnings("null")
    public AbstractAuthenticationProvider(final @Nonnull PasswordEncoder passwordEncoder) {

        setPasswordEncoder(passwordEncoder);
    }

    /**
     * Creates a successful {@link Authentication} object.
     * <p>
     * Protected so subclasses can override.
     * </p>
     * <p>
     * Subclasses will usually store the original credentials the user supplied (not salted or encoded passwords) in the
     * returned <code>Authentication</code> object.
     * </p>
     *
     * @param authentication
     *            that was presented to the provider for validation
     * @param user
     *            that was loaded by the implementation
     * @param userDetails
     *            the user details associated to user.
     * @return the successful authentication token
     */
    protected Authentication createSuccessAuthentication(final Authentication authentication,
        final IUser user,
        final UserDetails userDetails) {
        Authentication result = null;
        if (authentication instanceof UserAuthenticationToken) {
            result = authentication;
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            result = UserAuthenticationToken.forUser(user, userDetails);
        }
        return result;
    }

    /**
     * @param username
     *            the username of user to retrieve.
     * @param authentication
     *            the authentication to use.
     * @return Returns the the user associated to authentication.
     * @throws AuthenticationException
     *             if can not retrieve user from repository.
     */
    protected final IUser retrieveUser(final @Nonnull String username,
        final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        IUser loadedUser;

        try {
            loadedUser = loadUser(username);
        } catch (final UsernameNotFoundException notFound) {
            throw notFound;
        } catch (final Exception repositoryProblem) {
            throw new InternalAuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        return loadedUser;
    }

    /**
     * @param passwordEncoder
     *            the password encoder to use.
     */
    protected void setPasswordEncoder(final @Nonnull PasswordEncoder passwordEncoder) {
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.passwordEncoder = passwordEncoder;
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public @Nonnull PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    /**
     * @param username
     *            username to get.
     * @return Return a {@link IUser}.
     * @throws UsernameNotFoundException
     *             if username doesn't exist.
     * @throws DataAccessException
     *             if data access failed.
     */
    @Nonnull
    protected abstract IUser loadUser(@Nonnull String username) throws UsernameNotFoundException, DataAccessException;

    /**
     * @param user
     *            the user to use.
     * @return Returns the list of {@link GrantedAuthority} associated to user.
     */
    @Nonnull
    protected abstract List<GrantedAuthority> getGrantedAuthorities(@Nonnull IUser user);

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final IUser user = loadUser(Assert.checkNotNull(username, "username"));
        // Populating attributes
        final String password = user.getPassword();
        final boolean enabled = user.isActivated();
        final boolean accountNonExpired = true;
        final boolean credentialsNonExpired = true;
        final boolean accountNonLocked = true;

        // Returning new user details
        return new org.springframework.security.core.userdetails.User(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, getGrantedAuthorities(user));
    }

    /**
     * @param user
     *            the user to check
     * @param authentication
     *            the authentication to check.
     * @throws AuthenticationException
     *             if check failed.
     */
    protected void additionalAuthenticationChecks(final UserDetails user,
        final UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        // skip authentication
        if (authentication instanceof UserAuthenticationToken) {
            return;
        }
        // already authenticated
        if (authentication.isAuthenticated()) {
            return;
        }

        if (authentication.getCredentials() == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Authentication failed: no credentials provided");
            }

            throw new BadCredentialsException("Bad credentials");
        }

        final String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, user.getPassword())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Authentication failed: password does not match stored value");
            }

            throw new BadCredentialsException("Bad credentials");
        }
    }

}
