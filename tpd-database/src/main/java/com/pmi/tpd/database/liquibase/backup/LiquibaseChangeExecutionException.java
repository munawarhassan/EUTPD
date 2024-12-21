package com.pmi.tpd.database.liquibase.backup;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

import liquibase.change.Change;

/**
 * Parent class of exceptions indicating that the execution of a Liquibase change operation (e.g. insert data change)
 * failed.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseChangeExecutionException extends InvalidDataAccessResourceUsageException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LiquibaseChangeExecutionException(final Change change, final Throwable cause) {
        super("Failed to execute change: " + change.createChangeMetaData().getDescription(), cause);
    }
}
