package com.pmi.tpd.euceg.core.excel;

/**
 * Thrown when a error of mapping occurs.
 * 
 * @author Christophe Friederich
 * @since 1.0
 */
public class ExcelMappingException extends RuntimeException {

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
     */
    public ExcelMappingException(final String message) {
        super(message);
    }

}
