package com.pmi.tpd.core.startup;

/**
 * Implementations of this interface will be able to find a configured app.home directory in one particular way.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IHomePathLocator {

    /**
     * Returns the home path configured via this locator method, or null if none is configured.
     *
     * @return the home path configured via this locator method, or null if none is configured.
     */
    String getHome();

    /**
     * Returns a user-friendly and readable name for this locator to make support's life easier.
     *
     * @return a user-friendly and readable name for this locator to make support's life easier
     */
    String getDisplayName();
}
