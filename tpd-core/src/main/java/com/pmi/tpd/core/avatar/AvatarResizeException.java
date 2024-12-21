package com.pmi.tpd.core.avatar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.4
 */
public class AvatarResizeException extends AvatarLoadException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AvatarResizeException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    public AvatarResizeException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
