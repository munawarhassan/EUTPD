package com.pmi.tpd.core.security.audit;

import org.springframework.context.ApplicationListener;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;

import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.auth.AuthenticationFailureEvent;

/**
 * <p>
 * AuthorizationAuditListener class.
 * </p>
 *
 * @author Christophe Friederich IContentTypeResolver
 */
public class AuthorizationAuditListener implements ApplicationListener<AbstractAuthorizationEvent> {

    /** */
    private final IEventPublisher publisher;

    /**
     * <p>
     * Constructor for AuthorizationAuditListener.
     * </p>
     *
     * @param publisher
     *            a {@link com.pmi.tpd.api.event.publisher.IEventPublisher} object.
     */
    public AuthorizationAuditListener(final IEventPublisher publisher) {
        this.publisher = Assert.notNull(publisher);
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(final AbstractAuthorizationEvent event) {
        if (event instanceof AuthenticationCredentialsNotFoundEvent) {
            onAuthenticationCredentialsNotFoundEvent((AuthenticationCredentialsNotFoundEvent) event);
        } else if (event instanceof AuthorizationFailureEvent) {
            onAuthorizationFailureEvent((AuthorizationFailureEvent) event);
        }
    }

    private void onAuthenticationCredentialsNotFoundEvent(final AuthenticationCredentialsNotFoundEvent event) {
        publisher.publish(new AuthenticationFailureEvent(this, "<unknown>", "AUTHENTICATION_FAILURE",
                event.getCredentialsNotFoundException()));
    }

    private void onAuthorizationFailureEvent(final AuthorizationFailureEvent event) {
        publisher.publish(new AuthenticationFailureEvent(this, event.getAuthentication().getName(),
                "AUTHENTICATION_FAILURE", event.getAccessDeniedException()));
    }

}
