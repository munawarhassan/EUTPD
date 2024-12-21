package com.pmi.tpd.core.event.user;

import static com.pmi.tpd.api.util.Assert.checkHasText;

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
import com.pmi.tpd.core.event.user.UserDeletedEvent.AuditDeletedUserConverter;
import com.pmi.tpd.core.security.provider.IDirectory;

/**
 * Event indicating a user has been deleted from user directory reference.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
@Audited(converter = AuditDeletedUserConverter.class, priority = Priority.HIGH,
        channels = { Channels.ADMIN_LOG, Channels.SECURITY })
public class UserDeletedEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = -7786623932129966551L;

    /** */
    private final String username;

    /** */
    private final IDirectory directory;

    /**
     * @param source
     *            The object on which the Event initially occurred.
     * @param directory
     *            the directory where the user has been deleted.
     * @param username
     *            the username of user to delete.
     */
    public UserDeletedEvent(@Nonnull final Object source, @Nonnull final String username,
            @Nullable final IDirectory directory) {
        super(source);
        this.directory = directory;
        this.username = checkHasText(username, "username");
    }

    /**
     * @return Returns the directory where the user has been deleted.
     */
    @Nullable
    public IDirectory getDirectory() {
        return directory;
    }

    /**
     * @return Returns a {@link String} representing the username of user to delete.
     */
    @Nonnull
    public String getUsername() {
        return username;
    }

    public static class AuditDeletedUserConverter implements AuditEntryConverter<UserDeletedEvent> {

        @Override
        public IAuditEntry convert(final UserDeletedEvent event, final AuditEntryBuilder builder) {
            final Map<String, String> details = Maps.newHashMap();
            details.put("user", event.username);
            if (event.directory != null) {
                details.put("directory", event.directory.getName());
            }
            return builder.details(details).build();
        }

    }

}
