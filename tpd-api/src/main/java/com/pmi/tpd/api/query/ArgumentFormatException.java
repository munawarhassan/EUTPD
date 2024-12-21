package com.pmi.tpd.api.query;

/**
 * Indicate that argument is not in suitable format required by entity's property, i.e. is not parseable to the
 * specified type.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class ArgumentFormatException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String argument;

    /** */
    private final Class<?> propertyType;

    /**
     * Construct an <tt>ArgumentFormatException</tt> with specified argument and property type.
     *
     * @param argument
     * @param propertyType
     */
    public ArgumentFormatException(final String argument, final Class<?> propertyType, final Throwable cause) {
        super("Cannot cast '" + argument + "' to type " + propertyType, cause);
        this.argument = argument;
        this.propertyType = propertyType;
    }

    public String getArgument() {
        return argument;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }
}
