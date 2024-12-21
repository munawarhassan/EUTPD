package com.pmi.tpd.core.maintenance;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.api.user.IUser;

/**
 * Thrown when an attempt is made to {@link IMaintenanceService#lock() lock} the system for maintenance when it is
 * already locked.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LockedMaintenanceException extends LockFailedMaintenanceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final IUser locker;

    /**
     * @param message
     * @param locker
     */
    public LockedMaintenanceException(@Nonnull final KeyedMessage message, @Nonnull final IUser locker) {
        super(message);

        this.locker = checkNotNull(locker, "locker");
    }

    @Nonnull
    public IUser getLocker() {
        return locker;
    }
}
