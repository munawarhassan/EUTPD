package com.pmi.tpd.core.audit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.audit.AuditEvent;

/**
 * Sends {@link AuditEvent} events to the audit logging service
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public class AuditEventListener {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventListener.class);

    /** */
    private final IAuditEntryLoggingService service;

    /** */
    private Priority priorityToFilter;

    @Inject
    public AuditEventListener(@Nonnull final IAuditEntryLoggingService service, @Nullable final String priority) {
        this.service = Assert.checkNotNull(service, "service");
        this.setPriorityToLog(Strings.isEmpty(priority) ? Priority.HIGH.toString() : priority);
    }

    public AuditEventListener(@Nonnull final IAuditEntryLoggingService service) {
        this(service, null);
    }

    public void setPriorityToLog(@Nonnull final String priority) {
        try {
            priorityToFilter = Priority.valueOf(priority.toUpperCase());
        } catch (final IllegalArgumentException e) {
            priorityToFilter = Priority.HIGH;
            LOGGER.warn("The configured 'audit.highest.priority.to.log', '{}', is invalid; defaulting to '{}'.",
                priority,
                priorityToFilter);
        }
    }

    @EventListener
    public void onAuditEvent(@Nonnull final AuditEvent event) {
        if (priorityToFilter == Priority.NONE) {
            return;
        }
        if (event.getPriority().getWeight() >= priorityToFilter.getWeight()) {
            service.log(event.getEntry());
        }
    }
}
