package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * <p>
 * UserEmailAlreadyExistsException class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class UserEmailAlreadyExistsException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -3969927798296335762L;

    /** */

    public UserEmailAlreadyExistsException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    public UserEmailAlreadyExistsException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
