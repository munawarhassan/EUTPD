package com.pmi.tpd.core.event.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.util.Assert;

/**
 * Raised when a group is deleted from all user directories visible to the server.
 * <p>
 * Clients that store data used to <em>authorize</em> a group should subscribe to this event to clean up data related to
 * the deleted group. For example, if a plugin allocates resource permissions to groups, it should listen for this event
 * and delete those permissions.
 * </p>
 *
 * @since 2.0
 */
public class GroupCleanupEvent extends BaseEvent {

    /** */
    private static final long serialVersionUID = 684420508641933889L;

    /** */
    private final String group;

    /**
     * @param source
     * @param group
     */
    public GroupCleanupEvent(@Nonnull final Object source, @Nonnull final String group) {
        super(source);
        this.group = Assert.checkNotNull(group, "group");
    }

    /**
     * @return Returns name of group.
     */
    @Nonnull
    public String getGroup() {
        return group;
    }

}
