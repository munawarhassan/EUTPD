package com.pmi.tpd.core.security.provider;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.security.configuration.ActiveDirectoryAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.IAuthenticationProperties;
import com.pmi.tpd.core.security.configuration.LdapAuthenticationProperties;
import com.pmi.tpd.core.security.provider.ldap.ActiveDirectoryUserAuthenticationProvider;
import com.pmi.tpd.core.security.provider.ldap.InternalActiveDirectoryUserAuthenticationProvider;
import com.pmi.tpd.core.security.provider.ldap.InternalLdapUserAuthenticationProvider;
import com.pmi.tpd.core.security.provider.ldap.LdapUser;
import com.pmi.tpd.core.security.provider.ldap.LdapUserAuthenticationProvider;
import com.pmi.tpd.core.user.IGroup;

/**
 * <p>
 * IAuthenticationProvider interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IAuthenticationProvider extends AuthenticationProvider, UserDetailsService {

    /**
     * Create a specific {@link IAuthenticationProvider} according to type of {@code AuthenticationConfiguration}.
     *
     * @param config
     *            a authentication configuration.
     * @return Returns new instance of {@link IAuthenticationProvider}.
     */
    static Optional<IAuthenticationProvider> create(@Nonnull final IAuthenticationProperties config) {
        IAuthenticationProvider provider = null;
        final Class<?> cl = config.getClass();
        if (cl.equals(ActiveDirectoryAuthenticationProperties.class)) {
            if (((ActiveDirectoryAuthenticationProperties) config).isAuthenticationOnly()) {
                provider = new InternalActiveDirectoryUserAuthenticationProvider(
                        (ActiveDirectoryAuthenticationProperties) config);
            } else {
                provider = new ActiveDirectoryUserAuthenticationProvider(
                        (ActiveDirectoryAuthenticationProperties) config);
            }
        } else if (cl.equals(LdapAuthenticationProperties.class)) {
            if (((LdapAuthenticationProperties) config).isAuthenticationOnly()) {
                provider = new InternalLdapUserAuthenticationProvider<>((LdapAuthenticationProperties) config);
            } else {
                provider = new LdapUserAuthenticationProvider<>((LdapAuthenticationProperties) config);
            }
        }
        return Optional.ofNullable(provider);
    }

    /**
     * @return Returns {@code true} if the provider is use for internal authentification otherwise {@code false}.
     */
    boolean isInternal();

    /**
     * @return Returns the type of directory supported.
     */
    UserDirectory getSupportedDirectory();

    /**
     * <p>
     * getPasswordEncoder.
     * </p>
     *
     * @return a {@link PasswordEncoder} object.
     */
    @Nonnull
    PasswordEncoder getPasswordEncoder();

    /**
     * Locates the user based on the username. In the actual implementation, the search may possibly be case sensitive,
     * or case insensitive depending on how the implementation instance is configured. In this case, the
     * <code>UserDetails</code> object that comes back may have a username that is of a different case than what was
     * actually requested..
     *
     * @param username
     *            the username identifying the user whose data is required.
     * @return a fully populated user record (never <code>null</code>)
     * @throws UsernameNotFoundException
     *             if the user could not be found or the user has no GrantedAuthority
     */
    @Override
    UserDetails loadUserByUsername(String username);

    /**
     * @return Returns a {@link IDirectory} representing the type of user directory.
     */
    IDirectory getDirectory();

    /**
     * Retrieves a page of group names filtered by name.
     *
     * @param groupName
     *            0 or more characters to apply as a "containing" filter on returned groups
     * @param pageRequest
     *            defines the page of groups to retrieve
     * @return a page of group names, optionally filtered by the provided {@code groupName}, which may be empty but
     *         never {@code null}.
     */
    @Nonnull
    Page<String> findGroupsByName(@Nullable String groupName, @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of {@link LdapUser} users filtered by name.
     *
     * @param username
     *            0 or more characters to apply as a filter on returned users.
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return a page of users, optionally filtered by the provided {@code username}, which may be empty but never
     *         {@code null}.
     */
    @Nonnull
    Page<IUser> findUsersByName(@Nullable String username, @Nonnull Pageable pageRequest);

    /**
     * Retrieves a group by their name.
     *
     * @param groupName
     *            the name to match (can be {@code null}).
     * @return Returns the group or {@code null} if no group can be found with that name.
     */
    @Nullable
    IGroup findGroupByName(String groupName);

    /**
     * Retrieves a user by their username.
     *
     * @param username
     *            the username to match (can be {@code null}).
     * @return Returns the user or {@code null} if no user can be found with that username.
     */
    @Nullable
    IUser findUserByName(String username);

    /**
     * Check the connection.
     */
    void checkConnection();

}
