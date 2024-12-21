package com.pmi.tpd.core.event.advisor.spring.lifecycle;

/**
 * Enumerates certain <i>key</i> lifecycle states. An application may have many more fine-grained states than this
 * limited set. These states are enumerated here specifically because they are relevant to components.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum LifecycleState {

    /**
     * The application has been created but has not attempted to {@link #STARTING start}. This is a placeholder state
     * used when no explicit state has been set.
     */
    CREATED,
    /**
     * An attempt to {@link #STARTING start} the application failed. Application components like filters and servlets
     * will likely never be available.
     */
    FAILED,
    /**
     * The application has started successfully. All expected components like filters and servlets should be available.
     */
    STARTED,
    /**
     * The application is starting. Components may progressively become available during this state, but delegating
     * placeholders (like {@link LifecycleDelegatingFilterProxy filter proxies}) should not attempt to bind to them yet.
     */
    STARTING
}
