package com.pmi.tpd.core.backup;

import static com.pmi.tpd.core.database.DatabaseTable.APP_GROUP;
import static com.pmi.tpd.core.database.DatabaseTable.APP_USER_GROUP;
import static com.pmi.tpd.core.database.DatabaseTable.AUDIT_EVENT;
import static com.pmi.tpd.core.database.DatabaseTable.AUDIT_EVENT_DATA;
import static com.pmi.tpd.core.database.DatabaseTable.HIBERNATE_GENERATED_ID;
import static com.pmi.tpd.core.database.DatabaseTable.isKnownTable;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.number.OrderingComparison;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.core.database.DatabaseTable;
import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.database.DatabaseTableAttribute;
import com.pmi.tpd.database.spi.IDatabaseTable;
import com.pmi.tpd.testing.junit5.TestCase;

public class DatabaseTableTest extends TestCase {

    @Test
    public void testGetTableNames() {
        assertEquals(DatabaseTable.values().length,
            Iterables.size(DatabaseTable.getTables()),
            "getTableNames() with no attributes should return all tables");

    }

    @Test
    public void sanityTestOrderingSatisfiesForeignKeyConstraints() {
        // will fail if someone, say, alphabetizes the DatabaseTable enum
        MatcherAssert.assertThat(
            String.format("%s table should always precede %s table to appease foreign key constraints on restore!",
                AUDIT_EVENT.getTableName(),
                AUDIT_EVENT_DATA.getTableName()),
            AUDIT_EVENT.ordinal(),
            OrderingComparison.lessThan(AUDIT_EVENT_DATA.ordinal()));
    }

    @Test
    public void testIsKnownTable() {
        assertTrue(isKnownTable(HIBERNATE_GENERATED_ID.getTableName()));
        assertFalse(isKnownTable("bogus"));
    }

    @Test
    public void tableNamesAreLowerCase() throws Exception {
        for (final IDatabaseTable table : DatabaseTable.values()) {
            assertEquals(table.getTableName().toLowerCase(), table.getTableName());
        }
    }

    @Test
    public void orderingColumnIsNullOrNotBlank() throws Exception {
        for (final IDatabaseTable table : DatabaseTable.values()) {
            final String orderingColumn = table.getOrderingColumn();
            assertTrue(orderingColumn == null || StringUtils.isNotBlank(orderingColumn));
        }
    }

    @Test
    public void testOrderingforDeletion() {

        final List<IDatabaseTable> orderedList = Lists.newArrayList(
            new DefaultDatabaseTables().orderingforDeletion().with(DatabaseTableAttribute.PREPOPULATED));
        MatcherAssert.assertThat(String.format(
            "ManyToMany %s table should always precede %s table to appease mtn foreign key constraints on deletion!",
            APP_USER_GROUP.getTableName(),
            APP_GROUP.getTableName()),
            orderedList.indexOf(APP_USER_GROUP),
            OrderingComparison.lessThan(orderedList.indexOf(APP_GROUP)));
    }
}
