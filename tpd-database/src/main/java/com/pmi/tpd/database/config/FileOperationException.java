package com.pmi.tpd.database.config;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Indicates that an attempt was made to access a resource (e.g. file), by creating it or otherwise opening a connect to
 * it, and that the attempt failed.
 */
public class FileOperationException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public FileOperationException(final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FileOperationException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }
}
