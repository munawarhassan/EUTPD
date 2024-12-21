package com.pmi.tpd.core.avatar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.4
 */
public class AvatarStoreException extends AvatarException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AvatarStoreException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    public AvatarStoreException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
