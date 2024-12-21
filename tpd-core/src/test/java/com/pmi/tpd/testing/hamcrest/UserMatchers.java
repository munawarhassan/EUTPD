package com.pmi.tpd.testing.hamcrest;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import com.pmi.tpd.api.user.IUser;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
public class UserMatchers {

  private UserMatchers() {
    throw new UnsupportedOperationException("Do not instantiate " + getClass().getSimpleName());
  }

  @Nonnull
  public static Matcher<IUser> userIs(final long expectedId, @Nullable final String expectedName) {
    return allOf(userWithId(expectedId), userWithName(expectedName));
  }

  @Nonnull
  public static Matcher<IUser> userWithId(@Nullable final Long expectedId) {
    return userWithIdThat(is(expectedId));
  }

  @Nonnull
  public static Matcher<IUser> userWithIdThat(@Nonnull final Matcher<Long> idMatcher) {
    checkNotNull(idMatcher, "idMatcher");

    return new FeatureMatcher<IUser, Long>(idMatcher, "ID that", "id") {

      @Override
      protected Long featureValueOf(final IUser actual) {
        return actual.getId();
      }
    };
  }

  @Nonnull
  public static Matcher<IUser> userWithName(@Nullable final String expectedName) {
    return userWithNameThat(is(expectedName));
  }

  @Nonnull
  public static Matcher<IUser> userWithNameThat(@Nonnull final Matcher<String> nameMatcher) {
    checkNotNull(nameMatcher, "nameMatcher");

    return new FeatureMatcher<IUser, String>(nameMatcher, "name that", "name") {

      @Override
      protected String featureValueOf(final IUser actual) {
        return actual.getName();
      }
    };
  }
}
