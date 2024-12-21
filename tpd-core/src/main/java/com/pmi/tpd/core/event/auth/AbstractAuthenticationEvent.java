package com.pmi.tpd.core.event.auth;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.BaseEvent;

/**
 * Base class for constructing events that relate to authentication.
 */
public abstract class AbstractAuthenticationEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String authenticationMethod;

    /** */
    private final String username;

    protected AbstractAuthenticationEvent(@Nonnull final Object source, @Nullable final String username,
            @Nonnull final String method) {
        super(source);

        this.authenticationMethod = method;
        this.username = username;
    }

    @Nonnull
    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    @Nullable
    public String getUsername() {
        return username;
    }
}
