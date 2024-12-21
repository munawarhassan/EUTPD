package com.pmi.tpd.core.server;

/**
 * The current state of the server. Possible state transitions are:
 * <ul>
 * <li>STARTING -> (FIRST_RUN | RUNNING | ERROR | STOPPING)</li>
 * <li>FIRST_RUN -> (RUNNING | MAINTENANCE | ERROR | STOPPING)</li>
 * <li>RUNNING -> (MAINTENANCE | ERROR | STOPPING)</li>
 * <li>ERROR -> (RUNNING | STOPPING)</li>
 * <li>MAINTENANCE -> (RUNNING | ERROR | STOPPING)</li>
 * <li>STOPPING -></li>
 * </ul>
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public enum ApplicationState {

    /**
     * The application is starting up, but not yet available
     */
    STARTING,
    /**
     * The application is running for the first time and has not yet been configured. All requests to the web UI will be
     * redirected to the First Run Wizard.
     */
    FIRST_RUN,
    /**
     * The application has been setup and is running normally
     */
    RUNNING,
    /**
     * The application is currently not available because the application is under maintenance
     */
    MAINTENANCE,
    /**
     * The application is currently not available because of an error
     */
    ERROR,
    /**
     * The application is shutting down
     */
    STOPPING
}
