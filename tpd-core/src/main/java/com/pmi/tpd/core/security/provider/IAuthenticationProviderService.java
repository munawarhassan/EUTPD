package com.pmi.tpd.core.security.provider;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public interface IAuthenticationProviderService extends AuthenticationProvider, UserDetailsService {

    /**
     * @return Returns {@code true} whether a provider service correctly started.
     */
    boolean isStarted();

    /**
     * @return Returns {@code true} whether a external provider exists and correctly started.
     */
    boolean hasExternalProvider();

    /**
     * @return Returns the list of all managed {@link IAuthenticationProvider}.
     */
    @Nonnull
    List<IAuthenticationProvider> getAuthenticationProviders();

    /**
     * @param directory
     *            the type of user directory used.
     * @return Returns the {@link IAuthenticationProvider} associated to user directory type.
     */
    @Nonnull
    Optional<IAuthenticationProvider> getAuthenticationProvider(UserDirectory directory);

    /**
     * @return Returns the {@link IAuthenticationProvider} use for internal authentication.
     */
    @Nonnull
    Optional<IAuthenticationProvider> getInternalAuthenticationProvider();

    /**
     * @param user
     *            user used.
     * @return Returns the {@link IAuthenticationProvider} associated to user.
     */
    @Nonnull
    Optional<IAuthenticationProvider> getAuthenticationProvider(@Nonnull IUser user);

    /**
     * @param username
     *            the username used.
     * @return Returns the {@link IAuthenticationProvider} associated to username.
     */
    @Nonnull
    Optional<IAuthenticationProvider> getAuthenticationProvider(String username);

}
