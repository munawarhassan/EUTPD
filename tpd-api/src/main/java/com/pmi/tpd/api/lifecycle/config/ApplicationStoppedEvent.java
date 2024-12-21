package com.pmi.tpd.api.lifecycle.config;

/**
 * Event produced when the application has shut down, and all the life-cycle plug-ins have had their shutdown methods
 * called.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class ApplicationStoppedEvent extends ConfigEvent {

    public ApplicationStoppedEvent(final Object object) {
        super(object);
    }
}
