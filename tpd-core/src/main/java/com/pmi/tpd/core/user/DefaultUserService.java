package com.pmi.tpd.core.user;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Suppliers;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.exception.ServerException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.User;
import com.pmi.tpd.api.user.UserDirectory;
import com.pmi.tpd.api.user.UserProfileRequest;
import com.pmi.tpd.api.user.UserProfileRequest.UserProfileRequestBuilder;
import com.pmi.tpd.api.user.UserRequest;
import com.pmi.tpd.api.user.UserSettings;
import com.pmi.tpd.api.user.avatar.AvatarSize;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;
import com.pmi.tpd.core.avatar.INavBuilder;
import com.pmi.tpd.core.avatar.UserAvatarUpdatedEvent;
import com.pmi.tpd.core.avatar.impl.DataUriAvatarMetaSupplier;
import com.pmi.tpd.core.avatar.spi.IInternalAvatarService;
import com.pmi.tpd.core.event.user.UserCreatedEvent;
import com.pmi.tpd.core.exception.IncorrectPasswordAuthenticationException;
import com.pmi.tpd.core.exception.NoSuchUserException;
import com.pmi.tpd.core.exception.UserEmailAlreadyExistsException;
import com.pmi.tpd.core.exception.UsernameAlreadyExistsException;
import com.pmi.tpd.core.model.user.AbstractVoidUserEntityVisitor;
import com.pmi.tpd.core.model.user.GroupEntity;
import com.pmi.tpd.core.model.user.UserEntity;
import com.pmi.tpd.core.security.IAuthenticationService;
import com.pmi.tpd.core.security.provider.IDirectory;
import com.pmi.tpd.core.user.preference.IPreferences;
import com.pmi.tpd.core.user.preference.IUserPreferencesManager;
import com.pmi.tpd.core.user.spi.IGroupRepository;
import com.pmi.tpd.core.user.spi.IPasswordResetHelper;
import com.pmi.tpd.core.user.spi.IUserRepository;
import com.pmi.tpd.database.support.IdentifierUtils;
import com.pmi.tpd.security.AuthenticationException;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.annotation.Unsecured;
import com.pmi.tpd.security.spring.UserAuthenticationToken;
import com.querydsl.core.types.Predicate;

/**
 * <p>
 * DefaultUserService class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultUserService implements IUserService {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserService.class);

    /* */
    private final IEventPublisher eventPublisher;

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final I18nService i18nService;

    /** */
    private final IUserRepository userRepository;

    /** */
    private final IGroupRepository groupRepository;

    /** */
    private final IUserPreferencesManager userPreferencesManager;

    /** */
    private final IPasswordResetHelper passwordHelper;

    /** */
    private final Provider<IAuthenticationService> authenticationServiceProvider;

    /** */
    private final IInternalAvatarService avatarService;

    /** */
    private final INavBuilder navBuilder;

    /**
     * <p>
     * Constructor for DefaultUserService.
     * </p>
     *
     * @param userRepository
     *                                      user Repository.
     * @param groupRepository
     *                                      group repository
     * @param userPreferencesManager
     *                                      a user preference manager.
     * @param passwordHelper
     *                                      a password encoder.
     * @param authenticationContext
     *                                      the authentication context.
     * @param i18nService
     *                                      the localisation service.
     * @param authenticationServiceProvider
     *                                      the authentication provider service.
     */
    @Inject
    public DefaultUserService(@Nonnull final IEventPublisher eventPublisher, @Nonnull final INavBuilder navBuilder,
            @Lazy @Nonnull final IInternalAvatarService avatarService, @Nonnull final IUserRepository userRepository,
            @Nonnull final IGroupRepository groupRepository,
            @Nonnull final IUserPreferencesManager userPreferencesManager,
            @Nonnull final IAuthenticationContext authenticationContext,
            @Nonnull final IPasswordResetHelper passwordHelper, @Nonnull final I18nService i18nService,
            @Nonnull final Provider<IAuthenticationService> authenticationServiceProvider) {
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.navBuilder = checkNotNull(navBuilder, "navBuilder");
        this.avatarService = checkNotNull(avatarService, "avatarService");
        this.userRepository = checkNotNull(userRepository, "userRepository");
        this.groupRepository = checkNotNull(groupRepository, "groupRepository");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.userPreferencesManager = checkNotNull(userPreferencesManager, "userPreferencesManager");
        this.passwordHelper = checkNotNull(passwordHelper, "passwordHelper");
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.authenticationServiceProvider = checkNotNull(authenticationServiceProvider,
            "authenticationServiceProvider");
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @Transactional(propagation = Propagation.SUPPORTS,
            noRollbackFor = { AuthenticationException.class, NoSuchUserException.class })
    @Unsecured("This needs to be available to unauthenticated contexts")
    public IUser authenticate(@Nonnull final String username, @Nonnull final String password) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Authenticating user: {}", username);
        }
        return authenticationServiceProvider.get().authenticate(username, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Unsecured("This needs to be available to unauthenticated contexts")
    public IUser preauthenticate(@Nonnull final String username) {
        LOGGER.debug("Authenticating user: {}", username);
        final IUser user = getUserByName(username);
        if (user != null) {
            SecurityContextHolder.getContext().setAuthentication(UserAuthenticationToken.forUser(user));
        }
        return user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This needs to be available to all contexts")
    public void unauthenticate() {
        SecurityContextHolder.clearContext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This needs to be available in unauthenticated contexts")
    public boolean existsGroup(final String groupName) {
        return groupRepository.exists(groupName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This needs to be available in unauthenticated contexts")
    public IGroup findGroupByName(final String groupName) {
        return groupRepository.findByName(groupName);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public Page<String> findGroups(@Nonnull final Pageable pageRequest) {
        return groupRepository.findGroups(pageRequest).map(GroupEntity::getName);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public Page<String> findGroupsByName(@Nullable final String groupName, @Nonnull final Pageable pageRequest) {
        return groupRepository.findGroupsByName(groupName, pageRequest).map(GroupEntity::getName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("Used in register account  and admin user creation")
    public UserRequest createUser(final UserRequest userRequest,
        final boolean activate,
        final boolean addToDefaultGroup) throws UsernameAlreadyExistsException, UserEmailAlreadyExistsException {
        if (userRepository.existsUser(userRequest.getUsername())) {
            throw new UsernameAlreadyExistsException(i18nService
                    .createKeyedMessage("app.service.user.usernamealreadyexists", userRequest.getUsername()));
        }
        if (userRepository.existsEmail(userRequest.getEmail())) {
            throw new UserEmailAlreadyExistsException(
                    i18nService.createKeyedMessage("app.service.user.emailalreadyexists", userRequest.getEmail()));
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating User: {}", userRequest);
        }
        final UserEntity user = userRepository.save(UserEntity.builder()
                .activated(true)
                .email(userRequest.getEmail())
                .displayName(userRequest.getDisplayName())
                .activated(activate)
                .directory(
                    userRequest.getDirectory() == null ? UserDirectory.defaultDirectory() : userRequest.getDirectory())
                .username(userRequest.getUsername())
                .password(userRequest.getPassword() != null ? passwordHelper.encodePassord(userRequest.getPassword())
                        : "NO_PASSWORD")
                .build());
        if (addToDefaultGroup) {
            addToGroupIfExisting(user, ApplicationConstants.Security.DEFAULT_GROUP_USER_CODE);
        }
        this.eventPublisher.publish(new UserCreatedEvent(this, user));
        return toUserRequest(user).build();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @PreAuthorize("isCurrentUser(#userRequest)")
    @Transactional
    public UserProfileRequest updateUserProfile(@Nonnull final UserProfileRequest userRequest)
            throws UserEmailAlreadyExistsException {
        checkNotNull(userRequest, "userRequest");
        final String username = userRequest.getUsername();

        if (userRepository.existsEmail(userRequest.getEmail(), userRequest.getUsername())) {
            i18nService.createKeyedMessage("app.service.user.emailalreadyexists", userRequest.getEmail());
        }
        final UserEntity user = userRepository.findByName(username);
        if (user == null) {
            throw new NoSuchUserException(
                    i18nService.createKeyedMessage("app.service.user.nosuchuser", userRequest.getUsername()),
                    userRequest.getUsername());
        }

        return toUserProfileRequest(new DefaultUserProfile(
                userRepository.save(
                    user.copy().displayName(userRequest.getDisplayName()).email(userRequest.getEmail()).build()),
                userPreferencesManager, navBuilder, avatarService)).build();

    }

    @Nonnull
    @Override
    @PreAuthorize("isCurrentUser(#username)")
    @Transactional
    public UserProfileRequest getUserProfile(@Nonnull final String username) {
        final UserEntity user = userRepository.findByName(username);
        if (user == null) {
            throw new NoSuchUserException(i18nService.createKeyedMessage("app.service.user.nosuchuser", username),
                    username);
        }
        return toUserProfileRequest(new DefaultUserProfile(user, userPreferencesManager, navBuilder, avatarService))
                .build();
    }

    @Nonnull
    @Override
    @PreAuthorize("isCurrentUser(#user)")
    @Transactional
    public UserProfileRequest updateUserSettings(@Nonnull final IUser user, @Nonnull final UserSettings settings)
            throws ApplicationException {
        final Optional<IPreferences> pref = userPreferencesManager.getPreferences(user);
        if (pref.isPresent()) {
            pref.get().setString(UserPreferenceKeys.USER_I18_LOCALE, settings.getLangKey());
            pref.get()
                    .setString(UserPreferenceKeys.AVATAR_SOURCE,
                        settings.getAvatarSource() != null ? settings.getAvatarSource().name() : null);
        }
        return toUserProfileRequest(new DefaultUserProfile(user, userPreferencesManager, navBuilder, avatarService))
                .build();
    }

    @Nonnull
    @Override
    @PreAuthorize("isCurrentUser(#user)")
    @Transactional
    public UserProfileRequest updateLanguage(@Nonnull final IUser user, @Nonnull final String language)
            throws ApplicationException {
        final Optional<IPreferences> pref = userPreferencesManager.getPreferences(user);
        if (pref.isPresent()) {
            pref.get().setString(UserPreferenceKeys.USER_I18_LOCALE, language);
        }
        return toUserProfileRequest(new DefaultUserProfile(user, userPreferencesManager, navBuilder, avatarService))
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(final @Nullable String currentPassword, final @Nullable String newPassword)
            throws IncorrectPasswordAuthenticationException, ServerException {
        // get the username of the current user
        final String name = getCurrentUserForUpdate().getName();

        try {
            // check if the old password matches the known one
            final IUser user = authenticationServiceProvider.get().authenticate(name, currentPassword);
            passwordHelper.resetPassword(user, newPassword);
        } catch (final IncorrectPasswordAuthenticationException e) {
            throw new IncorrectPasswordAuthenticationException(
                    i18nService.createKeyedMessage("app.service.user.password.invalid"));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("Used in unauthenticated by permission checks and login processing")
    public Page<String> findGroupsByUser(final String username, final Pageable request) {

        final UserEntity user = userRepository.findByName(username);
        if (user == null) {
            return PageUtils.createEmptyPage(request);
        }
        return PageUtils.createPage(from(user.getGroups()).filter(@org.checkerframework.checker.nullness.qual.Nullable GroupEntity::isActive), request)
                .map(group -> IdentifierUtils.toLowerCase(group.getName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Unsecured("This needs to be available in unauthenticated contexts; it is used for password resets")
    public UserRequest findUserByNameOrEmail(@Nonnull final String value) {
        final Predicate predicate = userRepository.entity().username.equalsIgnoreCase(value)
                .or(userRepository.entity().email.equalsIgnoreCase(value));
        return toUserRequest(this.userRepository.findOne(predicate).orElse(null)).build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This needs to be available in unauthenticated contexts")
    public boolean existsUser(@Nonnull final String username) {
        return this.userRepository.existsUser(username);
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @PreAuthorize("isAuthenticated()")
    public Page<UserRequest> findUsers(@Nonnull final Pageable pageRequest) {
        return userRepository.findUsers(pageRequest).map(user -> toUserRequest(user).build());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @Unsecured("Used in unauthenticated contexts by permission checks and login processing")
    public Page<UserRequest> findUsersByGroup(@Nonnull final String groupName, @Nonnull final Pageable pageRequest) {
        return userRepository.findUsersWithGroup(groupName, pageRequest).map(user -> toUserRequest(user).build());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    @PreAuthorize("isAuthenticated()")
    public Page<UserRequest> findUsersByName(final String username, @Nonnull final Pageable pageRequest) {
        return userRepository.findByName(username, pageRequest).map(user -> toUserRequest(user).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This needs to be available in unauthenticated contexts")
    public IUser getUserById(final long id) {
        return getUserById(id, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This needs to be available in unauthenticated contexts")
    public IUser getUserById(final long id, final boolean inactive) {
        return maybeFilterInactive(userRepository.getById(id), inactive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Unsecured("This needs to be available in unauthenticated contexts")
    @Nullable
    public IUser getUserByName(@Nonnull final String username) {
        return getUserByName(username, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Unsecured("This needs to be available in unauthenticated contexts")
    @Nullable
    public IUser getUserByName(@Nonnull final String username, final boolean inactive) {
        checkNotNull(username, "username");
        return maybeFilterInactive(userRepository.findByName(checkNotNull(username, "username")), inactive);
    }

    @Override
    @Unsecured("This needs to be available in unauthenticated contexts")
    public IUser getUserBySlug(@Nonnull final String slug) {
        checkNotNull(slug, "slug");
        return maybeFilterInactive(userRepository.findBySlug(slug), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public boolean isUserInGroup(@Nonnull final IUser user, @Nonnull final String groupName) {
        checkNotNull(user, "user");
        checkNotNull(groupName, "groupName");

        // resolve lazily initialize groups collection
        final UserEntity currentUser = this.userRepository.getById(user.getId());

        return from(currentUser.getGroups()).transform(group -> group.getName().toLowerCase())
                .contains(groupName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unsecured("This is not restricted by permissions so that SAL tests pass. It should not be exposed via REST")
    public boolean isUserInGroup(@Nonnull final String username, @Nonnull final String groupName) {
        checkNotNull(username, "username");
        checkNotNull(groupName, "groupName");

        // Note: This _may_ create a user in a read-only transaction (meaning the
        // newly-created user is rolled
        // back when this method call returns). Because the user is not returned from
        // this method, that rollback
        // is acceptable. It's desirable not to have a read/write transaction here,
        // because this method is used
        // extensively in permission checks.
        final IUser user = getUserByName(username);
        return user != null && isUserInGroup(user, groupName);
    }

    @Nonnull
    @Override
    @Unsecured("User avatars are not more privileged than getUserBySlug(String)")
    public ICacheableAvatarSupplier getAvatar(@Nonnull final IUser user, final int size) {
        checkNotNull(user, "user");

        return avatarService.getForUser(user, size);
    }

    @Override
    // this _must_ be be the same permission check as _updateAvatar()_
    // users can delete their own avatars, admins can delete all avatars except
    // sysadmins',
    // sysadmins can delete all avatars
    @PreAuthorize("isCurrentUser(#user) or hasGlobalPermission('SYS_ADMIN') or "
            + "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#user, 'SYS_ADMIN'))")
    public void deleteAvatar(@Nonnull final IUser user) {
        avatarService.deleteForUser(checkNotNull(user, "user"));
    }

    @Override
    @PreAuthorize("isCurrentUser(#user) or " + // Users can always change their own avatar
            "hasGlobalPermission('SYS_ADMIN') or " + // SYS_ADMINs can change anyone's avatar
            // ADMINs can change anyone's avatar except SYS_ADMINs
            "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#user, 'SYS_ADMIN'))")
    public void updateAvatar(@Nonnull final IUser user, @Nonnull final IAvatarSupplier supplier) {
        checkNotNull(supplier, "supplier");

        doUpdateAvatar(user, Suppliers.ofInstance(supplier));
    }

    @Override
    @PreAuthorize("isCurrentUser(#user) or " + // Users can always change their own avatar
            "hasGlobalPermission('SYS_ADMIN') or " + // SYS_ADMINs can change anyone's avatar
            // ADMINs can change anyone's avatar except SYS_ADMINs
            "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#user, 'SYS_ADMIN'))")
    public void updateAvatar(@Nonnull final IUser user, @Nonnull final String uri) {
        Assert.state(!checkNotNull(uri, "uri").trim().isEmpty(), "A non-blank data URI is required");

        doUpdateAvatar(user, new DataUriAvatarMetaSupplier(avatarService, uri));
    }

    /**
     * Retrieves the user identified by the specified {@code slug} and updates its avatar using an
     * {@link IAvatarSupplier} obtained from the provided {@code metaSupplier}.
     *
     * @param user
     *                     the user to update
     * @param metaSupplier
     *                     an {@link IAvatarSupplier} supplier, which may not return {@code null}
     * @since 2.4
     */
    private void doUpdateAvatar(@Nonnull final IUser user, @Nonnull final Supplier<IAvatarSupplier> metaSupplier) {
        avatarService.saveForUser(checkNotNull(user, "user"), checkNotNull(metaSupplier, "metaSupplier").get());
        eventPublisher.publish(new UserAvatarUpdatedEvent(this, user));
    }

    /**
     * {@inheritDoc}
     */
    @Unsecured("This is internal user for this service and DefaultUserAdminService")
    @Override
    public UserRequest.Builder toUserRequest(final IUser user) {
        if (user == null) {
            return null;
        }
        final UserRequest.Builder builder = UserRequest.builder(user);

        user.accept(new AbstractVoidUserEntityVisitor() {

            @Override
            protected void doVisit(final IUser user) {
                this.addPreference(user);
            }

            @Override
            protected void doVisit(final User user) {
                this.addPreference(user);
            }

            @Override
            protected void doVisit(final UserEntity user) {
                this.addPreference(user);
            }

            private UserRequest.Builder addPreference(final IUser user) {
                toUserSettings(user).ifPresent(settings -> {

                    builder.avatarUrl(avatarService.getUrlForPerson(user,
                        AvatarRequest.from(navBuilder, AvatarSize.Medium, settings.getAvatarSource())));

                    builder.langKey(settings.getLangKey());
                });
                return builder;
            }

        });

        return builder;
    }

    @Unsecured("This is internal user for this service and Rest resource")
    @Override
    @Nonnull
    public Optional<UserSettings> toUserSettings(final IUser user) {
        if (user == null) {
            return Optional.empty();
        }
        final UserSettings.UserSettingsBuilder settings = UserSettings.builder();

        final Optional<IPreferences> pref = userPreferencesManager.getPreferences(user);
        if (pref.isPresent()) {
            settings.langKey(pref.get().getString(UserPreferenceKeys.USER_I18_LOCALE).orElse(null));
            settings.avatarSource(getAvatarSource(pref.get(), user));
        }
        return Optional.of(settings.build());
    }

    public UserProfileRequestBuilder toUserProfileRequest(final DefaultUserProfile userProfile) {
        if (userProfile == null) {
            return null;
        }
        final IDirectory directory = authenticationServiceProvider.get().findDirectoryFor(userProfile.getUser());
        final UserProfileRequestBuilder builder = UserProfileRequest.from(userProfile)
                .readOnly(directory != null ? !directory.isUserUpdatable() : false);
        userProfile.getUser().accept(new AbstractVoidUserEntityVisitor() {

            @Override
            protected void doVisit(@Nonnull final IUser user) {
                builder.settings(toUserSettings(user).orElse(null));
            }

            @Override
            protected void doVisit(@Nonnull final User user) {
                builder.settings(toUserSettings(user).orElse(null));
            }

            @Override
            protected void doVisit(@Nonnull final UserEntity user) {
                builder.settings(toUserSettings(user).orElse(null));
            }

        });

        return builder;
    }

    @Nonnull
    private IUser getCurrentUserForUpdate() {
        return authenticationContext.getCurrentUser()
                .orElseThrow(() -> new AuthorisationException(
                        i18nService.createKeyedMessage("app.service.user.anonymousupdate")));
    }

    private void addToGroupIfExisting(final UserEntity user, final String groupName) {
        final GroupEntity group = groupRepository.findByName(groupName);
        if (group != null) {
            userRepository.addGroupMember(group, user);
        }
    }

    private <U extends IUser> U maybeFilterInactive(final U user, final boolean inactive) {
        return inactive || user != null && user.isActivated() ? user : null;
    }

    private AvatarSourceType getAvatarSource(final IPreferences pref, final IUser user) {
        return pref.getString(UserPreferenceKeys.AVATAR_SOURCE)
                .map(AvatarSourceType::valueOf)
                .orElse(avatarService.getDefaultSource());
    }

}
