package com.pmi.tpd.web.context;

import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import com.pmi.tpd.web.servlet.ServletContextProvider;

/**
 * A support class to help access the Servlet context.
 *
 * @param <V>
 *            a object type.
 * @author Christophe Friederich
 * @since 1.0
 */
class ServletContextAccessor<V> implements IContextAccessor<V> {

    /** */
    private final String attributeName;

    ServletContextAccessor(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public V get() {
        V value = null;
        final ServletContext context = ServletContextProvider.getServletContext();
        if (context != null) {
            value = (V) context.getAttribute(attributeName);
        }
        return value;

    }

    @Override
    public void set(@Nullable final V value) {
        final ServletContext context = ServletContextProvider.getServletContext();
        if (context == null) {
            throw new IllegalStateException("The servlet context has not been initialised yet");
        }
        if (value == null) {
            context.removeAttribute(attributeName);
        } else {
            context.setAttribute(attributeName, value);
        }
    }

}
