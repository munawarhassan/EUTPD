package com.pmi.tpd.web.context;

/**
 * A boolean variable that is tied to the life of the current Restlet context.
 *
 * @since 1.0
 */
public class ContextBoolean {

    /** */
    private final IContextAccessor<Boolean> accessor;

    /**
     * Constructs a reference that has a false value.
     *
     * @param attributeName
     *            the name of the servlet context reference
     */
    public ContextBoolean(final String attributeName) {
        accessor = ContextAccessorFactory.createContextAcessor(attributeName);
    }

    /**
     * Sets the value of the servlet context boolean.
     *
     * @param value
     *            the new value of the boolean
     */
    public void set(final boolean value) {
        accessor.set(value);
    }

    /**
     * @return the current value of the servlet context boolean
     */
    public boolean get() {
        final Boolean value = accessor.get();
        return Boolean.TRUE.equals(value);
    }
}
