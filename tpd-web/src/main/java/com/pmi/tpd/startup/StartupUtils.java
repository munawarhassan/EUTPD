package com.pmi.tpd.startup;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.springframework.core.Conventions;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class StartupUtils {

    /** */
    private static final String ATTR_INSTANCE = Conventions.getQualifiedAttributeName(IStartupManager.class,
        "instance");

    private StartupUtils() {
    }

    @Nonnull
    public static IStartupManager getStartupManager(@Nonnull final ServletContext servletContext) {
        return (IStartupManager) servletContext.getAttribute(ATTR_INSTANCE);
    }

    /**
     * @param servletContext
     * @param startupManager
     */
    public static void setStartupManager(@Nonnull final ServletContext servletContext,
        @Nonnull final IStartupManager startupManager) {
        servletContext.setAttribute(ATTR_INSTANCE, startupManager);
    }
}
