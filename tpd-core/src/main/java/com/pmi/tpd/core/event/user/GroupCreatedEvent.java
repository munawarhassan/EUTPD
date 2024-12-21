package com.pmi.tpd.core.event.user;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.core.event.user.GroupCreatedEvent.AuditCreatedGroupConverter;

/**
 * Raised when a group is created from all user directories visible to the server.
 *
 * @since 2.4
 */
@Audited(converter = AuditCreatedGroupConverter.class, priority = Priority.HIGH,
        channels = { Channels.ADMIN_LOG, Channels.SECURITY })
public class GroupCreatedEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String groupName;

    /**
     * @param source
     * @param group
     */
    public GroupCreatedEvent(@Nonnull final Object source, @Nonnull final String groupName) {
        super(source);
        this.groupName = checkNotNull(groupName, "groupName");
    }

    /**
     * @return Returns name of group.
     */
    @Nonnull
    public String getGroup() {
        return groupName;
    }

    public static class AuditCreatedGroupConverter implements AuditEntryConverter<GroupCreatedEvent> {

        @Override
        public IAuditEntry convert(final GroupCreatedEvent event, final AuditEntryBuilder builder) {
            return builder.details(ImmutableMap.of("group", event.groupName)).build();
        }

    }

}
