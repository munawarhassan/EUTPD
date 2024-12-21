package com.pmi.tpd.web.context;

/**
 * A servlet context reference is a variable that is tied to the life of the current servlet context.
 *
 * @param <V>
 *            object type of variable.
 * @author Christophe Friederich
 * @since 1.0
 */
public class ContextReference<V> implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4550054609544629760L;

    /**
     *
     */
    private final IContextAccessor<V> accessor;

    /**
     * Constructs a reference that has a null value.
     *
     * @param attributeName
     *            the name of the servlet context reference
     */
    public ContextReference(final String attributeName) {
        accessor = ContextAccessorFactory.createContextAcessor(attributeName);
    }

    /**
     * Sets the value of the servlet context reference.
     *
     * @param value
     *            the new value of the reference
     */
    public void set(final V value) {
        accessor.set(value);
    }

    /**
     * @return the current value of the servlet context reference
     */
    public V get() {
        return accessor.get();
    }
}
