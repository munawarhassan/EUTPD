package com.pmi.tpd.core;

import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;

import com.pmi.tpd.api.Environment;

/**
 * <p>
 * IGlobalApplicationProperties interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IGlobalApplicationProperties {

    /**
     * Get the base URL of the current application.
     *
     * @return the current application's base URL
     */
    String getBaseUrl();

    /**
     * <p>
     * getDisplayName.
     * </p>
     *
     * @return the displayable name of the application
     */
    String getDisplayName();

    /**
     * <p>
     * getVersion.
     * </p>
     *
     * @return the version of the application
     */
    String getVersion();

    /**
     * <p>
     * getBuildDate.
     * </p>
     *
     * @return the build date of the application
     */
    Date getBuildDate();

    /**
     * <p>
     * getBuildNumber.
     * </p>
     *
     * @return the build number of the application
     */
    String getBuildNumber();

    /**
     * <p>
     * getHomeDirectory.
     * </p>
     *
     * @return the home directory of the application or null if none is defined
     */
    Path getHomeDirectory();

    /**
     * <p>
     * getDefaultLocale.
     * </p>
     *
     * @return a {@link java.util.Locale} object.
     */
    Locale getDefaultLocale();

    /**
     * <p>
     * getEnvironment.
     * </p>
     *
     * @return a {@link com.pmi.tpd.api.Environment} object.
     */
    Environment getEnvironment();

}
