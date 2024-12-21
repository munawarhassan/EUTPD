package com.pmi.tpd.euceg.core.util.validation;

import java.util.Collection;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * ValidationFailure implementation that described a failure of a single named property of a Java Bean object.
 *
 * @since 1.0
 * @author Christophe Friederich
 */
public class BeanValidationFailure extends SimpleValidationFailure {

    /**
     *
     */
    private static final long serialVersionUID = 2500734679142455099L;

    /** */
    protected String property;

    private static String validationMessage(final String attribute, final String message) {
        final StringBuilder buffer = new StringBuilder(message.length() + attribute.length() + 5);
        buffer.append('\"').append(attribute).append("\" ").append(message);
        return buffer.toString();
    }

    /**
     * Returns a ValidationFailure if a collection attribute of an object is null or empty.
     *
     * @param bean
     *            a {@link java.lang.Object} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.util.Collection} object.
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationFailure} object.
     */
    public static ValidationFailure validateNotEmpty(final Object bean,
        final String attribute,
        final Collection<?> value) {

        if (value == null) {
            return new BeanValidationFailure(bean, attribute, validationMessage(attribute, " is required."));
        }

        if (value.isEmpty()) {
            return new BeanValidationFailure(bean, attribute, validationMessage(attribute, " can not be empty."));
        }

        return null;
    }

    /**
     * <p>
     * validateMandatory.
     * </p>
     *
     * @param bean
     *            a {@link java.lang.Object} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.Object} object.
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationFailure} object.
     */
    public static ValidationFailure validateMandatory(final Object bean, final String attribute, final Object value) {

        if (value instanceof String) {
            return validateNotEmpty(bean, attribute, (String) value);
        }
        if (value instanceof Collection) {
            return validateNotEmpty(bean, attribute, (Collection<?>) value);
        }
        return validateNotNull(bean, attribute, value);
    }

    /**
     * <p>
     * validateMandatory.
     * </p>
     *
     * @param bean
     *            a {@link java.lang.Object} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationFailure} object.
     */
    public static ValidationFailure validateMandatory(final Object bean, final String attribute) {
        if (bean == null) {
            throw new NullPointerException("Null bean.");
        }

        try {
            final Object result = PropertyUtils.getProperty(bean, attribute);
            return validateMandatory(bean, attribute, result);
        } catch (final Exception ex) {
            throw new RuntimeException(
                    "Error validationg bean property: " + bean.getClass().getName() + "." + attribute, ex);
        }
    }

    /**
     * <p>
     * validateNotNull.
     * </p>
     *
     * @param bean
     *            a {@link java.lang.Object} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.Object} object.
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationFailure} object.
     */
    public static ValidationFailure validateNotNull(final Object bean, final String attribute, final Object value) {

        if (value == null) {
            return new BeanValidationFailure(bean, attribute, validationMessage(attribute, " is required."));
        }

        return null;
    }

    /**
     * A utility method that returns a ValidationFailure if a string is either null or has a length of zero; otherwise
     * returns null.
     *
     * @param bean
     *            a {@link java.lang.Object} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationFailure} object.
     */
    public static ValidationFailure validateNotEmpty(final Object bean, final String attribute, final String value) {

        if (value == null || value.length() == 0) {
            return new BeanValidationFailure(bean, attribute, validationMessage(attribute, " is a required field."));
        }
        return null;
    }

    /**
     * A utility method that checks that a given string is a valid Java full class name, returning a non-null
     * ValidationFailure if this is not so. Special case: primitive arrays like byte[] are also handled as a valid java
     * class name.
     *
     * @param bean
     *            a {@link java.lang.Object} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @param identifier
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.euceg.core.util.validation.ValidationFailure} object.
     */
    public static ValidationFailure validateJavaClassName(final Object bean, //
        final String attribute,
        String identifier) {

        final ValidationFailure emptyFailure = validateNotEmpty(bean, attribute, identifier);
        if (emptyFailure != null) {
            return emptyFailure;
        }

        char c = identifier.charAt(0);
        if (!Character.isJavaIdentifierStart(c)) {
            return new BeanValidationFailure(bean, attribute,
                    validationMessage(attribute, " starts with invalid character: " + c));
        }

        // handle arrays
        if (identifier.endsWith("[]")) {
            identifier = identifier.substring(0, identifier.length() - 2);
        }

        boolean wasDot = false;
        for (int i = 1; i < identifier.length(); i++) {
            c = identifier.charAt(i);

            if (c == '.') {
                if (wasDot || i + 1 == identifier.length()) {
                    return new BeanValidationFailure(bean, attribute,
                            validationMessage(attribute, " is not a valid Java Class Name: " + identifier));
                }

                wasDot = true;
                continue;
            }

            if (!Character.isJavaIdentifierPart(c)) {
                return new BeanValidationFailure(bean, attribute,
                        validationMessage(attribute, " contains invalid character: " + c));
            }

            wasDot = false;
        }

        return null;
    }

    /**
     * Creates new BeanValidationFailure.
     *
     * @param source
     *            a {@link java.lang.Object} object.
     * @param property
     *            a {@link java.lang.String} object.
     * @param error
     *            a {@link java.lang.Object} object.
     */
    public BeanValidationFailure(final Object source, final String property, final Object error) {
        super(source, error);

        if (source == null && property != null) {
            throw new IllegalArgumentException("ValidationFailure cannot have 'property' when 'source' is null.");
        }

        this.property = property;
    }

    /**
     * Returns a failed property of the failure source object.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProperty() {
        return property;
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
            final String property = getProperty();
            buffer.append(source.getClass().getName()).append('.').append(property == null ? "[General]" : property);
        }
        buffer.append(": ");
        buffer.append(getDescription());
        return buffer.toString();
    }
}
