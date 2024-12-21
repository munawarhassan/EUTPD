package com.pmi.tpd.core.audit;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.event.auth.AbstractAuthenticationEvent;
import com.pmi.tpd.core.event.auth.AuthenticationFailureEvent;
import com.pmi.tpd.core.event.auth.AuthenticationSuccessEvent;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * @author Christophe Friederich
 * @since 2.4
 */
public class AuthenticationEventListener extends AbstractAuditEventListener {

    /** */
    private final IUserService userService;

    @Autowired
    public AuthenticationEventListener(final IAuditEntryLoggingService auditLoggingService,
            final IRequestManager requestManager, final IAuthenticationContext authContext,
            final IEventPublisher eventPublisher, final IUserService userService) {
        super(auditLoggingService, requestManager, authContext, eventPublisher);
        this.userService = userService;
    }

    @EventListener
    public void onAuthenticationFailure(final AuthenticationFailureEvent event) throws Exception {
        final IAuditEntry auditEntry = createAuthenticationAuditEntryBuilder(event).details(toDetailsMap(event))
                .build();

        publish(event, auditEntry, Sets.newHashSet(Channels.AUTHENTICATION), Priority.HIGH);
    }

    @EventListener
    public void onAuthenticationSuccess(final AuthenticationSuccessEvent event) throws Exception {
        final IAuditEntry auditEntry = createAuthenticationAuditEntryBuilder(event).details(toDetailsMap(event))
                .build();

        publish(event, auditEntry, Sets.newHashSet(Channels.AUTHENTICATION), Priority.LOW);
    }

    private ImmutableMap.Builder<String, String> createDetailsBuilder(final AbstractAuthenticationEvent event) {
        return new ImmutableMap.Builder<String, String>().put("authentication-method", event.getAuthenticationMethod());
    }

    private AuditEntryBuilder createAuthenticationAuditEntryBuilder(final AbstractAuthenticationEvent event) {
        final AuditEntryBuilder builder = createAuditEntryBuilder(event);

        if (event.getUsername() == null) {
            builder.target("<anonymous>");
        } else {
            IUser user = event.getUser();
            if (user == null || !user.getName().equals(event.getUsername())) {
                user = userService.getUserByName(event.getUsername(), true);
            }
            builder.user(user);
            builder.target(event.getUsername());
        }
        return builder;
    }

    private Map<String, String> toDetailsMap(final AuthenticationFailureEvent event) throws IOException {
        final ImmutableMap.Builder<String, String> detailsBuilder = createDetailsBuilder(event);
        final Exception exception = event.getException();
        if (exception != null) {
            detailsBuilder.put("type", exception.getClass().getName());
            detailsBuilder.put("error", exception.getLocalizedMessage());
        }
        return detailsBuilder.build();
    }

    private Map<String, String> toDetailsMap(final AuthenticationSuccessEvent event) throws IOException {
        final ImmutableMap.Builder<String, String> detailsBuilder = createDetailsBuilder(event);
        return detailsBuilder.build();
    }

}
