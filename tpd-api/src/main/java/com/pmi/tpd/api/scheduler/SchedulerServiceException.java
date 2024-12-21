package com.pmi.tpd.api.scheduler;

/**
 * Indicates a problem interacting with the {@code SchedulerService}. This is a checked exception and is used to report
 * immediate failures, such as failed job registrations.
 * 
 * @author Christophe Friederich
 * @since 1.3
 * @see SchedulerRuntimeException
 */
public class SchedulerServiceException extends Exception {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public SchedulerServiceException(final String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public SchedulerServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
