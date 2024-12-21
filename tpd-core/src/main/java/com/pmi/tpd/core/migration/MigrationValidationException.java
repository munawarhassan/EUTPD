package com.pmi.tpd.core.migration;

import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * {@link MigrationException} specialisation for exceptions generated during validation of the configuration to be used
 * for database migration or setup.
 * <p>
 * This exception does not have a cause because the message itself should encapsulate the details of what went wrong.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class MigrationValidationException extends MigrationException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public MigrationValidationException(final KeyedMessage message) {
        super(message);
    }
}
