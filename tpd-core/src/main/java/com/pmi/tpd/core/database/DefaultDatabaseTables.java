package com.pmi.tpd.core.database;

import java.util.Iterator;
import java.util.List;

import com.pmi.tpd.database.spi.IDatabaseTable;
import com.pmi.tpd.database.spi.IDatabaseTables;

public class DefaultDatabaseTables implements IDatabaseTables {

    private static final List<IDatabaseTable> list = DatabaseTable.getTables();

    @Override
    public Iterator<IDatabaseTable> iterator() {
        return list.iterator();
    }

}
