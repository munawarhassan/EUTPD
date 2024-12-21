package com.pmi.tpd.core.event.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.event.annotation.TransactionAware;
import com.pmi.tpd.core.security.provider.IDirectory;

/**
 * @author Christophe Friederich
 * @since 2.0
 */
@TransactionAware
public class GroupMembershipDeletedEvent extends BaseEvent {

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
    public GroupMembershipDeletedEvent(@Nonnull final Object source, @Nonnull final IDirectory directory,
            @Nonnull final String groupName) {
        super(source);
        this.directory = directory;
        this.groupName = groupName;
    }

    /**
     * @return Returns the directory where the group has been deleted.
     */
    public IDirectory getDirectory() {
        return directory;
    }

    /**
     * @return Returns the deleted group name
     */
    public String getGroupName() {
        return groupName;
    }

}
