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
import com.pmi.tpd.core.event.user.GroupMemberRemovedEvent.AuditGroupMemberRemovedConverter;

/**
 * Raised when an user is removed to group.
 *
 * @since 2.4
 */
@Audited(converter = AuditGroupMemberRemovedConverter.class, priority = Priority.HIGH,
        channels = { Channels.ADMIN_LOG, Channels.SECURITY })
public class GroupMemberRemovedEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final String username;

    /** */
    private final String groupName;

    /**
     * @param source
     * @param group
     */
    public GroupMemberRemovedEvent(@Nonnull final Object source, @Nonnull final String username,
            @Nonnull final String groupName) {
        super(source);
        this.username = checkNotNull(username, "username");
        this.groupName = checkNotNull(groupName, "groupName");
    }

    public String getUsername() {
        return username;
    }

    /**
     * @return Returns name of group.
     */
    @Nonnull
    public String getGroup() {
        return groupName;
    }

    public static class AuditGroupMemberRemovedConverter implements AuditEntryConverter<GroupMemberRemovedEvent> {

        @Override
        public IAuditEntry convert(final GroupMemberRemovedEvent event, final AuditEntryBuilder builder) {
            return builder.details(ImmutableMap.of("username", event.username, "group", event.groupName)).build();
        }

    }

}
