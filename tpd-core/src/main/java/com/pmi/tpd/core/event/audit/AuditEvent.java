package com.pmi.tpd.core.event.audit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;

/**
 * Represents an AuditEvent derived from an {@link Audited Audited} annotated event
 */
public class AuditEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final IAuditEntry entry;

    /** */
    private final Set<String> channels;

    /** */
    private final Priority priority;

    public AuditEvent(@Nonnull final Object source, @Nonnull final IAuditEntry entry,
            @Nonnull final Set<String> channels, final Priority priority) {
        super(checkNotNull(source));
        this.priority = priority;
        this.entry = checkNotNull(entry);
        this.channels = Collections.unmodifiableSet(checkNotNull(channels));
    }

    /**
     * @return the details of the original event in a standard audit format
     */
    @Nonnull
    public IAuditEntry getEntry() {
        return entry;
    }

    /**
     * @return the channels which the original annotated event indicated would be interested in auditing the original
     *         event
     */
    @Nonnull
    public Set<String> getChannels() {
        return channels;
    }

    /**
     * @return the priority of the event
     */
    @Nonnull
    public Priority getPriority() {
        return priority;
    }
}
