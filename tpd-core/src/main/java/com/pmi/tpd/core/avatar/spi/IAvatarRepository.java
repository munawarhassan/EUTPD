package com.pmi.tpd.core.avatar.spi;

import javax.annotation.Nonnull;

import com.pmi.tpd.core.avatar.IAvatarSupplier;
import com.pmi.tpd.core.avatar.ICacheableAvatarSupplier;

/**
 * @since 2.4
 */
public interface IAvatarRepository {

    /**
     * Deletes any stored avatars for the specified object and type. This removes the original as well as any scaled
     * copies that have been created based on requested sizes.
     *
     * @param type
     *            the type of object associated with the provided ID
     * @param id
     *            the object's ID
     */
    void delete(@Nonnull AvatarType type, @Nonnull Long id);

    /**
     * Retrieves a flag indicating whether an avatar has been stored for the specified object and type. When this method
     * returns {@code false} {@link #load(AvatarType, Object, int) loading} an avatar will return a generic default.
     *
     * @param type
     *            the type of object associated with the provided ID
     * @param id
     *            the object's ID
     * @return {@code true} if a local avatar exists for the specified object; otherwise, {@code false}
     */
    boolean isStored(@Nonnull AvatarType type, @Nonnull Long id);

    /**
     * Retrieves the current avatar for the specified object and type, or a default avatar if no avatar has been stored.
     *
     * @param type
     *            the type of object associated with the provided ID
     * @param id
     *            the object's ID
     * @param size
     *            the size to retrieve the avatar in
     * @return a supplier for accessing the requested avatar
     */
    @Nonnull
    ICacheableAvatarSupplier load(@Nonnull AvatarType type, @Nonnull Long id, int size);

    /**
     * Loads a default avatar for the specified type in an approximation of the requested size. Default avatars are only
     * available in a fixed set of sizes, and dynamic sizes are not created, so the request size is normalized into that
     * set.
     *
     * @param type
     *            the type of object to retrieve a default avatar for
     * @param size
     *            the size to retrieve the avatar in
     * @return a supplier for accessing the requested default avatar
     */
    @Nonnull
    ICacheableAvatarSupplier loadDefault(@Nonnull AvatarType type, int size);

    /**
     * Stores the provided avatar for the specified object and type.
     * <p>
     * Various restrictions are applied to avatars before they are stored:
     * <ul>
     * <li>The image format must be understood, and will be normalized to PNG</li>
     * <li>Pixel dimensions must fall within a system-configurable limit (1024px by default, 256px minimum)</li>
     * </ul>
     * <p>
     * The file size for the avatar image is <i>not</i> checked by the repository. That restriction is expected to be
     * applied at the system's edges (REST, SpringMVC, etc.).
     *
     * @param type
     *            the type of object associated with the provided ID
     * @param id
     *            the object's ID
     * @param supplier
     *            a supplier providing access to the avatar data
     */
    void store(@Nonnull AvatarType type, @Nonnull Long id, @Nonnull IAvatarSupplier supplier);

    /**
     * Retrieves a version identifier for the avatar with the specified object and type.
     *
     * @param type
     *            the type of the object to retrieve a version for
     * @param id
     *            the Object's ID
     * @return a version identifier for the avatar identified by the id and type, which will change only when the
     *         underlying avatar is modified
     */
    long getVersionId(@Nonnull AvatarType type, @Nonnull Long id);
}
