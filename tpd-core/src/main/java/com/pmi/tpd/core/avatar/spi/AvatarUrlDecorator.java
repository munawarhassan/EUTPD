package com.pmi.tpd.core.avatar.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.avatar.INavBuilder;

/**
 * A decorator which can be used to add or modify an avatar url before it gets built.
 * <p>
 * This is currently used for stored avatars only.
 *
 * @since 2.4
 */
public interface AvatarUrlDecorator {

    /**
     * Decorate the URL for a given {@link IUser}'s avatar.
     *
     * @param builder
     *            the {@link INavBuilder}'s to decorate
     * @param user
     *            the user for which the avatar url should be decorated
     */
    void decorate(@Nonnull INavBuilder.Builder<?> builder, @Nonnull IUser user);

    /**
     * Invalidate the cache record for a given {@link IUser}'s avatar.
     *
     * @param user
     *            the user for which the avatar record should be invalidated
     */
    void invalidate(@Nonnull IUser user);

}
