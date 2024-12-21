package com.pmi.tpd.euceg.core.util.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a generic validation failure that contains failed object and a message describing the failure.
 *
 * @since 1.0
 * @author devacfr
 */
public class SimpleValidationFailure implements ValidationFailure {

    /**
     *
     */
    private static final long serialVersionUID = 8464118115080226892L;

    /** */
    protected Object source;

    /** */
    protected Object error;

    /**
     * <p>
     * Constructor for SimpleValidationFailure.
     * </p>
     *
     * @param source
     *            a {@link java.lang.Object} object.
     * @param error
     *            a {@link java.lang.Object} object.
     */
    public SimpleValidationFailure(final Object source, final Object error) {
        this.source = source;
        this.error = error;
    }

    /**
     * {@inheritDoc} Returns the error converted to String.
     */
    @Override
    public String getDescription() {
        return String.valueOf(error);
    }

    /**
     * {@inheritDoc} Returns object that failed the validation.
     */
    @Override
    @JsonIgnore
    public Object getSource() {
        return source;
    }

    /** {@inheritDoc} */
    @Override
    @JsonIgnore
    public Object getError() {
        return error;
    }

    /**
     * {@inheritDoc} Returns a String representation of the failure.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();

        buffer.append("Validation failure for ");
        final Object source = getSource();

        if (source == null) {
            buffer.append("[General]");
        } else {
            final String sourceLabel = source instanceof String ? source.toString() : source.getClass().getName();
            buffer.append(sourceLabel);
        }
        buffer.append(": ");
        buffer.append(getDescription());
        return buffer.toString();
    }
}
