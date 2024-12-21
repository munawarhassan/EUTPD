package com.pmi.tpd.database.spi;

import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.pmi.tpd.database.DatabaseTableAttribute;

public interface IDatabaseTables extends Iterable<IDatabaseTable> {

    /**
     * @param first
     *            an attribute that each returned table must have
     * @param rest
     *            further attributes that each returned table must have (thank you, Java generics)
     * @return the names of all application tables having the supplied attributes, or all application tables if no
     *         attributes are specified
     */
    default IDatabaseTables with(final DatabaseTableAttribute first, final DatabaseTableAttribute... rest) {
        final EnumSet<DatabaseTableAttribute> desired = EnumSet.of(first, rest);
        return () -> from(this).filter(table -> table.getAttributes().containsAll(desired)).toList().iterator();
    }

    /**
     * Gets a ordering list of {@link DatabaseTable} according relationship between themselves.
     *
     * @param tableToDelete
     *            list of table to delete
     * @return Returns a new ordering {@link List} of {@link DatabaseTable} according relationship between themselves.
     */
    default IDatabaseTables orderingforDeletion() {
        final List<IDatabaseTable> list = Lists.newArrayList(this);
        Collections.reverse(list);
        return () -> list.iterator();
    }

    default List<String> getTableNames() {
        return Streams.stream(this).map((table) -> table.getTableName()).collect(Collectors.toUnmodifiableList());
    }
}
