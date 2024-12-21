package com.pmi.tpd.spring.context.bind;

import org.springframework.beans.NotWritablePropertyException;

/**
 * A custom {@link NotWritablePropertyException} that is thrown when a failure occurs during relaxed binding.
 *
 * @author Andy Wilkinson
 * @since 1.3.0
 * @see RelaxedDataBinder
 */
public class RelaxedBindingNotWritablePropertyException extends NotWritablePropertyException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String message;

    /** */
    private final PropertyOrigin propertyOrigin;

    RelaxedBindingNotWritablePropertyException(final NotWritablePropertyException ex,
            final PropertyOrigin propertyOrigin) {
        super(ex.getBeanClass(), ex.getPropertyName());
        this.propertyOrigin = propertyOrigin;
        this.message = "Failed to bind '" + propertyOrigin.getName() + "' from '" + propertyOrigin.getSource().getName()
                + "' to '" + ex.getPropertyName() + "' property on '" + ex.getBeanClass().getName() + "'";
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public PropertyOrigin getPropertyOrigin() {
        return this.propertyOrigin;
    }

}
