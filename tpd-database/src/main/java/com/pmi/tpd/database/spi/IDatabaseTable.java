package com.pmi.tpd.database.spi;

import java.util.Set;

import com.pmi.tpd.database.DatabaseTableAttribute;

public interface IDatabaseTable {

    /**
     * @return the lower-cased name of the table in the database
     */
    String getTableName();

    /**
     * @return an optional column name to order the table's rows in a backup
     */
    String getOrderingColumn();

    /**
     * @return
     */
    Set<DatabaseTableAttribute> getAttributes();

}