package com.pmi.tpd.api.lifecycle.config;

/**
 * Event produced when the application is about to shut down, before any shutdown life-cycle plug-ins are run.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class ApplicationStoppingEvent extends ConfigEvent {

    public ApplicationStoppingEvent(final Object object) {
        super(object);
    }
}
