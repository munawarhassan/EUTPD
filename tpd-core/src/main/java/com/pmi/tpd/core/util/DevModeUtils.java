package com.pmi.tpd.core.util;

import org.springframework.core.env.Environment;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public final class DevModeUtils {

    /** */
    public static final String DEV_MODE = "app.dev.mode";

    private DevModeUtils() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * @return true if {@code app.dev.mode} is enabled
     */
    public static boolean isEnabled() {
        return Boolean.getBoolean(DEV_MODE);
    }

    /**
     * @return true if {@code app.dev.mode} is enabled in spring environment.
     */
    public static boolean isEnabled(final Environment environment) {
        return environment.getProperty(DEV_MODE, Boolean.class, Boolean.FALSE) || isEnabled();
    }
}
