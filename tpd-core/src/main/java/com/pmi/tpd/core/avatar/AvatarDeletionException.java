package com.pmi.tpd.core.avatar;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.4
 */
public class AvatarDeletionException extends AvatarException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public AvatarDeletionException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
