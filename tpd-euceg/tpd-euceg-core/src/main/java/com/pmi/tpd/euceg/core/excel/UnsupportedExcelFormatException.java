package com.pmi.tpd.euceg.core.excel;

/**
 * Thrown when use a unsupported excel file.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class UnsupportedExcelFormatException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create a new excel mapping exception with the specified detail message.
     *
     * @param message
     *            the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *            method.
     * @param cause
     *            the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt>
     *            value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public UnsupportedExcelFormatException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
