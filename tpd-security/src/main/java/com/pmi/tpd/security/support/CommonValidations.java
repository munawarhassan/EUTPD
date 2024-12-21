package com.pmi.tpd.security.support;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.api.util.Assert.isTrue;

import java.util.Collection;

import org.springframework.data.domain.Pageable;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.UserRequest;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public final class CommonValidations {

    /**/
    private static final Predicate<String> NOT_NULL_OR_EMPTY = s -> !Strings.isNullOrEmpty(s);

    private CommonValidations() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param permission
     */
    public static void validatePermission(final Permission permission) {
        checkNotNull(permission, "Permission is null");
    }

    /**
     * @param permission
     */
    public static void validateGrantablePermission(final Permission permission) {
        validatePermission(permission);
        isTrue(permission.isGrantable(), "Permission is not a grantable permission");
    }

    /**
     * @param permission
     */
    public static void validateGlobalPermission(final Permission permission) {
        validatePermission(permission);
        isTrue(permission.isGlobal(), "Permission is not a global permission");
    }

    /**
     * @param permission
     */
    public static void validateResourcePermission(final Permission permission) {
        validatePermission(permission);
        isTrue(permission.isResource(), "Permission is not a resource-based permission");
    }

    /**
     * @param user
     */
    public static void validateUser(final IUser user) {
        checkNotNull(user, "User is null");
    }

    /**
     * @param user
     */
    public static void validateUser(final UserRequest user) {
        checkNotNull(user, "User is null");
    }

    /**
     * @param username
     */
    public static void validateUser(final String username) {
        isTrue(NOT_NULL_OR_EMPTY.apply(username), "Username is empty");
    }

    /**
     * @param group
     */
    public static void validateGroup(final String group) {
        isTrue(NOT_NULL_OR_EMPTY.apply(group), "Group is empty");
    }

    /**
     * @param groups
     */
    public static void validateGroups(final Collection<String> groups) {
        isTrue(groups != null, "Groups is null");
        isTrue(Iterables.all(groups, NOT_NULL_OR_EMPTY), "Groups has empty groups");
    }

    /**
     * @param request
     */
    public static void validatePageRequest(final Pageable request) {
        checkNotNull(request, "PageRequest is null");
    }

}
