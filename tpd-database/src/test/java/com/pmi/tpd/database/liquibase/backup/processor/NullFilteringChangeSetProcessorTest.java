package com.pmi.tpd.database.liquibase.backup.processor;

import static com.pmi.tpd.database.liquibase.LiquibaseChangeSetTestUtils.newColumnConfig;
import static com.pmi.tpd.database.liquibase.LiquibaseMatchers.columnConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.util.Date;

import org.junit.jupiter.api.Test;

import liquibase.change.core.InsertDataChange;

public class NullFilteringChangeSetProcessorTest {

    @Test
    public void testFilterInsert() {
        final InsertDataChange insert = new InsertDataChange();
        insert.addColumn(
            newColumnConfig("col1", "some with CJK characters (\u6e2c\u8a66) and a null character (\u0000)"));
        insert.addColumn(
            newColumnConfig("col2", "some other text with only Cyrillic characters (\u0442\u0435\u0441\u0442)"));
        insert.addColumn(newColumnConfig("col3", 42L));
        insert.addColumn(newColumnConfig("col4", true));
        insert.addColumn(newColumnConfig("col5", "some more\u0000 text with null\u0000 characters"));
        insert.addColumn(newColumnConfig("col6", new Date(42L)));

        new NullFilteringChangeSetProcessor().onChangesetContent(insert);

        assertThat(insert.getColumns(),
            hasItem(columnConfig("col1", "some with CJK characters (\u6e2c\u8a66) and a null character ()")));
        assertThat(insert.getColumns(),
            hasItem(columnConfig("col2", "some other text with only Cyrillic characters (\u0442\u0435\u0441\u0442)")));
        assertThat(insert.getColumns(), hasItem(columnConfig("col3", 42L)));
        assertThat(insert.getColumns(), hasItem(columnConfig("col4", true)));
        assertThat(insert.getColumns(), hasItem(columnConfig("col5", "some more text with null characters")));
        assertThat(insert.getColumns(), hasItem(columnConfig("col6", new Date(42L))));
    }

}
