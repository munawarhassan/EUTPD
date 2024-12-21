package com.pmi.tpd.core.exception;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Indicates the named user does not exist
 *
 * @since 2.0
 */
public class NoSuchUserException extends NoSuchEntityException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String username;

    public NoSuchUserException(@Nonnull final KeyedMessage message, @Nonnull final String username) {
        super(message);

        this.username = checkNotNull(username, "username");
    }

    @Nonnull
    public String getUsername() {
        return username;
    }
}
