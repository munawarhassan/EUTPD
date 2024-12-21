package com.pmi.tpd.core.user.spi;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.paging.IFilterable;
import com.pmi.tpd.core.exception.NoSuchGroupException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.QUserEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.database.jpa.IDslAccessor;

/**
 * Spring JPA repository interface associated to {@link UserEntity}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUserRepository extends IDslAccessor<UserEntity, Long> {

    @Override
    QUserEntity entity();

    /**
     * Find the {@code UserEntity user} associated to {@code username}.
     *
     * @param username
     *            the user name (can <b>not</b> be {@code null}).
     * @return Returns the user or {@code null} if no user can be found with that name.
     */
    @Nullable
    UserEntity findByName(@Nonnull String username);

    /**
     * Retrieves a user by its slug
     *
     * @param slug
     *            the slug
     * @return the user if found, otherwise {@code null}
     */
    @Nullable
    UserEntity findBySlug(@Nonnull String slug);

    /**
     * Retrieves a page of {@link UserEntity#isActivated() active} users, optionally filtering the returned results to
     * those containing the specified {@code username}. If the provided {@code username} is {@code null}, this method
     * behaves identically to {@link #findUsers(Pageable)}. Otherwise, the {@code username} is matched against:
     * <ul>
     * <li>{@link UserEntity#getDisplayName() Display names}</li>
     * <li>{@link UserEntity#getEmail() E-mail addresses}</li>
     * <li>{@link UserEntity#getName() Usernames}</li>
     * </ul>
     *
     * @param username
     *            0 or more characters to apply as a filter on returned users (can be {@code null})..
     * @param request
     *            defines the page of users to retrieve (can <b>not</b> be {@code null}).
     * @return Returns the requested page of users, potentially filtered, which may be empty but never {@code null}.
     * @see #findUsers(Pageable)
     * @since 2.0
     */
    @Nonnull
    Page<UserEntity> findByName(@Nullable String username, @Nonnull Pageable request);

    /**
     * @param request
     *            defines the page of groups to retrieve (accept {@link IFilterable}) (can <b>not</b> be {@code null}).
     * @return Returns the requested page of users, which may be empty but never {@code null}.
     */
    @Nonnull
    Page<UserEntity> findUsers(@Nonnull Pageable request);

    /**
     * Gets the indicating whether the user with {@code username} exists.
     *
     * @param username
     *            the user name to check (can <b>not</b> be {@code null}).
     * @return Returns {@code true} whether the user with {@code username} exists, {@code false} otherwise.
     */
    boolean existsUser(@Nonnull String username);

    /**
     * Gets the indicating whether exits user with specific {@code email} address.
     *
     * @param email
     *            email address to check (can <b>not</b> be {@code null}).
     * @return Returns {@code true} whether exits user with specific {@code email} address, {@code false} otherwise.
     */
    boolean existsEmail(@Nonnull String email);

    /**
     * @param email
     *            Gets the indicating whether exits user with specific {@code email} address and and excluding the user
     *            name {@code usernameToExclude} (can <b>not</b> be {@code null}).
     * @param usernameToExclude
     *            the user name to exclude in search (can <b>not</b> be {@code null}).
     * @return Returns {@code true} whether exits user with specific {@code email} address and and excluding the user
     *         name {@code usernameToExclude}, {@code false} otherwise.
     */
    boolean existsEmail(@Nonnull String email, @Nonnull String usernameToExclude);

    /**
     * Gets the user identifier for the {@code userKey}.
     *
     * @param userKey
     *            the user key (can <b>not</b> be {@code null}).
     * @return Returns a {@link Long} representing the user identifier or {@code null} if doesn't exist.
     */
    @Nullable
    Long getIdForUserKey(@Nonnull String userKey);

    /**
     * Find the users within a group that match the page request.
     *
     * @param groupName
     *            name of the group the users must belong to
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return Returns the requested page of users, which may be empty but never {@code null}.
     * @since 2.0
     */
    @Nonnull
    Page<UserEntity> findUsersWithGroup(@Nonnull String groupName, @Nonnull Pageable pageRequest);

    /**
     * Find the users outside a group that match the page request.
     *
     * @param groupName
     *            name of the group the users must not belong to
     * @param pageRequest
     *            defines the page of users to retrieve
     * @return Returns the requested page of users, which may be empty but never {@code null}.
     * @since 2.0
     */
    @Nonnull
    Page<UserEntity> findUsersWithoutGroup(@Nonnull String groupName, @Nonnull Pageable pageRequest);

    /**
     * Retrieves a page of normal users by their {@link UserEntity#getDeletedDate()} being earlier than a specified
     * date.
     *
     * @param date
     *            date to compare against (can <b>not</b> be {@code null}).
     * @param request
     *            the page request (can <b>not</b> be {@code null}).
     * @return Returns a set of matching {@link UserEntity users} which may be empty but never {@code null}.
     * @since 2.0
     */
    @Nonnull
    Page<UserEntity> findByDeletedDateEarlierThan(@Nonnull Date date, @Nonnull Pageable request);

    /**
     * Adds the provided {@code user} to the specified {@code group}.
     *
     * @param group
     *            the group to add the user to
     * @param user
     *            the user to add to the group
     * @throws AuthenticationSystemException
     *             if the Crowd operation fails
     * @throws NoSuchGroupException
     *             if the specified {@code GroupEntity} does not exist
     * @throws NoSuchUserException
     *             if the specified {@code UserEntity} does not exist
     */
    void addGroupMember(@Nonnull GroupEntity group, @Nonnull UserEntity user);

    /**
     * Removes the provided {@code user} from the specified {@code group}.
     * <p>
     * When multiple directories are configured, it is possible for groups with the same name to exist in multiple
     * directories, and also possible for multiple users with the same name to exist in multiple directories. These
     * duplicates are treated as the same groups and users, and generally that works well. However, it can lead to
     * unexpected behaviour for this method. Consider the following scenario:
     * </p>
     * <ul>
     * <li>User "someuser" exists in two directories, A and B</li>
     * <li>In directory A, "someuser" is a member of "somegroup"</li>
     * <li>In directory B, "someuser" is <i>not</i> a member of "somegroup" (there may not even be a group with that
     * name in directory B)</li>
     * </ul>
     * If the directory traversal order when looking up "someuser" by name returns the user from directory B, attempting
     * to remove "someuser" from "somegroup" (which {@link #findGroupsByUser(String, String, boolean, PageRequest)} will
     * indicate "someuser" is a member of) will return {@code false}, because the implementation removes the user from
     * the named group within the same directory.
     * <p>
     * Note: If "somegroup" didn't exist <i>at all</i> in directory B, in this example, {@link NoSuchGroupException}
     * would be thrown <i>even though {@link #findGroups(Pageable)} would return "somegroup" as existing</i>.
     * </p>
     *
     * @param group
     *            the group to remove the user from
     * @param user
     *            the user to remove from the group
     * @return {@code true} if the user has been removed from the group; otherwise, {@code false} if the user was not a
     *         member of the group (and therefore not removed)
     * @throws AuthenticationSystemException
     *             if the directory containing the user does not allow modifying the user's groups, or if some other
     *             random failure prevents updating them
     * @throws NoSuchGroupException
     *             if the specified group does not exist
     * @throws NoSuchUserException
     *             if the specified user does not exist
     */
    boolean removeGroupMember(GroupEntity group, UserEntity user);

    /**
     * Change the username of the specified {@code user}.
     *
     * @param user
     *            the user to rename
     * @param newUsername
     *            the target username
     * @return the renamed uer
     * @throws com.pmi.tpd.core.exception.UsernameAlreadyExistsException
     *             if a user with the username {@code newUsername} already exists
     */
    UserEntity renameUser(@Nonnull UserEntity user, @Nonnull String newUsername);

}
