package com.pmi.tpd.core.exception;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @since 2.0
 */
public class SecurityException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -3858525073104495399L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public SecurityException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public SecurityException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
