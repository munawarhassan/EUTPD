package com.pmi.tpd.core.avatar;

import java.util.Objects;

import com.pmi.tpd.api.user.avatar.AvatarSize;
import com.pmi.tpd.api.user.avatar.AvatarSourceType;

/**
 * @since 2.4
 */
public class AvatarRequest {

    public static AvatarRequest from(final INavBuilder navBuilder) {
        return new AvatarRequest(navBuilder.isSecure(), AvatarSize.Medium, false, AvatarSourceType.Gravatar);
    }

    public static AvatarRequest from(final INavBuilder navBuilder,
        final AvatarSize size,
        final AvatarSourceType source) {
        return new AvatarRequest(navBuilder.isSecure(), size, false, source);
    }

    /** */
    private final boolean secure;

    /** */
    private final AvatarSize size;

    /** */
    private final boolean useConfigured;

    /** */
    private final AvatarSourceType source;

    /**
     * {@code useConfigured} is defaulted to {@code false} and the source is {@code Disable}.
     *
     * @see #AvatarRequest(boolean, int, boolean)
     */
    public AvatarRequest(final boolean secure, final AvatarSize size) {
        this(secure, size, false, AvatarSourceType.Disable);
    }

    /**
     * Constructs a new {@code AvatarRequest} for the provided {@code scheme} and {@code size}.
     * <p>
     * The {@code secure} flag allows the caller to control whether avatar URLs use HTTP or HTTPS. The {@code size}
     * controls the dimensions of the avatar returned.
     *
     * @param secure
     *            {@code true} if avatar URLs should use HTTPS; otherwise, {@code false} for HTTP
     * @param size
     *            the height/width of the avatar being requested
     * @param useConfigured
     *            whether to use the configured base URL; if {@code false} the request context will be used instead
     * @param source
     *            the source used to.
     * @throws IllegalArgumentException
     *             if the provided {@code size} is less than 1.
     * @throws NullPointerException
     *             if the provided {@code scheme} is {@code null}.
     */
    public AvatarRequest(final boolean secure, final AvatarSize size, final boolean useConfigured,
            final AvatarSourceType source) {
        this.secure = secure;
        this.size = size;
        this.source = source;
        this.useConfigured = useConfigured;
    }

    /**
     * Retrieves the size desired for the avatar. Avatars are assumed to be square, so the size is used for both the
     * height and the width.
     *
     * @return the size
     */
    public AvatarSize getSize() {
        return size;
    }

    /**
     * Retrieves a flag indicating whether avatar URLs should use HTTPS. If the flag is {@code true}, the returned URLs
     * will use HTTPS; otherwise, they will use HTTP.
     *
     * @return the scheme
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Retrieve the URL using the configured base URL. This flag is <em>only</em> applicable to avatar sources which
     * serve from the current application. Avatars hosted remotely ignore this flag
     *
     * @return {@code true} if the URL should use the configured base URL. {@code false} otherwise
     */
    public boolean isUseConfigured() {
        return useConfigured;
    }

    /**
     * Gets the source to use.
     *
     * @return Returns {@link AvatarSourceType} representing the source to use.
     */
    public AvatarSourceType getSource() {
        return source;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AvatarRequest that = (AvatarRequest) o;

        return size == that.size && secure == that.secure && useConfigured == that.useConfigured
                && source == that.source;
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, size, secure, useConfigured);
    }
}
