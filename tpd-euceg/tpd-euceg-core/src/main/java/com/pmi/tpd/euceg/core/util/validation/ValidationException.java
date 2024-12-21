package com.pmi.tpd.euceg.core.util.validation;

/**
 * <p>
 * ValidationException class.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public class ValidationException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 2455617036982816433L;

    /** */
    private ValidationResult result;

    /**
     * <p>
     * Constructor for ValidationException.
     * </p>
     *
     * @param message
     *            a {@link java.lang.String} object.
     */
    public ValidationException(final String message) {
        super(message);
    }

    /**
     * <p>
     * Constructor for ValidationException.
     * </p>
     *
     * @param result
     *            a {@link com.pmi.tpd.euceg.core.util.validation.ValidationResult} object.
     */
    public ValidationException(final ValidationResult result) {
        this("Validation failures: " + result.toString(), result);
    }

    /**
     * <p>
     * Constructor for ValidationException.
     * </p>
     *
     * @param message
     *            a {@link java.lang.String} object.
     * @param result
     *            a {@link com.pmi.tpd.euceg.core.util.validation.ValidationResult} object.
     */
    public ValidationException(final String message, final ValidationResult result) {
        super(message);
        this.result = result;
    }

    /**
     * <p>
     * getValidationResult.
     * </p>
     *
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationResult} object.
     */
    public ValidationResult getValidationResult() {
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString() + System.getProperty("line.separator") + this.result;
    }
}
