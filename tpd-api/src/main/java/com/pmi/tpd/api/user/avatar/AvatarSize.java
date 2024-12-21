package com.pmi.tpd.api.user.avatar;

/**
 * @author devacfr
 * @since 2.4
 */
public enum AvatarSize {

    ExtraLarge(256),
    Large(128),
    Medium(96),
    Small(64),
    ExtraSmall(48);

    private int width;

    private AvatarSize(final int width) {
        this.width = width;
    }

    public int width() {
        return width;
    }

    public static AvatarSize valueOf(final int width) {
        for (final AvatarSize size : AvatarSize.values()) {
            if (size.width == width) {
                return size;
            }
        }
        return AvatarSize.Medium;
    }
}