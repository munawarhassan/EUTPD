package com.pmi.tpd.core.event.user;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.event.user.UserCreatedEvent.AuditCreatedUserConverter;

/**
 * Raised when a user is created from all user directories visible to the server.
 *
 * @since 2.0
 */
@Audited(converter = AuditCreatedUserConverter.class, priority = Priority.HIGH,
        channels = { Channels.ADMIN_LOG, Channels.SECURITY })
public class UserCreatedEvent extends BaseEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final IUser deletedUser;

    /**
     * Default constructor.
     *
     * @param source
     *            The object on which the Event initially occurred.
     * @param createdUser
     *            the created user.
     */
    public UserCreatedEvent(@Nonnull final Object source, @Nonnull final IUser createdUser) {
        super(source);
        this.deletedUser = Assert.checkNotNull(createdUser, "createdUser");
    }

    /**
     * Gets the created user.
     *
     * @return Returns a {@link IUser} representing the created user.
     */
    @Nonnull
    public IUser getCreatedUser() {
        return deletedUser;
    }

    public static class AuditCreatedUserConverter implements AuditEntryConverter<UserCreatedEvent> {

        @Override
        public IAuditEntry convert(final UserCreatedEvent event, final AuditEntryBuilder builder) {
            return builder.details(ImmutableMap.of("user", event.deletedUser.getName())).build();
        }

    }

}
