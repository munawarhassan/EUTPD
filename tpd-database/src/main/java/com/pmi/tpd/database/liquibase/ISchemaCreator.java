package com.pmi.tpd.database.liquibase;

import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ISchemaCreator {

    /**
     * @param dataSource
     * @throws LiquibaseException
     */
    void createSchema(DataSource dataSource) throws LiquibaseException;
}
