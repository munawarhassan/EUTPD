package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Exception thrown when an operation on a data store, such as the database or disk, fails.
 */
public class DataStoreException extends ServiceException {

    private static final long serialVersionUID = -4664705833819381570L;

    /**
     * @param message
     *            a message describing the failure
     */
    public DataStoreException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     *            a message describing the failure
     * @param cause
     *            the cause of the failure, which may be {@code null} for top-level failures
     */
    public DataStoreException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
