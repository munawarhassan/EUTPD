package com.pmi.tpd.testing.hamcrest;

import static com.pmi.tpd.testing.hamcrest.UserMatchers.userWithId;
import static com.pmi.tpd.testing.hamcrest.UserMatchers.userWithName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.security.permission.IPermittedUser;
import com.pmi.tpd.security.permission.Permission;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class PermittedUserMatchers {

  @Nonnull
  public static Matcher<IPermittedUser> permittedUser(final long expectedUserId,
      @Nullable final Permission expectedPermission) {
    return allOf(withUserId(expectedUserId), withPermission(expectedPermission));
  }

  @Nonnull
  public static Matcher<IPermittedUser> permittedUser(@Nullable final String expectedUsername,
      @Nullable final Permission expectedPermission) {
    return allOf(withUsername(expectedUsername), withPermission(expectedPermission));
  }

  @Nonnull
  public static Matcher<IPermittedUser> withUserId(@Nullable final Long expectedUserId) {
    return withUserThat(userWithId(expectedUserId));
  }

  @Nonnull
  public static Matcher<IPermittedUser> withUsername(@Nullable final String expectedUsername) {
    return withUserThat(userWithName(expectedUsername));
  }

  @Nonnull
  public static Matcher<IPermittedUser> withUserThat(@Nonnull final Matcher<IUser> userMatcher) {
    return new FeatureMatcher<IPermittedUser, IUser>(userMatcher, "user that", "user") {

      @Override
      protected IUser featureValueOf(final IPermittedUser actual) {
        return actual.getUser();
      }
    };
  }

  @Nonnull
  public static Matcher<IPermittedUser> withPermission(@Nullable final Permission expectedPermission) {
    return withPermissionThat(is(expectedPermission));
  }

  @Nonnull
  public static Matcher<IPermittedUser> withPermissionThat(@Nonnull final Matcher<Permission> permissionMatcher) {
    return new FeatureMatcher<IPermittedUser, Permission>(permissionMatcher, "permission that", "permission") {

      @Override
      protected Permission featureValueOf(final IPermittedUser actual) {
        return actual.getPermission();
      }
    };
  }
}
