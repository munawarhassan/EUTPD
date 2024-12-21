package com.pmi.tpd.core.euceg;

/**
 * This exceptions thrown during trying update of an attachment that it is sending.
 * 
 * @author Christophe Friederich
 * @since 1.1
 */
public class ConcurrencyAttachmentAccessException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -4240433396091649069L;

    public ConcurrencyAttachmentAccessException() {
    }

    public ConcurrencyAttachmentAccessException(final String message) {
        super(message);
    }

    public ConcurrencyAttachmentAccessException(final Throwable cause) {
        super(cause);
    }

    public ConcurrencyAttachmentAccessException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConcurrencyAttachmentAccessException(final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
