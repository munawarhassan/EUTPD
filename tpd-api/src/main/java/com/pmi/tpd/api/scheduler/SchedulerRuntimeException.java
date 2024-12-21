package com.pmi.tpd.api.scheduler;

/**
 * Indicates a problem interacting with the {@code SchedulerService}. This is an unchecked exception and is used to
 * report unexpected inconsistencies in the scheduler configuration.
 * 
 * @author Christophe Friederich
 * @since 1.3
 * @see SchedulerServiceException
 */
public class SchedulerRuntimeException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public SchedulerRuntimeException(final String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public SchedulerRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
