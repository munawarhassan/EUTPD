package com.pmi.tpd.testing.hamcrest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.google.common.base.Preconditions;
import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.core.model.user.GrantedPermission;

/**
 * @author Christophe Friederich
 * @since 2.0
 * @param <T>
 */
public class GrantedPermissionMatcher<T extends GrantedPermission> extends TypeSafeMatcher<T> {

    private final Permission permission;

    private final String userName;

    private final String groupName;

    public static <T extends GrantedPermission> Matcher<T> userPermission(final Permission perm,
        final String userName) {
        return new GrantedPermissionMatcher<>(perm, userName, null);
    }

    public static <T extends GrantedPermission> Matcher<T> groupPermission(final Permission perm,
        final String groupName) {
        return new GrantedPermissionMatcher<>(perm, null, groupName);
    }

    public GrantedPermissionMatcher(final Permission permission, final String userName, final String groupName) {
        Preconditions.checkArgument(userName != null || groupName != null);
        this.permission = permission;
        this.userName = userName;
        this.groupName = groupName;
    }

    @Override
    protected boolean matchesSafely(final GrantedPermission perm) {
        if (perm.getPermission() != permission) {
            return false;
        }
        if (userName != null && userName.equals(perm.getUser().getUsername())) {
            return perm.getGroup() == null;
        }

        if (groupName != null && groupName.equals(perm.getGroup())) {
            return perm.getUser() == null;
        }

        return false;
    }

    @Override
    public void describeTo(final Description description) {
        description.appendText("a ")
                .appendValue(permission)
                .appendText(" permission ")
                .appendText("for " + (userName != null ? "user " + userName : "group " + groupName));
    }

}
