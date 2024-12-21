package com.pmi.tpd.core.avatar;

/**
 * Augments the {@link IAvatarSupplier} with timestamp information that can be used to control caching, or to simply
 * display a modification date indicating when the avatar was last updated.
 * 
 * @since 2.4
 */
public interface ICacheableAvatarSupplier extends IAvatarSupplier {

    /**
     * Marker value to be returned by {@link #getTimestamp()} for avatars which cannot be modified.
     */
    int TIMESTAMP_ETERNAL = -1;

    /**
     * Marker value to be returned by {@link #getTimestamp()} for avatars whose modification timestamp cannot be
     * determined by the system.
     */
    int TIMESTAMP_UNKNOWN = 0;

    /**
     * The timestamp at which the avatar was last modified, <i>in milliseconds</i>.
     * <p>
     * To account for differences in avatar repository implementations:
     * <ul>
     * <li>If timestamp information is <i>not available</i>, implementations shall return {@link #TIMESTAMP_UNKNOWN}.
     * Avatars without timestamps should not be cached, as it may not be possible to reliably detect updates.</li>
     * <li>If the avatar <i>cannot be updated</i>, implementations shall return {@link #TIMESTAMP_ETERNAL}. Such avatars
     * may be cached more aggressively, as they cannot change.</li>
     * </ul>
     *
     * @return the avatar's modification timestamp <i>in milliseconds</i>, or {@link #TIMESTAMP_UNKNOWN} if no
     *         modification date is available, or {@link #TIMESTAMP_ETERNAL} if the avatar is unmodifiable
     */
    long getTimestamp();
}
