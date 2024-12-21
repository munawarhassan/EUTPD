package com.pmi.tpd.core.euceg;

/**
 * This exceptions thrown during trying update of an attachment that it is sending.
 * 
 * @author Christophe Friederich
 * @since 1.1
 */
public class AttachmentInvalidFilenaneException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -4240433396091649069L;

    public AttachmentInvalidFilenaneException() {
    }

    public AttachmentInvalidFilenaneException(final String message) {
        super(message);
    }

    public AttachmentInvalidFilenaneException(final Throwable cause) {
        super(cause);
    }

    public AttachmentInvalidFilenaneException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AttachmentInvalidFilenaneException(final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
