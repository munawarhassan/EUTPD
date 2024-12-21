package com.pmi.tpd.core.event.user;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.Assert;

/**
 * Raised when a user is deleted from all user directories visible to the server.
 * <p>
 * Clients that store data used to <em>authenticate</em> or <em>authorize</em> a user should subscribe to this event to
 * clean up data related to the deleted user. For example, if a plugin stores a secure key used to authenticate a user,
 * it should listen for this event and delete those keys. Note that content created by the user typically shouldn't be
 * deleted when the user is deleted, as it may still be of historical interest.
 * </p>
 * 
 * @since 2.0
 */
public class UserCleanupEvent extends BaseEvent {

    /** */
    private static final long serialVersionUID = 2765410238775433600L;

    /** */
    private final IUser deletedUser;

    /**
     * Default constructor.
     *
     * @param source
     *            The object on which the Event initially occurred.
     * @param deletedUser
     *            the deleted user.
     */
    public UserCleanupEvent(@Nonnull final Object source, @Nonnull final IUser deletedUser) {
        super(source);
        this.deletedUser = Assert.checkNotNull(deletedUser, "deletedUser");
    }

    /**
     * Gets the deleted user.
     *
     * @return Returns a {@link IUser} representing the deleted user.
     */
    @Nonnull
    public IUser getDeletedUser() {
        return deletedUser;
    }

}
