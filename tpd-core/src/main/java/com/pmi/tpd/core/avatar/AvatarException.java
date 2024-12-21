package com.pmi.tpd.core.avatar;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.4
 */
public abstract class AvatarException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected AvatarException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    protected AvatarException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
