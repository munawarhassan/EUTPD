package com.pmi.tpd.core.event.user;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.core.event.user.GroupDeletedEvent.AuditDeletedGroupConverter;
import com.pmi.tpd.core.security.provider.IDirectory;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@Audited(converter = AuditDeletedGroupConverter.class, priority = Priority.HIGH,
        channels = { Channels.ADMIN_LOG, Channels.SECURITY })
public class GroupDeletedEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 484358215906131048L;

    /** */
    private final String groupName;

    /** */
    private final IDirectory directory;

    /**
     * @param source
     *            the instance source of this event;
     * @param directory
     *            the directory where the group has been deleted.
     * @param groupName
     *            the deleted group name
     */
    public GroupDeletedEvent(@Nonnull final Object source, @Nonnull final String groupName,
            @Nullable final IDirectory directory) {
        super(source);
        this.groupName = checkNotNull(groupName, "groupName");
        this.directory = directory;
    }

    /**
     * @return Returns the deleted group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @return Returns the directory where the group has been deleted.
     */
    @Nullable
    public IDirectory getDirectory() {
        return directory;
    }

    public static class AuditDeletedGroupConverter implements AuditEntryConverter<GroupDeletedEvent> {

        @Override
        public IAuditEntry convert(final GroupDeletedEvent event, final AuditEntryBuilder builder) {
            final Map<String, String> details = Maps.newHashMap();
            details.put("group", event.groupName);
            if (event.directory != null) {
                details.put("directory", event.directory.getName());
            }
            return builder.details(details).build();
        }

    }

}
