package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;

/**
 * Provides a context for working with the {@link IMaintenanceService#lock() maintenance lock}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IMaintenanceLock {

    /**
     * Retrieves the maintenance owner, which is the user who acquired the lock and started maintenance processing.
     *
     * @return the maintenance's owner
     */
    @Nonnull
    IUser getOwner();

    /**
     * Retrieves the token which can be used to {@link #unlock(String) unlock} the system.
     *
     * @return the unlock token
     */
    @Nonnull
    String getUnlockToken();

    /**
     * Releases the maintenance lock, restoring access to the system.
     *
     * @param token
     *            the unlock token generated when the system was {@link IMaintenanceService#lock() locked}
     * @throws com.pmi.tpd.security.AuthorisationException
     *             if the caller is not a SYS_ADMIN
     */
    void unlock(@Nonnull String token);
}
