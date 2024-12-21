package com.pmi.tpd.api.lifecycle.notification;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
@JsonSerialize
public class NotificationRequest {

    /**
     * @author Christophe Friederich
     */
    public enum Severity {
        /** success. */
        success,
        /** info. */
        info,
        /** warning. */
        warning,
        /** danger. */
        danger
    }

    /** */
    private final Severity severity;

    /** */
    private final String message;

    /** */
    private final long timeout;

    /**
     * @param severity
     * @param message
     * @param timeout
     */
    public NotificationRequest(@Nonnull final Severity severity, final String message, final long timeout) {
        this.severity = severity;
        this.message = message;
        if (timeout < 0) {
            this.timeout = 5000;
        } else {
            this.timeout = timeout;
        }
    }

    /**
     * @param severity
     * @param message
     */
    public NotificationRequest(final Severity severity, final String message) {
        this(severity, message, -1);

    }

    /**
     * @return
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return
     */
    public long getTimeout() {
        return timeout;
    }

}
