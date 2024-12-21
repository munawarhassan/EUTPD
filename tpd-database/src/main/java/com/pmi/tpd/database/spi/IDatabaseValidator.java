package com.pmi.tpd.database.spi;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import com.pmi.tpd.database.DatabaseSupportLevel;
import com.pmi.tpd.database.DatabaseValidationException;

/**
 * Validates the target database for a migration, or initial setup, using the {@code DataSource} constructed from the
 * proposed configuration.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface IDatabaseValidator {

    /**
     * Validates the provided {@code DataSource}, ensuring a connection can be opened to it, and that the database
     * targeted satisfies the following preconditions:
     * <ol>
     * <li>Not {@link DatabaseSupportLevel#UNSUPPORTED unsupported}.</li>
     * <li>Encoding is UTF-8</li>
     * <li>No existing tables using our {@link com.pmi.tpd.core.database.core.backup.DatabaseTable table names}.</li>
     * <li>Configured user has sufficient privileges to modify the schema.</li>
     * <li>Queries are case-sensitive.</li>
     * </ol>
     *
     * @param dataSource
     *                   the data source to validate
     * @throws DatabaseValidationException
     *                                     if any of the above preconditions fails
     */
    void validate(@Nonnull DataSource dataSource);
}
