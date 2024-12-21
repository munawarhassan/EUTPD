package com.pmi.tpd.api.user;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.util.Assert;

/**
 * This file was directly copied from SAL Represents an identifier that uniquely
 * identifies a user for the duration of
 * its existence.
 */
public final class UserKey implements Serializable {

  /**
   * Builds a {@link UserKey} object from a long id. The id will be converted to a
   * String first.
   *
   * @param userId
   *               a user ID
   * @return a {@link UserKey} object
   */
  @Nonnull
  public static UserKey fromLong(final long userId) {
    return new UserKey(String.valueOf(userId));
  }

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final String userkey;

  /**
   * Default constructor: builds a UserKey from its string representation.
   *
   * @param userkey
   *                the string representation of a UserKey. Must not be null.
   */
  public UserKey(@Nonnull final String userkey) {
    this.userkey = Assert.checkNotNull(userkey, "userkey");
  }

  /**
   * Returns a string representation of the current key.
   *
   * @return a string representation of the current key
   */
  @Nonnull
  public String getStringValue() {
    return userkey;
  }

  @Override
  @Nonnull
  public String toString() {
    return getStringValue();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final UserKey userKey = (UserKey) o;

    if (userkey != null ? !userkey.equals(userKey.userkey) : userKey.userkey != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return userkey != null ? userkey.hashCode() : 0;
  }

}
