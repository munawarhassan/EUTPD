package com.pmi.tpd.database.liquibase.backup;

/**
 * Information about a Liquibase changeset.
 *
 * @see ILiquibaseRestoreMonitor
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ILiquibaseChangeSet {

    /**
     * Gets the number of contained change.
     * 
     * @return Returns a long representing the number of contained change.
     */
    long getChangeCount();

    /**
     * Gets the weight.
     * 
     * @return Returns the weight.
     */
    int getWeight();

}
