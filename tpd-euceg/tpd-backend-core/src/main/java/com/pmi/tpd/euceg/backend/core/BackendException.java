package com.pmi.tpd.euceg.backend.core;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class BackendException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *            the detail message.
     */
    public BackendException(final String message) {
        super(message);
    }

    /**
     * @param cause
     *            the cause
     */
    public BackendException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     *            the detail message.
     * @param cause
     *            the cause
     */
    public BackendException(final String message, final Throwable cause) {
        super(message, cause);

    }

}
