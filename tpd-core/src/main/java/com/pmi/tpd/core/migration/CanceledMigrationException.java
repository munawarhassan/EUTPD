package com.pmi.tpd.core.migration;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.core.maintenance.IMaintenanceCanceled;

/**
 * Exception thrown to indicate that migration processing has been canceled by an administrator.
 * <p>
 * Prior to throwing this exception, the system should be restored to the state it was in prior to beginning the
 * migration process. The connected database should be the original, and the system configuration should contain the
 * configuration to connect to the original database.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class CanceledMigrationException extends MigrationException implements IMaintenanceCanceled {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new {@code CanceledMigrationException} and sets the message.
     * <p>
     * No constructor is provided which accepts a cause because this exception should only be thrown in response to an
     * administrator canceling the process, not because the migration process failed.
     *
     * @param message
     *            a localized message conveying that the process has been canceled
     */
    public CanceledMigrationException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
