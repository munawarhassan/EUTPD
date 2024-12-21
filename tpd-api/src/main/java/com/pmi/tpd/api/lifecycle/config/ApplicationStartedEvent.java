package com.pmi.tpd.api.lifecycle.config;

/**
 * Event produced when the application has completed starting up, and all life-cycle event plug-ins have had their
 * start-up methods called successfully.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class ApplicationStartedEvent extends ConfigEvent {

    public ApplicationStartedEvent(final Object object) {
        super(object);
    }
}
