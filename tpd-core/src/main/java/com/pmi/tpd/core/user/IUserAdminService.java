package com.pmi.tpd.core.user;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.core.exception.InvalidTokenException;
import com.pmi.tpd.core.exception.NoSuchGroupException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.exception.UsernameAlreadyExistsException;
import com.pmi.tpd.security.ForbiddenException;

/**
 * Provides methods for querying, <i>creating and updating</i> users and groups.
 * <p>
 * Many of the queries provided by this service are also provided by {@link IUserService}. The queries provided here are
 * specifically geared toward <i>administration</i>, retrieving {@link DetailedGroup detailed groups} and
 * {@link DetailedUser detailed users}. For non-administrative usages, like group and user pickers, the "normal" queries
 * from {@link UserService} should be used instead.
 * </p>
 *
 * @see IUserService
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IUserAdminService {

    /**
     * Adds a user to one or more groups.
     *
     * @param username
     *            name of the user who will receive the new group memberships
     * @param groupNames
     *            names of the groups to add to the user
     * @throws ForbiddenException
     *             if one of the groups would grant {@link Permission#SYS_ADMIN SYS_ADMIN} permission and the current
     *             user isn't a SYS_ADMIN; only SYS_ADMINs can grant SYS_ADMIN permission
     * @throws NoSuchGroupException
     *             if any of the specified groups does not exist
     * @throws NoSuchUserException
     *             if the specified user does not exist
     */
    void addUserToGroups(@Nonnull String username, @Nonnull Set<String> groupNames)
            throws ForbiddenException, NoSuchGroupException, NoSuchUserException;

    /**
     * Adds one or more users to a group.
     *
     * @param groupName
     *            names of the group to which the users will be added
     * @param usernames
     *            names of the users who will receive the new group membership
     * @throws ForbiddenException
     *             if the group would grant {@link Permission#SYS_ADMIN SYS_ADMIN} permission and the current user isn't
     *             a SYS_ADMIN; only SYS_ADMINs can grant SYS_ADMIN permission
     * @throws NoSuchGroupException
     *             if the specified group does not exist
     * @throws NoSuchUserException
     *             if any of the specified users does not exist
     */
    void addMembersToGroup(@Nonnull String groupName, @Nonnull Set<String> usernames)
            throws ForbiddenException, NoSuchGroupException, NoSuchUserException;

    /**
     * Retrieves a flag indicating whether it is possible to create a group.
     *
     * @return {@code true} if any directory exists in which a group can be created; otherwise, {@code false}
     */
    boolean canCreateGroups();

    /**
     * Retrieves a flag indicating whether it is possible to update groups.
     *
     * @return {@code true} if any directory exists in which a group can be updated; otherwise, {@code false}
     */
    boolean canUpdateGroups();

    /**
     * Retrieves a flag indicating whether it is possible to create a user.
     *
     * @return {@code true} if any directory exists in which a user can be created; otherwise, {@code false}
     */
    boolean canCreateUsers();

    /**
     * Retrieves a flag indicating whether it is possible to delete a group.
     *
     * @return {@code true} if any directory exists in which a group can be deleted; otherwise {@code false}
     */
    boolean canDeleteGroups();

    /**
     * Retrieves a flag indicating whether the new user is able to set a password or not.
     *
     * @return {code true} if user is able to set a password; otherwise {@code false}
     */
    boolean newUserCanResetPassword();

    /**
     * Creates a new group.
     *
     * @param groupName
     *            the name for the new group
     * @return Returns a {@link GroupRequest} representing the created group.
     * @throws IntegrityException
     *             if a group with the same already exists
     */
    @Nonnull
    GroupRequest createGroup(@Nonnull String groupName) throws IntegrityException;

    /**
     * Creates a new group.
     *
     * @param groupName
     *            the name for the new group
     * @param directory
     *            user directory to use.
     * @return Returns a {@link GroupRequest} representing the created group.
     * @throws IntegrityException
     *             if a group with the same already exists
     */
    GroupRequest createGroup(@Nonnull String groupName, @Nonnull UserDirectory directory) throws IntegrityException;

    /**
     * Add existing external group from user directory.
     *
     * @param directory
     *            type of directory to use (can <b>not</b> be {@code null}).
     * @param groups
     *            the list of name for the external group (can <b>not</b> be {@code null}).
     */
    void addGroups(@Nonnull UserDirectory directory, @Nonnull Set<String> groups);

    /**
     * Creates a new user and adds them to the default group, if it exists. If the user should be created without being
     * added to the default group, use {@link #createUser(String, String, String, String, String, boolean)} instead.
     * <p>
     * A non-{@code null}, non-blank {@code password} must be provided. It may be further vetted by the user store, for
     * example by applying complexity restrictions. Alternatively, if an e-mail server has been configured, new users
     * can be created {@link #createUserWithGeneratedPassword(String, String, String, String) with generated passwords},
     * allowing new users to set their own password when they first access the system.
     * </p>
     *
     * @return Returns a {@link UserRequest} instance representing the created user.
     * @param username
     *            the {@link IUser#getName() user name} for the new user
     * @param password
     *            the user's initial password, which may not be {@code null} or empty
     * @param displayName
     *            the {@link IUser#getDisplayName() display name} for the new user
     * @param lastName
     *            the {@link IUser#getLastName() last name} for the new user
     * @param emailAddress
     *            the {@link IUser#getEmail() e-mail address} for the new user
     * @throws IntegrityException
     *             if a user with the same user name already exists
     * @throws UserEmailAlreadyExistsException
     *             if a user has same email address.
     * @throws UsernameAlreadyExistsException
     *             if the user already exists.
     * @see #createUser(String, String, String, String, boolean)
     */
    UserRequest createUser(@Nonnull String username,
        @Nonnull String password,
        @Nonnull String displayName,
        @Nonnull String emailAddress)
            throws IntegrityException, UsernameAlreadyExistsException, UserEmailAlreadyExistsException;

    /**
     * Creates a new user and optionally adds them to the default group, if it exists.
     * <p>
     * A non-{@code null}, non-blank {@code password} must be provided. It may be further vetted by the user store, for
     * example by applying complexity restrictions. Alternatively, if an e-mail server has been configured, new users
     * can be created {@link #createUserWithGeneratedPassword(String, String, String, String) with generated passwords},
     * allowing new users to set their own password when they first access the system.
     * </p>
     * <p>
     * This method is not intended to be exposed via services like REST for general consumption. It exists to satisfy
     * specific use cases where the system may wish to create a user <i>without</i> adding them to the default group.
     * Generally it is expected that new users should be added to the default group, as it defines the set of "common"
     * permissions for all users of the system.
     * </p>
     *
     * @param username
     *            the {@link IUser#getName() user name} for the new user
     * @param password
     *            the user's initial password, which may not be {@code null} or empty
     * @param displayName
     *            the {@link IUser#getDisplayName() display name} for the new user
     * @param lastName
     *            the {@link IUser#getLastName() last name} for the new user
     * @param emailAddress
     *            the {@link IUser#getEmail() e-mail address} for the new user
     * @param directory
     *            the {@link IUser#getDirectory()} for the new user.
     * @param addToDefaultGroup
     *            indicate if add user to the default group
     * @return Return new instance of {@link UserRequest} representing the new created user.
     * @throws IntegrityException
     *             if a user with the same user name already exists
     * @throws UserEmailAlreadyExistsException
     *             if a user has same email address.
     * @throws UsernameAlreadyExistsException
     *             if the user already exists.
     * @see #createUser(String, String, String, String)
     */
    UserRequest createUser(@Nonnull String username,
        @Nonnull String password,
        @Nonnull String displayName,
        @Nonnull String emailAddress,
        @Nonnull UserDirectory directory,
        boolean addToDefaultGroup)
            throws IntegrityException, UsernameAlreadyExistsException, UserEmailAlreadyExistsException;

    /**
     * Creates a new user with a randomly-generated password. An e-mail notification will be sent to the new user's
     * e-mail address with instructions on how to reset their password and finish activating their account.
     *
     * @param username
     *            the {@link IUser#getName() user name} for the new user
     * @param displayName
     *            the {@link IUser#getDisplayName() display name} for the new user
     * @param lastName
     *            the {@link IUser#getLastName() last name} for the new user
     * @param emailAddress
     *            the {@link IUser#getEmail() e-mail address} for the new user
     * @param directory
     *            the {@link IUser#getDirectory()} for the new user.
     * @return Return new instance of {@link UserRequest} representing the new created user.
     * @throws IntegrityException
     *             if a user with the same user name already exists or if the user will be created in a directory that
     *             does not allow passwords to be reset
     * @throws MailException
     *             if the e-mail notification could not be sent to the created user to allow them to set their initial
     *             password
     * @throws UserEmailAlreadyExistsException
     *             if a user has same email address.
     * @throws UsernameAlreadyExistsException
     *             if the user already exists.
     * @throws NoMailHostConfigurationException
     *             if no e-mail server has been configured
     */
    UserRequest createUserWithGeneratedPassword(@Nonnull String username,
        @Nonnull String displayName,
        @Nonnull String emailAddress,
        @Nonnull UserDirectory directory)
            throws IntegrityException, MailException, UsernameAlreadyExistsException, UserEmailAlreadyExistsException;

    /**
     * Deletes a group. Deleting a group will also revoke all permissions which had been granted to that group.
     *
     * @param groupName
     *            the name of the group to delete
     * @return Returns a {@link GroupRequest} instance representing the deleted group.
     * @throws ForbiddenException
     *             if the group grants {@link Permission#SYS_ADMIN SYS_ADMIN} permission but the current user is not a
     *             SYS_ADMIN
     * @throws IntegrityException
     *             if deleting the group would revoke the {@link Permission#ADMIN ADMIN} or {@link Permission#SYS_ADMIN
     *             SYS_ADMIN} permissions of the current user
     * @throws NoSuchGroupException
     *             if the group does not exist
     */
    @Nonnull
    GroupRequest deleteGroup(@Nonnull String groupName)
            throws ForbiddenException, IntegrityException, NoSuchGroupException;

    /**
     * Deletes a user. Deleting a user will also revoke all permissions which had been granted to that user, as well as
     * invalidating any cached authentication tokens they may have.
     *
     * @param username
     *            the username of the user to delete
     * @return Returns a {@link UserRequest} instance representing the deleted user.
     * @throws ForbiddenException
     *             if the user to delete is a {@link Permission#SYS_ADMIN SYS_ADMIN} and the current user is not
     * @throws IntegrityException
     *             if the user to delete is the current user
     * @throws NoSuchUserException
     *             if the user does not exist
     */
    @Nonnull
    UserRequest deleteUser(@Nonnull String username) throws ForbiddenException, IntegrityException, NoSuchUserException;

    /**
     * Retrieves a page of groups with full {@link DetailedGroup details}.
     * <p>
     * Note: To filter the retrieved groups by name, use {@link #findGroupsByName(String, Pageable)} instead.
     * </p>
     *
     * @param pageRequest
     *            defines the page of groups to retrieve
     * @return the requested page of groups, which may be empty but never {@code null}
     * @see #findGroupsByName(String, PageRequest)
     * @see com.pmi.tpd.core.user.IUserService#findGroups(PageRequest)
     */
    @Nonnull
    Page<GroupRequest> findGroups(@Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of groups with full {@link GroupRequest details}, optionally filtering the returned results to
     * those containing the specified {@code groupName}. If the provided {@code groupName} is {@code null}, this method
     * behaves identically to {@link #findGroups(Pageable)}.
     *
     * @param pageRequest
     *            defines the page of groups to retrieve
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups.
     * @return the requested page of groups, potentially filtered, which may be empty but never {@code null}
     * @see #findGroups(PageRequest)
     * @see com.pmi.tpd.core.user.IUserService#findGroupsByName(String, PageRequest)
     */
    @Nonnull
    Page<GroupRequest> findGroupsByName(@Nullable String groupName, @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of groups with group name, optionally filtering the returned results to those containing the
     * specified {@code groupName}.
     *
     * @param pageRequest
     *            defines the page of groups to retrieve
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups.
     * @param directory
     *            the specific user directory to use.
     * @return the requested page of groups, potentially filtered, which may be empty but never {@code null}
     * @see #findGroups(PageRequest)
     * @see com.pmi.tpd.core.user.IUserService#findGroupsByName(String, PageRequest)
     */
    Page<String> findGroupsForDirectory(@Nonnull Pageable pageRequest,
        @Nonnull UserDirectory directory,
        @Nonnull String groupName);

    /**
     * Retrieves a page of groups which the specified user is a member of, with full {@link DetailedGroup details},
     * optionally filtering the returned results to those containing the specified {@code groupName}. If the provided
     * {@code groupName} is {@code null} or empty groups will not be filtered. If no user exists with the specified
     * {@code username}, no groups will be returned.
     *
     * @param username
     *            the <i>exact</i> name of the user to retrieve groups for
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups
     * @param pageRequest
     *            defines the page of groups to retrieve
     * @return the requested page of groups, which may be empty but never {@code null}
     * @see #findGroupsWithoutUser(String, String, PageRequest)
     * @see #findUsersWithGroup(String, String, PageRequest)
     * @see com.pmi.tpd.core.userIUserService#findGroupsByUser(String, PageRequest)
     */
    @Nonnull
    Page<GroupRequest> findGroupsWithUser(@Nonnull String username,
        @Nullable String groupName,
        @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of groups which the specified user is <i>not</i> a member of, with full {@link DetailedGroup
     * details}, optionally filtering the returned results to those containing the specified {@code groupName}. If the
     * provided {@code groupName} is {@code null} or empty groups will not be filtered. If no user exists with the
     * specified {@code username}, no groups will be returned.
     *
     * @param username
     *            the <i>exact</i> name of the user to retrieve groups for
     * @param groupName
     *            0 or more characters to apply as a filter on returned groups
     * @param pageRequest
     *            defines the page of groups to retrieve
     * @return the requested page of groups, which may be empty but never {@code null}
     * @see #findGroupsWithUser(String, String, PageRequest)
     * @see #findUsersWithGroup(String, String, PageRequest)
     * @see com.pmi.tpd.core.userIUserService#findGroupsByUser(String, PageRequest)
     */
    @Nonnull
    Page<GroupRequest> findGroupsWithoutUser(@Nonnull String username,
        @Nullable String groupName,
        @Nonnull Pageable pageRequest);

    /**
     * Find a password reset request using the token generated by {@link #requestPasswordReset(String)}.
     *
     * @param token
     *            token identifying the password reset request
     * @return the user matching the password reset request or {@code null} if no request matches the token or if the
     *         request has expired
     * @see #requestPasswordReset(String)
     */
    @Nullable
    UserRequest findUserByPasswordResetToken(@Nonnull String token);

    /**
     * Retrieves a page of users with full {@link DetailedUser details}.
     * <p>
     * Note: To filter the retrieved users by name, use {@link #findUsersByName(String, Pageable)} instead.
     * </p>
     *
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return the requested page of users, which may be empty but never {@code null}
     * @see #findUsersByName(String, PageRequest)
     * @see com.pmi.tpd.core.userIUserService#findUsers(PageRequest)
     */
    @Nonnull
    Page<UserRequest> findUsers(@Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of users, optionally filtering the returned results to those containing the specified
     * {@code username}. If the provided {@code username} is {@code null}, this method behaves identically to
     * {@link #findUsers(PageRequest)}. Otherwise, the {@code username} is matched against:
     * <ul>
     * <li>{@link IUser#getDisplayName() Display names}</li>
     * <li>{@link IUser#getEmail() E-mail addresses}</li>
     * <li>{@link IUser#getName() Usernames}</li>
     * </ul>
     *
     * @param username
     *            0 or more characters to apply as a filter on returned users
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return the requested page of users, potentially filtered, which may be empty but never {@code null}
     * @see #findUsers(Pageable)
     * @see com.pmi.tpd.core.userIUserService#findUsersByName(String, Pageable)
     */
    @Nonnull
    Page<UserRequest> findUsersByName(@Nullable String username, @Nonnull Pageable pageRequest);

    /**
     * Find the users within a group that match the page request.
     *
     * @param groupName
     *            name of the group the users must belong to
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return Returns the requested page of users, which may be empty but never {@code null}
     */
    @Nonnull
    Page<UserRequest> findUsersWithGroup(@Nonnull String groupName, @Nonnull Pageable pageRequest);

    /**
     * Find the users outside a group that match the page request.
     *
     * @param groupName
     *            name of the group the users must not belong to
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return Returns the requested page of users, which may be empty but never {@code null}
     */
    @Nonnull
    Page<UserRequest> findUsersWithoutGroup(@Nonnull String groupName, @Nonnull Pageable pageRequest);

    /**
     * Retrieves full {@link UserRequest details} for the user with the specified {@link IUser#getName() username}, or
     * {@code null} if no such user exists.
     *
     * @param username
     *            the <i>exact</i> username of the user to retrieve
     * @return full {@link UserRequest details} for the specified user, or {@code null} if no user
     */
    @Nullable
    UserRequest getUserDetails(@Nonnull String username);

    /**
     * Retrieves full details for the group with the specified groupName, or null if no such group exists.
     *
     * @param groupName
     *            the <i>exact</i> group name of the group to retrieve
     * @return full {@link GroupRequest details} for the specified group, or {@code null} if no group
     */
    @Nullable
    GroupRequest getGroupDetails(@Nonnull String groupName);

    /**
     * Retrieves full {@link UserRequest details} for the specified {@link IUser user}. This method is intended to
     * "promote" from a {@link IUser} to a {@link UserRequest}, providing access to mutability details and other
     * information for the user.
     *
     * @param user
     *            the user to retrieve details for
     * @return full {@link UserRequest details} for the specified user
     * @throws NoSuchUserException
     *             if the specified {@code user} does not exist in the underlying user store
     */
    @Nonnull
    UserRequest getUserDetails(@Nonnull IUser user);

    /**
     * Removes a user from a group.
     *
     * @param groupName
     *            name of the group the user will be removed from
     * @param username
     *            name of the user to remove from the group
     * @throws ForbiddenException
     *             if the group grants {@link Permission#SYS_ADMIN SYS_ADMIN} permission but the current user is not a
     *             SYS_ADMIN
     * @throws IntegrityException
     *             if the current user belongs to the specified group and removing them from the group would revoke
     *             their {@link Permission#SYS_ADMIN SYS_ADMIN} or {@link Permission#ADMIN ADMIN} permission
     * @throws NoSuchGroupException
     *             if the specified group does not exist
     * @throws NoSuchUserException
     *             if the specified user does not exist
     */
    void removeUserFromGroup(@Nonnull String groupName, @Nonnull String username)
            throws ForbiddenException, IntegrityException, NoSuchGroupException, NoSuchUserException;

    /**
     * Change the name of a user.
     *
     * @param currentUsername
     *            the current name of the user
     * @param newUsername
     *            the new name of the user
     * @return the newly renamed user
     * @throws NoSuchUserException
     *             if the specified user does not exist
     * @throws com.pmi.tpd.core.exception.UsernameAlreadyExistsException
     *             if a user already exists in the directory with the new name
     * @throws ForbiddenException
     *             if renaming is not supported by the directory the target user belongs to
     */
    @Nonnull
    UserRequest renameUser(@Nonnull String currentUsername, @Nonnull String newUsername);

    /**
     * Generates a unique token which can be used to perform a {@link #resetPassword(String, String) password reset} for
     * the specified user and e-mails it to the address associated with their account.
     *
     * @param username
     *            username of the user
     * @throws MailException
     *             if the e-mail notification could not be sent to the user (ex: the mail server is down)
     * @throws NoMailHostConfigurationException
     *             if no e-mail server has been configured
     * @throws NoSuchUserException
     *             if the user does not exist
     */
    void requestPasswordReset(@Nonnull String username) throws MailException, NoSuchUserException;

    /**
     * Resets the password for the {@link IUser user} associated with the specified token to the provided value.
     *
     * @param token
     *            the token identifying the user whose password should be reset
     * @param password
     *            the new password for the user
     * @throws InvalidTokenException
     *             if no user matches the specified token
     */
    void resetPassword(@Nonnull String token, @Nonnull String password) throws InvalidTokenException;

    /**
     * Updates the password of the specified user.
     * <p>
     * Note: A {@link Permission#ADMIN ADMIN} cannot update the password of a {@link Permission#SYS_ADMIN SYS_ADMIN}.
     * </p>
     *
     * @param username
     *            the user's username
     * @param newPassword
     *            the user's new password
     */
    void updatePassword(@Nonnull String username, @Nonnull String newPassword);

    /**
     * Updates the {@link IUser#getDisplayName() display name} and {@link IUser#getEmail() e-mail address} of the
     * specified user.
     * <p>
     * Note: A {@link Permission#ADMIN ADMIN} cannot update the details of a {@link Permission#SYS_ADMIN SYS_ADMIN}.
     * </p>
     *
     * @return Returns a {@link UserUpdate} instance representing the updated user.
     * @param user
     *            the user to update
     * @throws UserEmailAlreadyExistsException
     *             if a user has same email address.
     */
    @Nonnull
    UserRequest updateUser(@Nonnull UserUpdate user) throws UserEmailAlreadyExistsException;

    /**
     * Activate or deactivate user.
     *
     * @param username
     *            the user name to activate (can not be empty or {@code null}).
     * @param activated
     *            if {@code true} activate the user, otherwise deactivate.
     * @return Returns a {@link UserRequest} instance representing the updated user.
     */
    @Nonnull
    UserRequest activateUser(@Nonnull String username, boolean activated);

    /**
     * Updates the {@link IUser#getDisplayName() display name} and {@link IUser#getEmail() e-mail address} of the
     * specified user.
     * <p>
     * Note: A {@link Permission#ADMIN ADMIN} cannot update the details of a {@link Permission#SYS_ADMIN SYS_ADMIN}.
     * </p>
     *
     * @return Returns a {@link UserRequest} instance representing the updated user.
     * @param username
     *            the user's username
     * @param displayName
     *            the user's new display name
     * @param emailAddress
     *            the user's new email address
     * @throws UserEmailAlreadyExistsException
     *             if a user has same email address.
     */
    @Nonnull
    UserRequest updateUser(@Nonnull String username, @Nonnull String displayName, @Nonnull String emailAddress)
            throws UserEmailAlreadyExistsException;

}
