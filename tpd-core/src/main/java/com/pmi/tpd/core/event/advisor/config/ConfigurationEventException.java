package com.pmi.tpd.core.event.advisor.config;

import com.pmi.tpd.core.event.advisor.EventApplicationException;

/**
 * Thrown if errors are encountered while parsing {@link IEventConfig}.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class ConfigurationEventException extends EventApplicationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code ConfigurationEventException} with the provided message.
     *
     * @param s
     *            the exception message
     */
    public ConfigurationEventException(final String s) {
        super(s);
    }

    /**
     * Constructs a new {@code ConfigurationEventException} with the provided message and cause.
     *
     * @param s
     *            the exception message
     * @param throwable
     *            the cause
     */
    public ConfigurationEventException(final String s, final Throwable throwable) {
        super(s, throwable);
    }
}
