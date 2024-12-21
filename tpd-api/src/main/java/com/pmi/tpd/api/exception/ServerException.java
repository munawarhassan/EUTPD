package com.pmi.tpd.api.exception;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 */
public class ServerException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = -4963089357087149349L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public ServerException(final KeyedMessage message) {
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
    public ServerException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }
}
