package com.pmi.tpd.database;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.database.spi.IDatabaseValidator;

/**
 * Thrown when a database fails {@link IDatabaseValidator#validate(javax.sql.DataSource) validation}. The message will
 * detail what aspect of the target caused the validation failure.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class DatabaseValidationException extends ArgumentValidationException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new database validation exception with the specified i18n detail message.
     *
     * @param message
     *                the i18n detail message. The detail message is saved for later retrieval by the
     *                {@link #getMessage()} method.
     */
    public DatabaseValidationException(@Nonnull final KeyedMessage message) {
        super(message);
    }
}
