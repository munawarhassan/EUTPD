package com.pmi.tpd.web.rest.rsrc.api.admin;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableSet;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.security.permission.IPermissionAdminService;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.web.core.rs.error.BadRequestException;
import com.pmi.tpd.web.core.rs.error.NotFoundException;

public abstract class AbstractPermissionResource {

    protected final IPermissionAdminService permissionAdminService;

    protected final IUserService userService;

    protected final I18nService i18nService;

    @Inject
    public AbstractPermissionResource(final I18nService i18nService,
            final IPermissionAdminService permissionAdminService, final IUserService userService) {
        this.i18nService = checkNotNull(i18nService, "i18nService");
        this.permissionAdminService = checkNotNull(permissionAdminService, "permissionAdminService");
        this.userService = checkNotNull(userService, "userService");
    }

    protected Permission validatePermission(final String permissionName, final Class<?> resourceClass) {
        if (permissionName == null) {
            final String message = i18nService.getMessage("app.rest.permissionadmin.missingpermission");
            throw new BadRequestException(message);
        }
        Permission permission;
        try {
            permission = Permission.valueOf(permissionName.toUpperCase().replace('-', '_'));

        } catch (final IllegalArgumentException e) {
            throw new BadRequestException(
                    i18nService.getMessage("app.rest.permissionadmin.invalidpermission", permissionName));
        }

        if (resourceClass != null && !permission.isResource(resourceClass)) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.rest.permission.admin.notapplicablewithresource",
                        permission.name(),
                        resourceClass));
        } else if (resourceClass == null && !permission.isGlobal()) {
            throw new BadRequestException(i18nService
                    .getMessage("app.rest.permission.admin.notapplicablewithoutresource", permission.name()));
        }

        if (!permission.isGrantable()) {
            throw new BadRequestException(
                    i18nService.getMessage("app.rest.permissionadmin.ungrantablepermission", permission.name()));
        }

        return permission;
    }

    protected String validateGroup(final String groupName, final boolean allowNonExistent) {
        if (StringUtils.isEmpty(groupName)) {
            throw new BadRequestException(i18nService.getMessage("app.rest.permission.admin.invalidgroup"));
        }

        // we don't check whether the group exists when deleting. It may have been deleted but still have permissions
        // bound to it.
        if (!allowNonExistent && !userService.existsGroup(groupName)) {
            throw new NotFoundException(i18nService.getMessage("app.rest.permission.nosuchgroup", groupName));
        }

        return groupName;
    }

    protected Set<String> validateGroups(final Set<String> groupNames, final boolean allowNonExistent) {
        if (groupNames == null || groupNames.isEmpty()) {
            final String message = i18nService.getMessage("app.rest.permission.no.groups");
            throw new BadRequestException(message);
        }

        final Set<String> groups = new HashSet<>(groupNames.size());
        final List<String> missing = new ArrayList<>();
        for (final String groupName : groupNames) {
            try {
                groups.add(validateGroup(groupName, allowNonExistent));
            } catch (final NotFoundException e) {
                missing.add(groupName);
            }
        }

        if (!missing.isEmpty()) {
            final String message = i18nService.getMessage("app.rest.permission.nosuchgroups",
                StringUtils.join(missing, ", "));
            throw new NotFoundException(message);
        }

        return ImmutableSet.copyOf(groups);
    }

    protected IUser validateUser(final String username, final boolean returnDeleted) {
        if (StringUtils.isEmpty(username)) {
            final String message = i18nService.getMessage("app.rest.permission.admin.invaliduser");
            throw new BadRequestException(message);
        }
        final IUser user = userService.getUserByName(username, returnDeleted);
        if (user == null) {
            final String message = i18nService.getMessage("app.rest.permission.nosuchuser", username);
            throw new NotFoundException(message);
        }
        return user;
    }

    protected Set<IUser> validateUsers(final Collection<String> usernames, final boolean returnDeleted) {
        if (usernames == null || usernames.isEmpty()) {
            final String message = i18nService.getMessage("app.rest.permission.no.users");
            throw new BadRequestException(message);
        }

        final Set<IUser> users = new HashSet<>(usernames.size());
        final List<String> missing = new ArrayList<>();
        for (final String username : usernames) {
            try {
                users.add(validateUser(username, returnDeleted));
            } catch (final NotFoundException e) {
                missing.add(username);
            }
        }

        if (!missing.isEmpty()) {
            final String message = i18nService.getMessage("app.rest.permission.nosuchusers",
                StringUtils.join(missing, ", "));
            throw new NotFoundException(message);
        }

        return ImmutableSet.copyOf(users);
    }

}
