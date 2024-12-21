package com.pmi.tpd.web.context;

import javax.servlet.ServletContext;

import com.pmi.tpd.web.servlet.ServletContextProvider;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ContextAccessorFactory {

    private ContextAccessorFactory() {

    }

    /**
     * Create a specific context accessor.
     *
     * @param attributeName
     *            the name of the servlet context reference
     * @return Returns mew instance of {@link IContextAccessor}.
     * @param <T>
     *            the type associate to accessor.
     */
    public static <T> IContextAccessor<T> createContextAcessor(final String attributeName) {
        final ServletContext context = ServletContextProvider.getServletContext();
        if (context == null) {
            return new MemoryContextAccessor<>(attributeName);
        }
        return new ServletContextAccessor<>(attributeName);
    }
}
