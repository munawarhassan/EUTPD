package com.pmi.tpd.core.security.audit;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.auth.AuthenticationFailureEvent;
import com.pmi.tpd.core.event.auth.AuthenticationSuccessEvent;

/**
 * <p>
 * AuthenticationAuditListener class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class AuthenticationAuditListener implements ApplicationListener<AbstractAuthenticationEvent> {

    /** */
    private final IEventPublisher publisher;

    /** */
    private final WebAuditListener webListener = maybeCreateWebListener();

    /**
     * <p>
     * Constructor for AuthenticationAuditListener.
     * </p>
     *
     * @param publisher
     *            a {@link com.pmi.tpd.api.event.publisher.IEventPublisher} object.
     */
    @Inject
    public AuthenticationAuditListener(final IEventPublisher publisher) {
        this.publisher = Assert.notNull(publisher);
    }

    private static WebAuditListener maybeCreateWebListener() {
        return new WebAuditListener();
    }

    /** {@inheritDoc} */
    @Override
    public void onApplicationEvent(final AbstractAuthenticationEvent event) {
        if (event instanceof AbstractAuthenticationFailureEvent) {
            onAuthenticationFailureEvent((AbstractAuthenticationFailureEvent) event);
        } else if (this.webListener != null && this.webListener.accepts(event)) {
            this.webListener.process(this, event);
        } else {
            onAuthenticationEvent(event);
        }
    }

    private void onAuthenticationFailureEvent(final AbstractAuthenticationFailureEvent event) {
        publisher.publish(new AuthenticationFailureEvent(this, event.getAuthentication().getName(),
                "AUTHENTICATION_FAILURE", event.getException()));
    }

    private void onAuthenticationEvent(final AbstractAuthenticationEvent event) {
        publisher.publish(new AuthenticationSuccessEvent(this, event.getAuthentication().getName(),
                "AUTHENTICATION_SUCCESS", toDetail(event)));
    }

    private void publish(final BaseEvent event) {
        this.publisher.publish(event);
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    private static class WebAuditListener {

        public void process(final AuthenticationAuditListener listener, final AbstractAuthenticationEvent input) {
            if (listener != null) {
                final AuthenticationSwitchUserEvent event = (AuthenticationSwitchUserEvent) input;
                listener.publish(new AuthenticationSuccessEvent(listener, event.getTargetUser().getUsername(),
                        "AUTHENTICATION_SUCCESS", toDetail(input)));
            }

        }

        public boolean accepts(final AbstractAuthenticationEvent event) {
            return event instanceof AuthenticationSwitchUserEvent;
        }

    }

    @VisibleForTesting
    private static Map<String, String> toDetail(final AbstractAuthenticationEvent event) {
        if (event.getAuthentication().getDetails() != null) {
            return Collections.emptyMap();
        }
        final Map<String, String> data = Maps.newHashMap();
        final Object detail = event.getAuthentication().getDetails();
        if (detail instanceof WebAuthenticationDetails) {
            final WebAuthenticationDetails authenticationDetails = (WebAuthenticationDetails) detail;
            data.put("remoteAddress", authenticationDetails.getRemoteAddress());
            data.put("sessionId", authenticationDetails.getSessionId());
        } else {
            data.put("details", detail.toString());
        }
        return data;
    }

}
