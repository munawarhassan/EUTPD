package com.pmi.tpd.core.audit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;

import com.google.common.collect.Sets;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * Handles the conversion of {@link Audited} events into {@link AuditEvent} events
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public class AuditedAnnotatedEventListener extends AbstractAuditEventPublisher {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditedAnnotatedEventListener.class);

    @Autowired
    public AuditedAnnotatedEventListener(@Nonnull final IEventPublisher eventPublisher,
            @Nonnull final IRequestManager requestManager, @Nullable final IAuthenticationContext authContext,
            @Nonnull final IAuditEntryLoggingService auditLoggingService) {
        super(auditLoggingService, requestManager, authContext, eventPublisher);
    }

    /**
     * Handle event annotated with {@link Audited}.
     *
     * @param event
     *            the event to handle
     * @throws Exception
     *             if errors
     */
    @EventListener
    public void onEvent(final Object event) throws Exception {
        final Audited auditAnnotated = AnnotationUtils.findAnnotation(event.getClass(), Audited.class);
        if (auditAnnotated != null) {
            if (event instanceof AuditEvent) {
                LOGGER.warn("Got @Audited annotated AuditEvent, ignoring to prevent looping {} {}",
                    event,
                    event.getClass());
                return;
            }

            IAuditEntry entry;
            try {
                @SuppressWarnings("unchecked")
                final AuditEntryConverter<Object> converter = (AuditEntryConverter<Object>) auditAnnotated.converter()
                        .getDeclaredConstructor()
                        .newInstance();
                final AuditEntryBuilder builder = new AuditEntryBuilder();
                setCommonFields(event, builder);

                entry = converter.convert(event, builder);
            } catch (final Exception ex) {
                throw logAndRethrow(event, ex);
            }
            publish(event, entry, Sets.newHashSet(auditAnnotated.channels()), auditAnnotated.priority());
        }
    }
}
