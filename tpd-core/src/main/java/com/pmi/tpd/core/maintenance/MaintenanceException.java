package com.pmi.tpd.core.maintenance;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Base class for exceptions thrown by the {@link MaintenanceException}.
 * <p>
 * Note: This base should <i>not</i> be used for exceptions thrown by {@link MaintenanceException tasks}.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public abstract class MaintenanceException extends ServiceException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    protected MaintenanceException(@Nonnull final KeyedMessage message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    protected MaintenanceException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
        super(message, cause);
    }
}
