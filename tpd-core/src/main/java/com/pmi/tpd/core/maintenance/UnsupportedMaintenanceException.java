package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class UnsupportedMaintenanceException extends MaintenanceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public UnsupportedMaintenanceException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public UnsupportedMaintenanceException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
