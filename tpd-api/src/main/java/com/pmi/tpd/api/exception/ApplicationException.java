package com.pmi.tpd.api.exception;

/**
 * <p>
 * ApplicationException class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class ApplicationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -796256332387682948L;

    /**
     * <p>
     * Constructor for ApplicationException.
     * </p>
     */
    public ApplicationException() {
        super();
    }

    /**
     * <p>
     * Constructor for ApplicationException.
     * </p>
     *
     * @param message
     *            a {@link java.lang.String} object.
     * @param cause
     *            a {@link java.lang.Throwable} object.
     */
    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * <p>
     * Constructor for ApplicationException.
     * </p>
     *
     * @param message
     *            a {@link java.lang.String} object.
     */
    public ApplicationException(final String message) {
        super(message);
    }

    /**
     * <p>
     * Constructor for ApplicationException.
     * </p>
     *
     * @param cause
     *            a {@link java.lang.Throwable} object.
     */
    public ApplicationException(final Throwable cause) {
        super(cause);

    }

}
