package com.pmi.tpd.euceg.api;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Specific business exception for errors occur in <b>euceg</b> namespace.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class EucegException extends ServiceException {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EucegException(final KeyedMessage message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     *            the i18n detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     */
    public EucegException(final KeyedMessage message) {
        super(message);
    }

}
