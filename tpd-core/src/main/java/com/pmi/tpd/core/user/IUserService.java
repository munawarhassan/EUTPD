package com.pmi.tpd.core.user;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.exception.ServerException;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserProfileRequest;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.api.user.UserSettings;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;
import com.pmi.tpd.core.exception.ExpiredPasswordAuthenticationException;
import com.pmi.tpd.core.exception.InactiveUserAuthenticationException;
import com.pmi.tpd.core.exception.IncorrectPasswordAuthenticationException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.exception.UsernameAlreadyExistsException;

/**
 * <p>
 * IUserService interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUserService {

  /**
   * Attempts to authenticate the specified user given their password.
   *
   * @param username
   *                 the username
   * @param password
   *                 the user's password
   * @return the {@link IUser} representing the authenticated user
   * @throws AuthenticationSystemException
   *                                                  if a failure occurs in Crowd
   * @throws ExpiredPasswordAuthenticationException
   *                                                  if the user's password has
   *                                                  expired and must be changed
   * @throws InactiveUserAuthenticationException
   *                                                  if the specified user is
   *                                                  inactive
   * @throws IncorrectPasswordAuthenticationException
   *                                                  if the provided password is
   *                                                  incorrect
   * @throws NoSuchUserException
   *                                                  if the specified user does
   *                                                  not exist or their user
   *                                                  details cannot be retrieved
   * @see #unauthenticate()
   * @since 2.0
   */
  IUser authenticate(@Nonnull String username, @Nonnull String password);

  /**
   * Authenticate as user {@code username}, without checking passwords. Should
   * only be used for trusted logins.
   *
   * @param username
   *                 the username
   * @return the {@link IUser} representing the authenticated user.
   * @since 2.0
   */
  @Nullable
  IUser preauthenticate(@Nonnull String username);

  /**
   * Clears the {@link #authenticate(String, String) authentication} for the
   * current user, logging them out.
   *
   * @since 2.0
   */
  void unauthenticate();

  /**
   * Retrieves a flag indicating whether the specified group exists.
   *
   * @param groupName
   *                  the group name
   * @return {@code true} if a group with the specified name exists in any
   *         directory; otherwise, {@code false}
   */
  boolean existsGroup(String groupName);

  /**
   * Creates a new user and optionally adds them to the default group and activate
   * it , if it exists.
   * <p>
   * A non-{@code null}, non-blank {@code password} must be provided. It may be
   * further vetted by the user store, for
   * example by applying complexity restrictions. Alternatively, if an e-mail
   * server has been configured, new users
   * can be created
   * {@link #createUserWithGeneratedPassword(String, String, String, String) with
   * generated passwords},
   * allowing new users to set their own password when they first access the
   * system.
   * </p>
   * <p>
   * This method is not intended to be exposed via services like REST for general
   * consumption. It exists to satisfy
   * specific use cases where the system may wish to create a user <i>without</i>
   * adding them to the default group.
   * Generally it is expected that new users should be added to the default group,
   * as it defines the set of "common"
   * permissions for all users of the system.
   * </p>
   *
   * @param userRequest
   *                          the user for the new user
   * @param activate
   *                          indicate if the new user is activate directly.
   * @param addToDefaultGroup
   *                          indicate if add user to the default group
   * @return Return new instance of {@link UserRequest} representing the new
   *         created user.
   * @throws UsernameAlreadyExistsException
   *                                         if a user has same email address.
   * @throws UserEmailAlreadyExistsException
   *                                         if the user already exists.
   */
  UserRequest createUser(UserRequest userRequest, boolean activate, boolean addToDefaultGroup)
      throws UsernameAlreadyExistsException, UserEmailAlreadyExistsException;

  /**
   * Retrieves a page of {@link IUser#isActivated() active} users.
   * <p>
   * Note: To filter the retrieved users by name, use
   * {@link #findUsersByName(String, Pageable)} instead.
   * </p>
   *
   * @param pageRequest
   *                    defines the page of users to retrieve
   * @return the requested page of users, which may be empty but never
   *         {@code null}
   * @see #findUsersByName(String, Pageable)
   * @since 2.0
   */
  @Nonnull
  Page<UserRequest> findUsers(@Nonnull Pageable pageRequest);

  /**
   * Retrieves a page of {@link UserRequest active users}, optionally filtering
   * the returned results to those
   * containing the specified {@code username}. If the provided {@code username}
   * is {@code null}, this method behaves
   * identically to {@link #findUsers(Pageable)}. Otherwise, the {@code username}
   * is matched against:
   * <ul>
   * <li>{@link IUser#getDisplayName() Display names}</li>
   * <li>{@link IUser#getEmail() E-mail addresses}</li>
   * <li>{@link IUser#getName() Usernames}</li>
   * </ul>
   *
   * @param username
   *                    0 or more characters to apply as a filter on returned
   *                    users
   * @param pageRequest
   *                    defines the page of users to retrieve
   * @return the requested page of users, potentially filtered, which may be empty
   *         but never {@code null}
   * @see #findUsers(Pageable)
   * @since 2.0
   */
  @Nonnull
  Page<UserRequest> findUsersByName(@Nullable String username, @Nonnull Pageable pageRequest);

  /**
   * Retrieves a page of {@link IUser#isActivated() active} users which are
   * members of the specified group. The
   * {@code groupName} provided must match <i>exactly</i> in order for any results
   * to be returned.
   *
   * @param groupName
   *                    the <i>exact</i> name of the group to retrieve members for
   * @param pageRequest
   *                    defines the page of users to retrieve
   * @return the requested page of users, which may be empty but never
   *         {@code null}
   * @see #findGroupsByUser(String, Pageable)
   * @since 2.0
   */
  @Nonnull
  Page<UserRequest> findUsersByGroup(@Nonnull String groupName, @Nonnull Pageable pageRequest);

  /**
   * Retrieves a flag indicating whether the specified user is a member of the
   * specified group. If the provided user
   * does not exist in the user store, or if the group does not exist,
   * {@code false} is returned in preference to
   * throwing an exception.
   *
   * @param currentUser
   *                    the user to query group membership for
   * @param groupName
   *                    the <i>exact</i> name of the group to query membership in
   * @return {@code true} if the specified user is a member of the specified
   *         group; otherwise, {@code false} if the
   *         user does not exist in the backing store, the group does not exist or
   *         the user is not a member of the
   *         group.
   * @since 2.0
   */
  boolean isUserInGroup(IUser currentUser, String groupName);

  /**
   * Retrieves a flag indicating whether the specified user is a member of the
   * specified group. If the user or group
   * does not exist, {@code false} is returned in preference to throwing an
   * exception.
   *
   * @param username
   *                  the <i>exact</i> name of the user to query group membership
   *                  for
   * @param groupName
   *                  the <i>exact</i> name of the group to query membership in
   * @return {@code true} if the specified user is a member of the specified
   *         group; otherwise, {@code false} if the
   *         user does not exist, the group does not exist or the user is not a
   *         member of the group
   * @since 2.0
   */
  boolean isUserInGroup(@Nonnull String username, @Nonnull String groupName);

  /**
   * Retrieves a page of groups.
   * <p>
   * Note: To filter the retrieved groups by name, use
   * {@link #findGroupsByName(String, Pageable)} instead.
   * </p>
   *
   * @param pageRequest
   *                    defines the page of groups to retrieve
   * @return the requested page of groups, which may be empty but never
   *         {@code null}
   * @see #findGroupsByName(String, Pageable)
   * @since 2.0
   */
  @Nonnull
  Page<String> findGroups(@Nonnull Pageable pageRequest);

  /**
   * Retrieve the first group whose group name matches the provided {@code value}.
   *
   * @param groupName
   *                  the value to match, first against group name.
   * @return Returns a group whose group name matches the specified value, or
   *         {@code null} if no matching group was
   *         found
   * @since 2.0
   */
  @Nullable
  IGroup findGroupByName(@Nonnull String groupName);

  /**
   * Retrieves a page of groups, optionally filtering the returned results to
   * those containing the specified
   * {@code groupName}. If the provided {@code groupName} is {@code null}, this
   * method behaves identically to
   * {@link #findGroups(PageRequest)}.
   *
   * @param groupName
   *                    0 or more characters to apply as a filter on returned
   *                    groups
   * @param pageRequest
   *                    defines the page of groups to retrieve
   * @return the requested page of groups, potentially filtered, which may be
   *         empty but never {@code null}.
   * @see #findGroups(Pageable)
   * @since 2.0
   */
  @Nonnull
  Page<String> findGroupsByName(@Nullable String groupName, @Nonnull Pageable pageRequest);

  /**
   * Retrieves a page of active groups which the specified user is a member of.
   * The {@code username} provided must
   * match <i>exactly</i> in order for any results to be returned.
   *
   * @param username
   *                 the <i>exact</i> name of the user to retrieve groups for
   * @param request
   *                 defines the page of groups to retrieve
   * @return the requested page of groups, which may be empty but never
   *         {@code null}
   * @see #findUsersByGroup(String, Pageable)
   * @since 2.0
   */
  Page<String> findGroupsByUser(String username, Pageable request);

  /**
   * Retrieve the first {@link IUser#isActivated() active} user whose username or
   * e-mail address <i>exactly</i>
   * matches the provided {@code value}.
   * <p>
   * Usernames are the preferred match, so they will be tested first. If no user
   * exists with a username matching the
   * value, e-mail addresses will then be checked.
   * </p>
   *
   * @param value
   *              the value to match, first against usernames and then against
   *              e-mail addresses
   * @return a user whose username or e-mail address matches the specified value,
   *         or {@code null} if no matching user
   *         was found
   * @since 2.0
   */
  @Nullable
  UserRequest findUserByNameOrEmail(@Nonnull String value);

  /**
   * Retrieves a flag indicating whether the specified username exists.
   *
   * @param username
   *                 the username
   * @return {@code true} if a user with the specified username exists in any
   *         directory; otherwise, {@code false}.
   */
  boolean existsUser(String username);

  /**
   * Retrieves a {@link IUser} by its {@link IUser#getId() ID}.
   * <p>
   * This method will <i>not</i> return deleted or inactive users; use
   * {@link #getUserById(long, boolean)} instead if
   * deleted users are desired. See the class documentation for more details about
   * what "deleted" means in this
   * context.
   * </p>
   *
   * @param id
   *           the ID of the user to retrieve
   * @return the user with the specified ID, or {@code null} if no matching user
   *         exists
   * @since 2.0
   */
  @Nullable
  IUser getUserById(long id);

  /**
   * Retrieves a {@link IUser} by its {@link IUser#getId() ID}.
   * <p>
   * If requested, this method will return deleted or inactive users. See the
   * class documentation for more details
   * about what "deleted" means in this context.
   * </p>
   *
   * @param id
   *                 the ID of the user to retrieve
   * @param inactive
   *                 {@code true} if deleted and inactive users should be
   *                 returned, {@code false} otherwise.
   * @return the user with the specified ID, or {@code null} if no matching user
   *         exists
   * @since 2.0
   */
  @Nullable
  IUser getUserById(long id, boolean inactive);

  /**
   * Retrieves an {@link ApplicationUser#isActive() active}
   * {@link ApplicationUser} by its exact (case-sensitive)
   * {@link ApplicationUser#getSlug() slug}.
   * <p>
   * This method will <i>not</i> return deleted or inactive users. See the class
   * documentation for more details about
   * what "deleted" means in this context.
   *
   * @param slug
   *             the <i>exact</i> slug of the user to retrieve
   * @return the user with the specified slug, or {@code null} if no matching user
   *         exists <i>or</i> the matching user
   *         has been deleted or is inactive
   * @since 2.4
   */
  @Nullable
  IUser getUserBySlug(@Nonnull String slug);

  /**
   * Retrieves an {@link IUser#isActive() active} {@link IUser} by its
   * {@link IUser#getName() username}.
   * <p>
   * This method will <i>not</i> return deleted or inactive users; use
   * {@link #getUserByName(String, boolean)} instead
   * if deleted users are desired. See the class documentation for more details
   * about what "deleted" means in this
   * context.
   * </p>
   *
   * @param username
   *                 the <i>exact</i> username of the user to retrieve
   * @return the user with the specified username, or {@code null} if no matching
   *         user exists <i>or</i> the matching
   *         user has been deleted from the user store
   * @since 2.0
   */
  @Nullable
  IUser getUserByName(@Nonnull String username);

  /**
   * Retrieves a {@link IUser} by its {@link IUser#getName() username}, optionally
   * returning the user even if it has
   * been removed from the underlying user store.
   * <p>
   * If requested, this method will return deleted or inactive users. See the
   * class documentation for more details
   * about what "deleted" means in this context.
   *
   * @param username
   *                 the <i>exact</i> username of the user to retrieve
   * @param inactive
   *                 {@code true} if deleted and inactive users should be
   *                 returned, {@code false} otherwise.
   * @return the user with the specified username, or {@code null} if no matching
   *         user exists <i>or</i> the matching
   *         user has been deleted from the user store and deleted users were not
   *         requested
   * @since 2.0
   */
  @Nullable
  IUser getUserByName(@Nonnull String username, boolean inactive);

  /**
   * Updates the {@link IUser#getDisplayName() display name} and
   * {@link IUser#getEmail() e-mail address} for the
   * <i>current user</i>.
   * <p>
   * The current user always has permission to modify their own details. However,
   * the underlying user store may not
   * support the operation. A {@link ServerException} will be thrown if the user
   * store is read-only.
   *
   * @param user
   *             the current user.
   * @return the current user, with the updated details.
   * @throws UserEmailAlreadyExistsException
   *                                         if the email address exist for
   *                                         another user.
   * @throws ServerException
   *                                         if the underlying user store does not
   *                                         support updating users' details.
   * @since 2.0
   */
  @Nonnull
  UserProfileRequest updateUserProfile(UserProfileRequest user) throws UserEmailAlreadyExistsException;

  /**
   * @param username
   * @return
   * @since 2.4
   */
  @Nonnull
  UserProfileRequest getUserProfile(@Nonnull String username);

  /**
   * Updates the password for the <i>current user</i>.
   * <p>
   * The current user always has permission to modify their own password. However,
   * the underlying user store may not
   * support the operation, or it may apply specific requirements to the
   * complexity of the new password. If the user
   * store is read-only, or the password does not meet complexity requirements, a
   * {@link ServerException} is thrown.
   * </p>
   *
   * @param currentPassword
   *                        the current user's current password
   * @param newPassword
   *                        the current user's desired new password
   * @throws IncorrectPasswordAuthenticationException
   *                                                  if the current password
   *                                                  provided does not match the
   *                                                  user's current password
   * @throws ServerException
   *                                                  if the underlying user store
   *                                                  does not support updating
   *                                                  users' passwords
   */
  void updatePassword(String currentPassword, String newPassword)
      throws IncorrectPasswordAuthenticationException, ServerException;

  /**
   * convert {@code user} to {@link UserRequest}.
   *
   * @param user
   *             the user to convert.
   * @return Return new instance of {@link UserRequest} representing the
   *         {@code user}.
   */
  UserRequest.Builder toUserRequest(IUser user);

  /**
   * Retrieves the current avatar for the specified user at a requested size.
   * Avatars are square, so the size provided
   * here is used as both height and width for the avatar.
   * <p>
   * The requested size will be normalised to fall within a well-defined set
   * sizes. The supported sizes are:
   * <ul>
   * <li>256</li>
   * <li>128</li>
   * <li>96</li>
   * <li>64</li>
   * <li>48</li>
   * </ul>
   * Any size larger than 256 will be normalised down to 256, and any size smaller
   * than 48 will be normalised up to
   * 48. Otherwise, sizes are normalised to the next size up, where they don't
   * match exactly: 56 will be normalised to
   * 64, 100 will be normalised to 128, and so on.
   *
   * @param user
   *             the user whose avatar should be retrieved
   * @param size
   *             the desired height and width for the avatar
   * @return a supplier which can be used to access the requested avatar
   * @since 2.4
   */
  @Nonnull
  ICacheableAvatarSupplier getAvatar(@Nonnull IUser user, int size);

  /**
   * Updates the specified user's avatar, replacing it with the one contained in
   * the provided {@link IAvatarSupplier
   * supplier}. Updating a user's avatar also updates the avatar for their
   * {@link PersonalProject personal project}.
   * <p>
   * Previous avatars <i>are not retained</i>. When a user's avatar is updated,
   * the previous avatar is removed. To
   * reuse a previous avatar, it must be provided again.
   *
   * @param user
   *                 the user to update the avatar for
   * @param supplier
   *                 a supplier providing access to the new avatar to use
   * @since 2.4
   */
  void updateAvatar(@Nonnull IUser user, @Nonnull IAvatarSupplier supplier);

  /**
   * Delete the avatar associated with a user.
   * <p>
   * This will revert the avatar of the user in the UI to his/her Gravatar image
   * (if the Gravatar integration is
   * enabled) or to the default user avatar (if the Gravatar integration is
   * disabled)
   *
   * @param user
   *             the user whose (local) avatar will be removed
   * @since 2.4
   */
  void deleteAvatar(@Nonnull IUser user);

  /**
   * Updates the specified user's avatar, replacing it with the one contained in
   * the provided data URI. Updating a
   * user's avatar also updates the avatar for their {@link PersonalProject
   * personal project}.
   * <p>
   * The data URI is required to contain Base64-encoded image data, and should be
   * in the format: <code>
   *     data:(content type, e.g. image/png);base64,(data)
   * </code> If the data is not Base64-encoded, or if a character set is defined
   * in the URI, it will be rejected.
   * <p>
   * Previous avatars <i>are not retained</i>. When a project's avatar is updated,
   * the previous avatar is removed. To
   * reuse a previous avatar, it must be provided again.
   *
   * @param user
   *             the user to update the avatar for
   * @param uri
   *             a data URI containing a Base64-encoded avatar image
   * @since 2.4
   */
  void updateAvatar(@Nonnull IUser user, @Nonnull String uri);

  /**
   * @param settings
   * @return
   * @throws ApplicationException
   * @since 2.4
   */
  @Nonnull
  UserProfileRequest updateUserSettings(@Nonnull IUser user, @Nonnull UserSettings settings)
      throws ApplicationException;

  @Nonnull
  UserProfileRequest updateLanguage(@Nonnull final IUser user, @Nonnull final String language)
      throws ApplicationException;

  /**
   * @param user
   * @return
   * @since 3.0
   */
  Optional<UserSettings> toUserSettings(final IUser user);
}
