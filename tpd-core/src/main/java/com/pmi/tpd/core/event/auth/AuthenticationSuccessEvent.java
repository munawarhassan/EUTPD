package com.pmi.tpd.core.event.auth;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.api.event.annotation.EventName;

/**
 * Event that is raised when a user successfully authenticates.
 */
@AsynchronousPreferred
@EventName("app.user.authentication.succeeded")
public class AuthenticationSuccessEvent extends AbstractAuthenticationEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final Map<String, String> tokenDetails;

    public AuthenticationSuccessEvent(@Nonnull final Object source, @Nullable final String username,
            @Nonnull final String method) {
        this(source, username, method, null);
    }

    public AuthenticationSuccessEvent(@Nonnull final Object source, @Nullable final String username,
            @Nonnull final String method, @Nullable final Map<String, String> tokenDetails) {
        super(source, username, method);

        this.tokenDetails = tokenDetails;
    }

    /**
     * @return details describing the token that was used for authentication.
     */
    @Nullable
    public Map<String, String> getDetails() {
        return tokenDetails;
    }
}
