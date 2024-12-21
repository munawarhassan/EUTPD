package com.pmi.tpd.core.event.advisor;

/**
 * Base class for all exceptions thrown by event application.
 * <p/>
 * Note: This base class explicitly disallows the construction of exceptions without a message, a throwable or both.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class EventApplicationException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code EventApplicationException} with the provided message.
     *
     * @param s
     *            the exception message
     */
    public EventApplicationException(final String s) {
        super(s);
    }

    /**
     * Constructs a new {@code EventApplicationException} with the provided message and cause.
     *
     * @param s
     *            the exception message
     * @param throwable
     *            the cause
     */
    public EventApplicationException(final String s, final Throwable throwable) {
        super(s, throwable);
    }

    /**
     * Constructs a new {@code EventApplicationException} the the provided cause and no message.
     *
     * @param throwable
     *            the cause
     */
    public EventApplicationException(final Throwable throwable) {
        super(throwable);
    }
}
