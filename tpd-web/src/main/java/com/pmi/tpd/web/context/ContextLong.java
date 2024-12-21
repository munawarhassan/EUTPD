package com.pmi.tpd.web.context;

/**
 * A long variable that is tied to the life of the current Restlet context.
 *
 * @since 1.0
 */
public class ContextLong implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6148959976177292940L;

    /** */
    private final IContextAccessor<Long> accessor;

    /**
     * Constructs a reference that has a 0 value.
     *
     * @param attributeName
     *            the name of the servlet context reference
     */
    public ContextLong(final String attributeName) {
        accessor = ContextAccessorFactory.createContextAcessor(attributeName);
    }

    /**
     * Sets the value of the servlet context long.
     *
     * @param value
     *            the new value of the long
     */
    public void set(final long value) {
        accessor.set(value);
    }

    /**
     * @return the current value of the servlet context long or 0 if its never been set.
     */
    public long get() {
        final Long value = accessor.get();
        return value == null ? 0L : value;
    }
}
