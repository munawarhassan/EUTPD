package com.pmi.tpd.core.avatar;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.4
 */
public class NoSuchAvatarException extends NoSuchEntityException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String id;

    public NoSuchAvatarException(@Nonnull final KeyedMessage message, @Nonnull final String id) {
        super(message);

        this.id = checkNotNull(id, "id");
    }

    @Nonnull
    public String getId() {
        return id;
    }
}
