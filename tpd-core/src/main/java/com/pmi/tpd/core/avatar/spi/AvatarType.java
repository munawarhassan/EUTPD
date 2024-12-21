package com.pmi.tpd.core.avatar.spi;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.net.MediaType;
import com.pmi.tpd.core.avatar.IAvatarSupplier;

/**
 * @since 2.4
 */
public enum AvatarType {

    /**
     * @since 2.4
     */
    USER(1, "users", MediaType.PNG) {

        /**
         * Retrieves a silhouette default image in the requested size.
         *
         * @param id
         *            ignored
         * @param size
         *            the size of the avatar being requested
         * @return a {@link IAvatarSupplier supplier} which will produce a silhouette default image
         * @throws IllegalArgumentException
         *             if the requested {@code size} is not available
         * @throws NullPointerException
         *             if the provided {@code id} is {@code null}
         */
        @Nonnull
        @Override
        protected String buildPath(@Nonnull final String id, final int size) {
            checkNotNull(id, "id");
            checkArgument(DEFAULT_SIZES.contains(size), "Default user avatars are not available in " + size + "px");

            return "avatars/user/" + size + ".png";
        }
    };

    /**
     * Defines the sizes in which default avatars are available.
     * <p>
     * Note: It is assumed sizes have been normalised prior to attempting to retrieve a default avatar. This set exists
     * purely to try and catch cases where developer error might allow through a request for a size which does not
     * exist.
     */
    static final Set<Integer> DEFAULT_SIZES = ImmutableSet.of(48, 64, 96, 128, 256);

    private final String contentType;

    private final String directoryName;

    private final int id;

    AvatarType(final int id, final String directoryName, final MediaType mediaType) {
        this.directoryName = checkNotNull(directoryName, "directoryName");
        this.id = id;

        contentType = mediaType.toString();
    }

    public static AvatarType fromId(final int id) {
        for (final AvatarType value : values()) {
            if (value.getId() == id) {
                return value;
            }
        }
        throw new IllegalArgumentException("No AvatarType is available for ID " + id);
    }

    /**
     * @return the content type for avatars of this type
     */
    @Nonnull
    public String getContentType() {
        return contentType;
    }

    /**
     * @return the directory on disk where avatars of this type are stored
     */
    @Nonnull
    public String getDirectoryName() {
        return directoryName;
    }

    /**
     * @return a fixed ID for this avatar type, used for serialization
     */
    public int getId() {
        return id;
    }

    /**
     * Retrieves a default avatar, potentially randomized based on the requested ID.
     *
     * @param id
     *            the entity ID for which a default avatar is being requested
     * @param size
     *            the size of the avatar being requested
     * @return a supplier for providing access to the selected default avatar
     */
    @Nonnull
    public IAvatarSupplier loadDefault(@Nonnull final String id, final int size) {
        return new ResourceAvatarSupplier(contentType, buildPath(id, size));
    }

    /**
     * Retrieves a fixed default avatar, agnostic of any form of identifier. This is primarily intended to simplify
     * retrieving a default avatar in anonymous or unauthorized contexts where a fixed avatar should be returned to
     * prevent leaking information about whether or not a given object exists.
     *
     * @param size
     *            the size of the avatar being requested
     * @return a supplier providing access to the default avatar
     */
    @Nonnull
    public IAvatarSupplier loadFixedDefault(final int size) {
        return loadDefault("unknown", size);
    }

    /**
     * Builds the resource path used to stream the requested default avatar.
     *
     * @param id
     *            the entity ID, potentially used to randomize the default avatar
     * @param size
     *            the size of the avatar being requested
     * @return the resource path to open to stream the requested default avatar
     */
    @Nonnull
    protected abstract String buildPath(@Nonnull String id, int size);
}
