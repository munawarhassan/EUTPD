package com.pmi.tpd.core.avatar.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IPerson;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;
import com.pmi.tpd.core.avatar.AvatarRequest;
import com.pmi.tpd.core.avatar.IAvatarService;
import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;

/**
 * Non-public extensions to the {@link IAvatarService} which are not intended to be available.
 *
 * @since 2.4
 */
public interface IInternalAvatarService extends IAvatarService {

    /**
     * Creates an {@link IAvatarSupplier} from the provided data URI.
     *
     * @param uri
     *            the data URI containing the avatar's Base64-encoded image
     * @return an {@link IAvatarSupplier} which can be used to read in the image from the provided data {@code uri}
     */
    @Nonnull
    IAvatarSupplier createSupplierFromDataUri(@Nonnull String uri);

    /**
     * Delete the avatar associated with the specified {@link ApplicationUser}.
     *
     * @param user
     *            the user whose avatar is being removed
     * @see #saveForUser(ApplicationUser, IAvatarSupplier)
     */
    void deleteForUser(@Nonnull IUser user);

    /**
     * Retrieves the current avatar for the specified {@link ApplicationUser user}. If no explicit avatar has been set,
     * a default avatar will be returned.
     *
     * @param user
     *            the user to retrieve the avatar for
     * @param size
     *            the size to retrieve the avatar in
     * @return an {@link IAvatarSupplier} which can be used to read the avatar
     */
    @Nonnull
    ICacheableAvatarSupplier getForUser(@Nonnull IUser user, int size);

    /**
     * Retrieves a stable default user avatar, suitable for display in anonymous contexts.
     *
     * @param size
     *            the size to retrieve the avatar in
     * @return an {@link IAvatarSupplier} which can be used to read the avatar
     */
    @Nonnull
    ICacheableAvatarSupplier getUserDefault(int size);

    /**
     * Retrieves a flag indicating whether per-user avatars are enabled. When disabled, default avatar icons are shown
     * for all users. When enabled, per-user avatars are displayed if set; otherwise, a default avatar is displayed.
     *
     * @return {@code true} if avatars are enabled for {@link IPerson people}
     * @see IAvatarService#getUrlForPerson(IPerson, AvatarRequest)
     * @see #setEnabled(boolean)
     */
    boolean isEnabled();

    /**
     * Gets the default avartar source to use.
     *
     * @return Returns {@link AvatarSourceType} representing the default used avatar source.
     */
    @Nonnull
    AvatarSourceType getDefaultSource();

    /**
     * Sets the default avartar source to use.
     *
     * @param defaultSource
     *            the avatar source
     */
    void setDefaultSource(@Nonnull AvatarSourceType defaultSource);

    /**
     * Checks whether the user has an avatar stored locally.
     *
     * @return {@code true} if the user's avatar is stored locally.
     */
    boolean isLocalForUser(@Nonnull IUser user);

    /**
     * Saves the avatar contained in the provided {@link IAvatarSupplier supplier} as the new avatar for the specified
     * {@link ApplicationUser}, <i>removing</i> any previously-stored avatar.
     * <p>
     * Previous avatars <i>are not maintained</i>. The only way to restore a previous avatar is to save it again.
     *
     * @param user
     *            the user whose avatar is being set
     * @param supplier
     *            a supplier containing the avatar data to store
     */
    void saveForUser(@Nonnull IUser user, @Nonnull IAvatarSupplier supplier);

    /**
     * Sets a flag which controls whether per-user avatar URLs are enabled. When disabled, default avatar icons are
     * shown for all users. When enabled, per-user avatars are displayed if set; otherwise, a default avatar is
     * displayed.
     * <p>
     * Note: This flag does <i>not</i> control whether avatars are <i>displayed</i>--only whether default avatars are
     * always used.
     *
     * @param enabled
     *            {@code true} if per-user avatars should be used; otherwise, {@code false} to always use default
     *            avatars for all users
     */
    void setEnabled(boolean enabled);

}
