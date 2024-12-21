package com.pmi.tpd.core.user.permission;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.security.permission.Permission.ADMIN;
import static com.pmi.tpd.security.permission.Permission.SYS_ADMIN;
import static com.pmi.tpd.security.support.CommonValidations.validateGrantablePermission;
import static com.pmi.tpd.security.support.CommonValidations.validateGroup;
import static com.pmi.tpd.security.support.CommonValidations.validatePageRequest;
import static com.pmi.tpd.security.support.CommonValidations.validateUser;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.pmi.tpd.api.Product;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.exception.IntegrityException;
import com.pmi.tpd.api.exception.ServerException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.lifecycle.SimpleCancelState;
import com.pmi.tpd.api.paging.IPageProvider;
import com.pmi.tpd.api.paging.PagedIterable;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.event.permission.GlobalPermissionGrantRequestedEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionGrantedEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionModificationRequestedEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionModifiedEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionRevocationRequestedEvent;
import com.pmi.tpd.core.event.permission.GlobalPermissionRevokedEvent;
import com.pmi.tpd.core.event.user.GroupCleanupEvent;
import com.pmi.tpd.core.event.user.UserCleanupEvent;
import com.pmi.tpd.core.exception.RequestCanceledException;
import com.pmi.tpd.core.model.user.GlobalPermissionEntity;
import com.pmi.tpd.core.model.user.UserConverter;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.core.user.permission.internal.InIteratorPredicate;
import com.pmi.tpd.core.user.permission.spi.IGlobalPermissionRepository;
import com.pmi.tpd.database.support.IdentifierUtils;
import com.pmi.tpd.security.AuthorisationException;
import com.pmi.tpd.security.ForbiddenException;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.security.annotation.Secured;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.IPermissionService;
import com.pmi.tpd.security.permission.IPermittedGroup;
import com.pmi.tpd.security.permission.IPermittedUser;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.SetPermissionRequest;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@Service
@Transactional(readOnly = true)
public class DefaultPermissionAdminService implements IPermissionAdminService {

    /** */
    private static final Comparator<String> CASE_INSENSITIVE_COMPARATOR = IdentifierUtils::compareToInLowerCase;

    /** */
    private static final Comparator<IUser> CASE_INSENSITIVE_NAME_COMPARATOR = (u1, u2) -> IdentifierUtils
            .compareToInLowerCase(u1.getName(), u2.getName());

    /** */
    public static final int INTERNAL_PAGE_LIMIT = 500;

    /** */
    private final IEventPublisher eventPublisher;

    /** */
    private final IGlobalPermissionRepository globalPermissionDao;

    /** */
    private final I18nService i18nService;

    /** */
    private final IPermissionService permissionService;

    /** */
    private final IPermissionValidationService permissionValidationService;

    /** */
    private final IAuthenticationContext authenticationContext;

    /** */
    private final IUserService userService;

    /** */
    private final UserConverter userConverter;

    /**
     * @param userService
     * @param globalPermissionDao
     * @param projectPermissionDao
     * @param repositoryPermissionDao
     * @param authenticationContext
     * @param permissionService
     * @param permissionValidationService
     * @param i18nService
     * @param licenseService
     * @param eventPublisher
     */
    @Inject
    public DefaultPermissionAdminService(final IUserService userService, final UserConverter userConverter,
            final IGlobalPermissionRepository globalPermissionDao, final IAuthenticationContext authenticationContext,
            final IPermissionService permissionService, final IPermissionValidationService permissionValidationService,
            final I18nService i18nService, final IEventPublisher eventPublisher) {
        this.userService = userService;
        this.userConverter = userConverter;
        this.globalPermissionDao = globalPermissionDao;
        this.authenticationContext = authenticationContext;
        this.permissionService = permissionService;
        this.permissionValidationService = permissionValidationService;
        this.i18nService = i18nService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public Page<IPermittedUser> findUsersWithGlobalPermission(final String filter,
        @Nonnull final Pageable pageRequest) {
        return searchUsers(filter, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public Page<IUser> findUsersWithoutGlobalPermission(@Nonnull final Pageable pageRequest) {
        return searchUsersLacking(pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public Page<IPermittedGroup> findGroupsWithGlobalPermission(final String filter,
        @Nonnull final Pageable pageRequest) {
        return searchGroups(filter, pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public Page<String> findGroupsWithoutGlobalPermission(@Nonnull final Pageable pageRequest) {
        return searchGroupsLacking(pageRequest);
    }

    @Nonnull
    @Secured("Secured internally using the PermissionValidationService")
    public Page<IPermittedUser> searchUsers(@Nullable final String filter, @Nonnull final Pageable pageRequest) {
        validatePageRequest(pageRequest);

        permissionValidationService.validateForGlobal(Permission.ADMIN);

        return globalPermissionDao.findHighestPermissionPerUser(filter, pageRequest);
    }

    @Nonnull
    @Secured("Secured internally using the PermissionValidationService")
    public Page<IPermittedGroup> searchGroups(@Nullable final String filter, @Nonnull final Pageable pageRequest) {
        validatePageRequest(pageRequest);

        permissionValidationService.validateForGlobal(Permission.ADMIN);

        return globalPermissionDao.findHighestPermissionPerGroup(filter, pageRequest);
    }

    @Nonnull
    @Secured("Secured internally using the PermissionValidationService")
    public Page<IUser> searchUsersLacking(@Nonnull final Pageable pageRequest) {
        validatePageRequest(pageRequest);

        permissionValidationService.validateForGlobal(Permission.ADMIN);
        return globalPermissionDao.findUsersWithoutPermission(pageRequest);
    }

    /**
     * @param pageRequest
     *            bounds the page of groups to be returned.
     * @return a page containing zero or more groups matching the specified criteria, ordered by name.
     */
    @Nonnull
    @Secured("Secured internally using the PermissionValidationService")
    public Page<String> searchGroupsLacking(@Nonnull final Pageable pageRequest) {
        validatePageRequest(pageRequest);
        permissionValidationService.validateForGlobal(Permission.ADMIN);
        return globalPermissionDao.findGroupsWithoutPermission(pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void revokeAllGlobalPermissions(@Nonnull final String group) {
        validateGroup(group);

        final Permission revoked = doRevokeAllGlobalPermissions(group);
        if (revoked != null) {
            fireGlobalPermissionRequestedEvent(revoked, null, group, null);
            fireGlobalPermissionEvent(revoked, null, group, null);
        }
    }

    private Permission doRevokeAllGlobalPermissions(final String group) {
        if (permissionService.hasGlobalGroupPermission(Permission.SYS_ADMIN, group)
                && !permissionService.hasGlobalPermission(Permission.SYS_ADMIN)) {
            throw new IntegrityException(i18nService
                    .createKeyedMessage("app.service.permissionadmin.revoke.insufficient.permission", group));
        }

        Permission revoked = null;
        for (final Permission permission : Permission.getGlobalPermissions()) {
            final GlobalPermissionEntity perm = buildPermission(permission, group, null);
            if (globalPermissionDao.hasPermissionEntry(perm) && revokeGlobalPermission(permission, group)) {
                revoked = Permission.max(revoked, permission);
            }
        }
        return revoked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void revokeAllGlobalPermissions(@Nonnull final IUser user) {
        validateUser(user);

        final Permission revoked = doRevokeAllGlobalPermissions(user);
        if (revoked != null) {
            fireGlobalPermissionRequestedEvent(revoked, null, null, user);
            fireGlobalPermissionEvent(revoked, null, null, user);
        }
    }

    private Permission doRevokeAllGlobalPermissions(final IUser user) {
        // As an optimisation check the current users permission before checking the target users
        // It is more likely that the current users permissions are cached or the current thread's
        // permissions have been elevated
        if (!permissionService.hasGlobalPermission(Permission.SYS_ADMIN)
                && permissionService.hasGlobalPermission(user, Permission.SYS_ADMIN)) {
            throw new IntegrityException(
                    i18nService.createKeyedMessage("app.service.permissionadmin.revoke.insufficient.permission",
                        user.getDisplayName()));
        }

        Permission revoked = null;
        for (final Permission permission : Permission.getGlobalPermissions()) {
            final GlobalPermissionEntity perm = buildPermission(permission, null, user);
            if (globalPermissionDao.hasPermissionEntry(perm) && revokeGlobalPermission(permission, user)) {
                revoked = Permission.max(revoked, permission);
            }
        }
        return revoked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Secured("Permissions checks done internally")
    public void setPermission(@Nonnull final SetPermissionRequest request) {
        final Permission permission = request.getPermission();
        // final Submission submission= request.getSubmission();

        final boolean hasInsufficientGlobalPermission = permission.isGlobal()
                && !permissionService.hasGlobalPermission(Permission.max(permission, ADMIN));
        // final boolean hasInsufficientSubmissionPermission = permission.isResource(Submission.class)
        // && !permissionService.hasSubmissionPermission(submisstion, PROJECT_ADMIN);

        final boolean hasInsufficientPermission = hasInsufficientGlobalPermission;
        // || hasInsufficientSubmissionPermission;

        if (hasInsufficientPermission) {
            throw new AuthorisationException(i18nService
                    .createKeyedMessage("app.service.permissionadmin.usercannotgrantpermission", permission));
        } else {
            doSetPermission(request);
        }
    }

    private void doSetPermission(@Nonnull final SetPermissionRequest request) {
        final Permission permission = request.getPermission();
        final Set<IUser> users = request.getUsers();
        final Set<String> groups = request.getGroups();

        validateGrantablePermission(permission);

        if (permission.isGlobal()) {
            for (final IUser user : users) {
                final Permission revoked = doRevokeAllGlobalPermissions(user);
                fireGlobalPermissionRequestedEvent(revoked, permission, null, user);
                grantGlobalPermission(permission, user);
                fireGlobalPermissionEvent(revoked, permission, null, user);
            }
            for (final String group : groups) {
                final Permission revoked = doRevokeAllGlobalPermissions(group);
                fireGlobalPermissionRequestedEvent(revoked, permission, group, null);
                grantGlobalPermission(permission, group);
                fireGlobalPermissionEvent(revoked, permission, group, null);
            }
        } else {
            throw new ServerException(
                    i18nService.createKeyedMessage("app.service.permissionadmin.cannotgrantunknownpermission",
                        Product.getName(),
                        permission));
        }
    }

    // private IPageProvider<IUser> createUserProvider(final String usernamePattern) {
    // return request -> {
    // final Page<? extends IUser> page = userService.findUsersByName(usernamePattern, request);
    //
    // return PageUtils.asPageOf(IUser.class, page);
    // };
    // }

    private void grantGlobalPermission(@Nonnull final Permission permission, @Nonnull final IUser user) {
        final GlobalPermissionEntity perm = buildPermission(permission, null, user);
        grantPermission(perm);
    }

    private void grantGlobalPermission(@Nonnull final Permission permission, @Nonnull final String group) {
        final GlobalPermissionEntity perm = buildPermission(permission, group, null);
        grantPermission(perm);
    }

    private void grantPermission(final GlobalPermissionEntity perm) {
        if (!globalPermissionDao.hasPermissionEntry(perm)) {
            globalPermissionDao.save(perm);
        }
    }

    private boolean revokeGlobalPermission(@Nonnull final Permission permission, @Nonnull final IUser user) {
        // revoke is always allowee else's permission
        boolean revokeAllowed = !user.getUsername()
                .equals(authenticationContext.getCurrentUser().orElseThrow().getUsername());

        if (!revokeAllowed) {
            // revoke not yet allowed since user is working on their own permissions
            // we gate now if they are revoking a permission and they hold a higher permission
            final Set<Permission> implyingPermissions = permission.getImplyingPermissions();

            for (final Permission higherPermission : implyingPermissions) {
                revokeAllowed = permissionService.hasGlobalPermission(higherPermission);
                if (revokeAllowed) {
                    break;
                }
            }
        }

        if (!revokeAllowed) {
            // The current user is removing their own permission and they do not have a higher permission
            // Gate through now if they have the permission through group membership
            revokeAllowed = permissionService.hasGlobalPermissionThroughGroupMembership(permission,
                Collections.<String> emptySet());
        }

        if (revokeAllowed) {
            return revokePermission(GlobalPermissionEntity.builder()
                    .permission(permission)
                    .user(this.userConverter.convertToEntityUser(user))
                    .build());
        } else {
            final KeyedMessage message = i18nService
                    .createKeyedMessage("app.service.permissionadmin.cantrevokeownpermission");
            throw new IntegrityException(message);
        }
    }

    private boolean revokePermission(final GlobalPermissionEntity globalPermission) {
        return globalPermissionDao.revoke(globalPermission) > 0;
    }

    private boolean revokeGlobalPermission(@Nonnull final Permission permission, @Nonnull final String group) {
        IUser currentUser = authenticationContext.getCurrentUser()
                .map(this.userConverter::convertToEntityUser)
                .orElseThrow();

        // attach entity to current session
        currentUser = this.userService.getUserById(currentUser.getId());

        // Revoke can always go ahead if the user is removing permission from a group
        // of which they are not a member
        boolean revokeAllowed = currentUser == null || !userService.isUserInGroup(currentUser, group);

        if (!revokeAllowed) {
            // revoke not yet allowed since user is working on their own group
            // we gate now if they are revoking a permission and they hold a higher permission
            final Set<Permission> implyingPermissions = permission.getImplyingPermissions();

            for (final Permission higherPermission : implyingPermissions) {
                revokeAllowed = permissionService.hasGlobalPermission(higherPermission);
                if (revokeAllowed) {
                    break;
                }
            }
        }

        if (!revokeAllowed) {
            // The current user is removing the permission of a group of which they are a member and they do not have a
            // higher permission
            // Gate through now if they have explicit user permission
            revokeAllowed = permissionService.hasDirectGlobalUserPermission(permission);
        }

        if (!revokeAllowed) {
            // The user is removing a group permission which gives them their own permission and they does not have
            // direct permission themselves. Do they have permission through another group?
            revokeAllowed = permissionService.hasGlobalPermissionThroughGroupMembership(permission,
                Collections.singleton(group));
        }

        if (revokeAllowed) {
            return revokePermission(GlobalPermissionEntity.builder().permission(permission).group(group).build());
        } else {
            final KeyedMessage message = i18nService
                    .createKeyedMessage("app.service.permissionadmin.cantrevokegrouppermission", group);
            throw new IntegrityException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @PreAuthorize("not isCurrentUser(#username) and (hasGlobalPermission('SYS_ADMIN') or "
            + "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#username, 'SYS_ADMIN')))")
    @Override
    public void revokeAllUserPermissions(@Nonnull final String username) {
        validateUser(username); // both isCurrentUser(username) and hasGlobalPermission(username, perm) accepts null
                                // usernames

        final IUser user = userService.getUserByName(username);
        if (user != null) {
            revokeAllUserPermissions(user);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("not isCurrentUser(#user) and (hasGlobalPermission('SYS_ADMIN') or "
            + "(hasGlobalPermission('ADMIN') and not hasGlobalPermission(#user, 'SYS_ADMIN')))")
    public void revokeAllUserPermissions(@Nonnull final IUser user) {
        checkNotNull(user, "user");
        globalPermissionDao.revokeAll(user.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void revokeAllGroupPermissions(@Nonnull final String name) {
        validateGroup(name);
        canRemovePermissionsFromGroup(name);

        globalPermissionDao.revokeAll(name);
    }

    private void canRemovePermissionsFromGroup(final String name) {
        try {
            canDeleteGroup(name);
        } catch (final IntegrityException e) {
            final KeyedMessage message = i18nService
                    .createKeyedMessage("app.service.permissionadmin.cannotremovepermissionsgroup");
            throw new IntegrityException(message);
        } catch (final ForbiddenException e) {
            final KeyedMessage message = i18nService.createKeyedMessage("app.service.permissionadmin.cannotdeletegroup",
                "You cannot remove the permissions of the group {0} as "
                        + "it has System Administrator privileges, and you are" + " not a System Administrator.",
                name);
            throw new ForbiddenException(message);
        }
    }

    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void canRemoveUserFromGroup(@Nonnull final String username, @Nonnull final String group) {
        validateUser(username);
        validateGroup(group);

        final IUser currentUser = authenticationContext.getCurrentUser().orElse(null);

        if (currentUser != null && IdentifierUtils.equalsInLowerCase(currentUser.getUsername(), username)) {
            // removing self from group
            if (!userService.isUserInGroup(currentUser, group)) {
                // user is not a member of the group so this can proceed
                return;
            }
            if (!canRemoveCurrentUserFromGroup(group)) {
                final KeyedMessage message = i18nService
                        .createKeyedMessage("app.service.permissionadmin.cannotremoveself");
                throw new IntegrityException(message);
            }
        } else {
            // removing another user from a group. This is ok unless the group has sys-admin and user does not have
            // sys-admin
            if (permissionService.hasGlobalGroupPermission(SYS_ADMIN, group)
                    && !permissionService.hasGlobalPermission(SYS_ADMIN)) {
                final KeyedMessage message = i18nService
                        .createKeyedMessage("app.service.permissionadmin.cannotremoveuser", username, group);
                throw new ForbiddenException(message);
            }
        }

    }

    private boolean canRemoveCurrentUserFromGroup(final String group) {
        if (permissionService.hasGlobalGroupPermission(SYS_ADMIN, group)) {
            // The group has sys-admin privilege. The user can only be removed from the group if they have sys-admin
            // through a different means
            if (!permissionService.hasDirectGlobalUserPermission(SYS_ADMIN) && !permissionService
                    .hasGlobalPermissionThroughGroupMembership(SYS_ADMIN, Collections.singleton(group))) {
                return false;
            }
        } else if (permissionService.hasGlobalGroupPermission(Permission.ADMIN, group)) {
            // The group has admin privilege. This can proceed if the user has sys-admin or
            // they have admin privilege through another means
            if (!permissionService.hasGlobalPermission(SYS_ADMIN)
                    && !permissionService.hasDirectGlobalUserPermission(Permission.ADMIN)
                    && !permissionService.hasGlobalPermissionThroughGroupMembership(Permission.ADMIN,
                        Collections.singleton(group))) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void canAddUserToGroup(@Nonnull final String group) {
        validateGroup(group);

        // We can only add a user to a group if we are a sys-admin or the group to which we are adding the user
        // does not have sys-admin privilege
        if (!permissionService.hasGlobalPermission(SYS_ADMIN)
                && permissionService.hasGlobalGroupPermission(SYS_ADMIN, group)) {
            final KeyedMessage message = i18nService
                    .createKeyedMessage("app.service.permissionadmin.cannotaddusertogroup", group);
            throw new ForbiddenException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void canDeleteGroup(@Nonnull final String group) {
        validateGroup(group);

        final IUser currentUser = authenticationContext.getCurrentUser().orElse(null);
        if (currentUser != null && userService.isUserInGroup(currentUser, group)) {
            // we can delete the group if we can safely remove this user from the group
            if (!canRemoveCurrentUserFromGroup(group)) {
                final KeyedMessage message = i18nService
                        .createKeyedMessage("app.service.permissionadmin.cannotremovegroup");
                throw new IntegrityException(message);
            }
        } else {
            // we are deleting a group, which we are not a member of
            // we can not delete if we do not have SYS-ADMIN but the group does
            if (!permissionService.hasGlobalPermission(SYS_ADMIN)
                    && permissionService.hasGlobalGroupPermission(SYS_ADMIN, group)) {
                final KeyedMessage message = i18nService
                        .createKeyedMessage("app.service.permissionadmin.cannotdeletegroup", group);
                throw new ForbiddenException(message);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void canDeleteUser(@Nonnull final String username) {
        validateUser(username);
        canDeleteUser(userService.getUserByName(username));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void canDeleteUser(@Nonnull final IUser user) {
        if (user == null) {
            // there is no such user
            return;
        }
        final String username = user.getUsername();
        validateUser(username);

        final IUser currentUser = authenticationContext.getCurrentUser().orElse(null);
        // A user cannot delete themselves
        if (currentUser != null && IdentifierUtils.equalsInLowerCase(currentUser.getUsername(), username)) {
            final KeyedMessage message = i18nService.createKeyedMessage("app.service.permissionadmin.selfdelete");
            throw new IntegrityException(message);
        }

        // We do not allow a user to delete a sys-admin if the user themselves is not a sys-admin
        if (permissionService.hasGlobalPermission(user, SYS_ADMIN)
                && !permissionService.hasGlobalPermission(SYS_ADMIN)) {
            final KeyedMessage message = i18nService.createKeyedMessage("app.service.permissionadmin.cannotdeleteuser",
                user.getUsername());
            throw new ForbiddenException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('ADMIN')")
    public void canActivateUser(@Nonnull final IUser user, final boolean activate) {
        if (user == null) {
            // there is no such user
            return;
        }
        final String username = user.getUsername();
        validateUser(username);

        final IUser currentUser = authenticationContext.getCurrentUser().orElse(null);
        // A user cannot deactivate themselves
        if (!activate && currentUser != null
                && IdentifierUtils.equalsInLowerCase(currentUser.getUsername(), username)) {
            final KeyedMessage message = i18nService.createKeyedMessage("app.service.permissionadmin.selfdeactivate");
            throw new IntegrityException(message);
        }

        // We do not allow a user to deactivate or activate a sys-admin if the user themselves is not a sys-admin
        if (permissionService.hasGlobalPermission(user, SYS_ADMIN)
                && !permissionService.hasGlobalPermission(SYS_ADMIN)) {
            final KeyedMessage message = activate
                    ? i18nService.createKeyedMessage("app.service.permissionadmin.cannotactivateuser",
                        user.getUsername())
                    : i18nService.createKeyedMessage("app.service.permissionadmin.cannotdeactivateuser",
                        user.getUsername());
            throw new ForbiddenException(message);
        }
    }

    /**
     * @param event
     *            the event on deleted group
     */
    @EventListener
    public void onGroupDeleted(final GroupCleanupEvent event) {
        // DO NOT CALL revokeAllGroupPermissions here! The validation performed by canRemovePermissionsFromGroup, called
        // by that method, can only trigger exceptions that will be swallowed by the event framework. It is not
        // possible,
        // in this method, to prevent group deletion. It's only possible to clean up after the deleted group.
        globalPermissionDao.revokeAll(event.getGroup());
    }

    /**
     * @param event
     *            the event on deleted user
     */
    @EventListener
    public void onUserDeleted(final UserCleanupEvent event) {
        // DO NOT CALL revokeAllUserPermissions here! The event provides the deleted user already; there is no need
        // to perform another database query for it. As with onGroupDeleted, it is not possible to prevent user deletion
        // in this method. It's only possible to clean up after the deleted user.
        //
        // Note: This will also revoke a user's access to its personal project. That access is restored in another event
        // listener in ProjectServiceImpl
        globalPermissionDao.revokeAll(event.getDeletedUser().getId());
    }

    private GlobalPermissionEntity buildPermission(final Permission permission, final String group, final IUser user) {
        return GlobalPermissionEntity.builder()
                .permission(permission)
                .user(this.userConverter.convertToEntityUser(user))
                .group(group)
                .build();
    }

    private void fireGlobalPermissionEvent(final Permission oldValue,
        final Permission newValue,
        final String group,
        final IUser user) {
        eventPublisher
                .publish(visitPermissionAction(oldValue, newValue, new PermissionActionVisitor<PermissionEvent>() {

                    @Override
                    public GlobalPermissionGrantedEvent visitPermissionGranted() {
                        return new GlobalPermissionGrantedEvent(DefaultPermissionAdminService.this, newValue, group,
                                user);
                    }

                    @Override
                    public GlobalPermissionModifiedEvent visitPermissionModified() {
                        return new GlobalPermissionModifiedEvent(DefaultPermissionAdminService.this, oldValue, newValue,
                                group, user);
                    }

                    @Override
                    public GlobalPermissionRevokedEvent visitPermissionRevoked() {
                        return new GlobalPermissionRevokedEvent(DefaultPermissionAdminService.this, oldValue, group,
                                user);
                    }
                }));
    }

    private void fireGlobalPermissionRequestedEvent(final Permission oldValue,
        final Permission newValue,
        final String group,
        final IUser user) {
        final SimpleCancelState cancelState = new SimpleCancelState();
        eventPublisher
                .publish(visitPermissionAction(oldValue, newValue, new PermissionActionVisitor<PermissionEvent>() {

                    @Override
                    public GlobalPermissionGrantRequestedEvent visitPermissionGranted() {
                        return new GlobalPermissionGrantRequestedEvent(DefaultPermissionAdminService.this, newValue,
                                group, user, cancelState);
                    }

                    @Override
                    public GlobalPermissionModificationRequestedEvent visitPermissionModified() {
                        return new GlobalPermissionModificationRequestedEvent(DefaultPermissionAdminService.this,
                                oldValue, newValue, group, user, cancelState);
                    }

                    @Override
                    public GlobalPermissionRevocationRequestedEvent visitPermissionRevoked() {
                        return new GlobalPermissionRevocationRequestedEvent(DefaultPermissionAdminService.this,
                                oldValue, group, user, cancelState);
                    }
                }));

        maybeCancelRequest(oldValue, newValue, cancelState);
    }

    /**
     * @throws RequestCanceledException
     *             if the permission action was cancelled by an event listener. The exception is unchecked, and as a
     *             result any work done previous to this exception will be rolled back at the transaction boundary
     */
    private void maybeCancelRequest(final Permission oldValue,
        final Permission newValue,
        final SimpleCancelState cancelState) {
        if (cancelState.isCanceled()) {
            throw visitPermissionAction(oldValue, newValue, new PermissionActionVisitor<RequestCanceledException>() {

                @Override
                public PermissionGrantCanceledException visitPermissionGranted() {
                    return new PermissionGrantCanceledException(
                            i18nService.createKeyedMessage("app.service.permissionadmin.grantcanceled"),
                            cancelState.getCancelMessages());
                }

                @Override
                public PermissionModificationCanceledException visitPermissionModified() {
                    return new PermissionModificationCanceledException(
                            i18nService.createKeyedMessage("app.service.permissionadmin.modificationcanceled"),
                            cancelState.getCancelMessages());
                }

                @Override
                public PermissionRevocationCanceledException visitPermissionRevoked() {
                    return new PermissionRevocationCanceledException(
                            i18nService.createKeyedMessage("app.service.permissionadmin.revocationcanceled"),
                            cancelState.getCancelMessages());
                }
            });
        }
    }

    /**
     * @param provider
     *            a group page provider for the groups to exclude
     * @return a stateful Predicate which filters a target iterator to not contain groups in the specified provider
     */
    @VisibleForTesting
    static Predicate<String> withoutGroups(final IPageProvider<String> provider) {
        return Predicates.not(new InIteratorPredicate<>(CASE_INSENSITIVE_COMPARATOR,
                new PagedIterable<>(provider, INTERNAL_PAGE_LIMIT).iterator()));
    }

    /**
     * @param provider
     *            a user page provider for the users to exclude
     * @return a stateful Predicate which filters a target iterator to not contain users in the specified provider
     */
    @VisibleForTesting
    static Predicate<IUser> withoutUsers(final IPageProvider<IUser> provider) {
        return Predicates.not(new InIteratorPredicate<>(CASE_INSENSITIVE_NAME_COMPARATOR,
                new PagedIterable<>(provider, INTERNAL_PAGE_LIMIT).iterator()));
    }

    private static <T> T visitPermissionAction(final Permission existingPermission,
        final Permission newPermission,
        final PermissionActionVisitor<T> visitor) {
        if (existingPermission == null) {
            return visitor.visitPermissionGranted();
        } else if (newPermission == null) {
            return visitor.visitPermissionRevoked();
        } else {
            return visitor.visitPermissionModified();
        }
    }

    @SuppressWarnings("unused")
    private <R> R validateRequest(final R request) {
        return checkNotNull(request, "request");
    }

    /**
     * @author devacfr<christophefriederich@mac.com>
     * @param <T>
     */
    private interface PermissionActionVisitor<T> {

        T visitPermissionGranted();

        T visitPermissionModified();

        T visitPermissionRevoked();
    }

}
