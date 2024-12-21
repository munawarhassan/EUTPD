package com.pmi.tpd.api.lifecycle.config;

/**
 * <p>
 * Base event for Config's <em>legacy</em> events.
 * </p>
 * <p>
 * New events might not want to tie themselves to this event which sole purpose is to keep some semblance of backward
 * compatibility for those events that uses to extends Spring's {@code org.springframework.context.ApplicationEvent}
 * </p>
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
abstract class ConfigEvent implements ILifecycleEvent {

    /** */
    private final Object source;

    /** */
    private final long timestamp;

    /**
     * @param source
     */
    public ConfigEvent(final Object source) {
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }

    public Object getSource() {
        return source;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
