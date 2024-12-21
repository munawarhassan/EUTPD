package com.pmi.tpd.web.servlet;

import javax.servlet.ServletContext;

/**
 * A means to acquire the ServletContext.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ServletContextProvider {

    private ServletContextProvider() {
    }

    /**
     * Gets the current {@link ServletContext}.
     *
     * @return Returns {@code null} only if we are not running in a {@link ServletContext} or if the web app is not
     *         properly configured.
     */
    public static ServletContext getServletContext() {
        return ServletContextProviderListener.getServletContext();
    }
}
