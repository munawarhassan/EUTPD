package com.pmi.tpd.core.event.auth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.api.event.annotation.EventName;

/**
 * Event that is raised when an attempt to authenticate fails.
 */
@AsynchronousPreferred
@EventName("app.user.authentication.failed")
public class AuthenticationFailureEvent extends AbstractAuthenticationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Exception exception;

    public AuthenticationFailureEvent(@Nonnull final Object source, @Nullable final String username,
            @Nonnull final String method, @Nullable final Exception exception) {
        super(source, username, method);

        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}