package com.pmi.tpd.core.security;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.core.exception.ExpiredPasswordAuthenticationException;
import com.pmi.tpd.core.exception.InactiveUserAuthenticationException;
import com.pmi.tpd.core.exception.IncorrectPasswordAuthenticationException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.security.provider.DefaultDirectory;
import com.pmi.tpd.core.security.provider.IAuthenticationProvider;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.IGroup;

/**
 * Provide a interface between {@link DefaultDirectory} and {@link IAuthenticationProvider}.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IAuthenticationService extends UserDetailsService, AuthenticationProvider {

    /**
     * Lists all of the directories which have been configured.
     * <p>
     * Note: The system creates a single internal {@code Directory} when it is first started. Administrators may
     * configure additional directories but there will always be at least one <i>active</i> {@code Directory} in the
     * returned list; it is not possible to disable all directories.
     * </p>
     *
     * @return a list containing at least one active directory
     */
    @Nonnull
    List<IDirectory> listDirectories();

    /**
     * Retrieves the {@code Directory} in which the provided {@code IUser} exists.
     *
     * @param user
     *            the user to retrieve the directory for
     * @return the directory in which the specified {@code user} exists
     */
    @Nullable
    IDirectory findDirectoryFor(@Nonnull IUser user);

    /**
     * Retrieves the {@code Directory} in which the provided {@code IUser} exists.
     *
     * @param directory
     *            the type of directory to retrieve the directory for
     * @return the directory in which the specified {@code user} exists
     */
    @Nullable
    IDirectory findDirectoryFor(UserDirectory directory);

    /**
     * Checks whether the user's password can be locally reset.
     * <p>
     * Only users from internal directories (excluding internal directories with delegated LDAP authentication) can
     * reset their passwords through application. Users from remote directories (LDAP, Active Directory, etc.) need to
     * update their passwords using their providers' user interface or API.
     * </p>
     *
     * @param username
     *            the username of the user
     * @return whether the user can reset his/her password in application
     */
    boolean canResetPassword(@Nonnull String username);

    /**
     * Retrieves a page of {@link IUser#isActivated() active} users.
     *
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return Returns a page of users, which may be empty but never {@code null}
     */
    @Nonnull
    Page<IUser> findUsers(@Nonnull Pageable pageRequest);

    /**
     * Attempts to retrieve the user with the specified {@code username}.
     *
     * @param username
     *            the <i>exact</i> name of the user to retrieve
     * @param inactive
     *            {@code false} if only {@link User#isActive() active} users should be considered, {@code true} to
     *            consider all users
     * @return the named user, or {@code null} if no such user exists
     */
    @Nullable
    IUser findUser(@Nonnull String username, boolean inactive);

    /**
     * Retrieves a group by their {@link GroupEntity#getName()}.
     *
     * @param directory
     *            the user directory to use (can <b>not</b> be {@code null}).
     * @param groupName
     *            the name to match (can be {@code null}).
     * @return Returns the group or {@code null} if no group can be found with that name.
     */
    @Nullable
    IGroup findGroup(@Nonnull UserDirectory directory, @Nonnull String groupName);

    /**
     * Retrieves a page of group names.
     *
     * @param directory
     *            the user directory to use (can <b>not</b> be {@code null}).
     * @param groupName
     *            the name to match (can be {@code null}).
     * @param pageRequest
     *            defines the page of users to retrieve (can <b>not</b> be {@code null}).
     * @return Returns a page of group names, which may be empty but never {@code null}
     */
    @Nonnull
    Page<String> findGroups(@Nonnull UserDirectory directory, @Nonnull String groupName, @Nonnull Pageable pageRequest);

    /**
     * Gets the indicating whether the user with {@code username} exists.
     *
     * @param username
     *            the user name to check (can <b>not</b> be {@code null}).
     * @return Returns {@code true} whether the user with {@code username} exists, {@code false} otherwise.
     */
    boolean existsUser(String username);

    /**
     * Attempts to authenticate the specified user given their password.
     *
     * @param username
     *            the username
     * @param currentPassword
     *            the user's password
     * @return the {@link IUser} representing the authenticated user
     * @throws ExpiredPasswordAuthenticationException
     *             if the user's password has expired and must be changed
     * @throws InactiveUserAuthenticationException
     *             if the specified user is inactive
     * @throws IncorrectPasswordAuthenticationException
     *             if the provided password is incorrect
     * @throws NoSuchUserException
     *             if the specified user does not exist or their user details cannot be retrieved
     */
    IUser authenticate(String username, String currentPassword) throws IncorrectPasswordAuthenticationException;

}