package com.pmi.tpd.core.avatar;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.4
 */
public class UnsupportedAvatarException extends AvatarStoreException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public UnsupportedAvatarException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
