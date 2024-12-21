package com.pmi.tpd.core.user.preference;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.user.IUser;

/**
 * A simple manager for retrieving, caching and updating user preferences objects.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IUserPreferencesManager {

    /**
     * <p>
     * getPreferences.
     * </p>
     *
     * @return The user preferences for a user, or null if the user is null
     * @param user
     *             a {@link com.pmi.tpd.api.user.IUser} object.
     */
    @Nonnull
    Optional<IPreferences> getPreferences(IUser user);

    /**
     * @param passwordResetTokenProperty
     * @param token
     * @return
     * @since 2.0
     */
    Optional<IUser> findUserByProperty(String property, String value);

    /**
     * <p>
     * getPreferences.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.core.user.preference.IPreferences} object, which may be empty but never
     *         {@code null}.
     */
    @Nonnull
    Optional<IPreferences> getPreferences(@Nullable String key);

    /**
     * Clear all cached preferences.
     */
    void clearCache();

    /**
     * Clear any cached preferences for a given user.
     *
     * @param user
     *             a {@link com.pmi.tpd.api.user.IUser} object.
     */
    void clearCache(IUser user);

}
