package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Thrown when an attempt to {@link IMaintenanceService#lock() lock} the system for maintenance fails.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LockFailedMaintenanceException extends MaintenanceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LockFailedMaintenanceException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
