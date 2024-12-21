package com.pmi.tpd.core.audit;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;
import com.pmi.tpd.web.core.request.IRequestMetadata;

/**
 * Base class for classes which construct an publish {@link AuditEvent audit events}
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public abstract class AbstractAuditEventPublisher {

    /** */
    protected final IAuditEntryLoggingService auditLoggingService;

    /** */
    protected final IAuthenticationContext authContext;

    /** */
    protected final IEventPublisher eventPublisher;

    /** */
    protected final Logger log;

    /** */
    protected final IRequestManager requestManager;

    protected AbstractAuditEventPublisher(@Nonnull final IAuditEntryLoggingService auditLoggingService,
            @Nonnull final IRequestManager requestManager, @Nullable final IAuthenticationContext authContext,
            @Nonnull final IEventPublisher eventPublisher) {
        this.auditLoggingService = checkNotNull(auditLoggingService, "auditLoggingService");
        this.authContext = authContext;
        this.eventPublisher = checkNotNull(eventPublisher, "eventPublisher");
        this.requestManager = checkNotNull(requestManager, "requestManager");

        log = LoggerFactory.getLogger(getClass());
    }

    protected Optional<IUser> getCurrentUser() {
        return authContext != null ? authContext.getCurrentUser() : Optional.empty();
    }

    protected <T extends Exception> T logAndRethrow(final Object event, final T ex) throws T {
        final AuditEntryBuilder builder = new AuditEntryBuilder().action("Publish AuditEvent")
                .target(event.getClass().getSimpleName())
                .timestamp(new Date())
                .details(ImmutableMap.of("error", "Failed to publish @Audited annotated event"));

        setSourceIp(builder);
        auditLoggingService.log(builder.build());
        log.error("Failed to convert and publish @Audited annotated event {} of type {} due to {}. Note that "
                + "AuditEntryConverter require a default no-args constructor.",
            event,
            event.getClass(),
            ex);
        throw ex;
    }

    protected void publish(final Object event, final IAuditEntry auditEntry, final Priority priority) throws Exception {
        publish(event, auditEntry, Collections.<String> emptySet(), priority);
    }

    protected void publish(final Object event,
        final IAuditEntry auditEntry,
        final Set<String> channels,
        final Priority priority) throws Exception {
        try {
            eventPublisher.publish(new AuditEvent(this, auditEntry, channels, priority));
        } catch (final Exception ex) {
            auditLoggingService.log(auditEntry);
            throw logAndRethrow(event, ex);
        }
    }

    protected AuditEntryBuilder setCommonFields(final Object event, final AuditEntryBuilder builder) {
        return setSourceIp(builder).action(event.getClass()).timestamp(new Date()).user(getCurrentUser().orElse(null));
    }

    protected AuditEntryBuilder setSourceIp(final AuditEntryBuilder builder) {
        final IRequestMetadata context = requestManager.getRequestMetadata();
        if (context != null) {
            builder.sourceIpAddress(context.getRemoteAddress());
        }
        return builder;
    }
}
