package com.pmi.tpd.testing.hamcrest;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import com.pmi.tpd.security.permission.Permission;
import com.pmi.tpd.security.permission.IPermittedGroup;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class PermittedGroupMatchers {

    @Nonnull
    public static Matcher<IPermittedGroup> permittedGroup(@Nullable final String expectedGroup,
        @Nullable final Permission expectedPermission) {
        return allOf(withGroup(expectedGroup), withPermission(expectedPermission));
    }

    @Nonnull
    public static Matcher<IPermittedGroup> withGroup(@Nullable final String expectedGroup) {
        return withGroupThat(is(expectedGroup));
    }

    @Nonnull
    public static Matcher<IPermittedGroup> withGroupThat(@Nonnull final Matcher<String> groupMatcher) {
        return new FeatureMatcher<IPermittedGroup, String>(groupMatcher, "group that", "group") {

            @Override
            protected String featureValueOf(final IPermittedGroup actual) {
                return actual.getGroup();
            }
        };
    }

    @Nonnull
    public static Matcher<IPermittedGroup> withPermission(@Nullable final Permission expectedPermission) {
        return withPermissionThat(is(expectedPermission));
    }

    @Nonnull
    public static Matcher<IPermittedGroup> withPermissionThat(@Nonnull final Matcher<Permission> permissionMatcher) {
        return new FeatureMatcher<IPermittedGroup, Permission>(permissionMatcher, "permission that", "permission") {

            @Override
            protected Permission featureValueOf(final IPermittedGroup actual) {
                return actual.getPermission();
            }
        };
    }
}
