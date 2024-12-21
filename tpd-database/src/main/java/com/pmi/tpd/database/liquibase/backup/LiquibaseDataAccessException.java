package com.pmi.tpd.database.liquibase.backup;

import org.springframework.dao.UncategorizedDataAccessException;

import liquibase.database.Database;

/**
 * Parent class of exceptions that don't distinguish anything more specific than something went wrong with the Liquibase
 * system.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class LiquibaseDataAccessException extends UncategorizedDataAccessException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public LiquibaseDataAccessException(final String msg, final Database database, final Throwable cause) {
        this(msg + ". Database type: " + database.getShortName(), cause);
    }

    public LiquibaseDataAccessException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
