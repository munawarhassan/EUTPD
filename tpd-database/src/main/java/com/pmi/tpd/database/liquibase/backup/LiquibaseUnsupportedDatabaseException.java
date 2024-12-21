package com.pmi.tpd.database.liquibase.backup;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * An exception indicating that Liquibase does not support the database to which a connection has attempted to be made.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseUnsupportedDatabaseException extends DataAccessResourceFailureException {

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param databaseProductName
     */
    public LiquibaseUnsupportedDatabaseException(final String databaseProductName) {
        super("No Liquibase support for " + databaseProductName);
    }
}
