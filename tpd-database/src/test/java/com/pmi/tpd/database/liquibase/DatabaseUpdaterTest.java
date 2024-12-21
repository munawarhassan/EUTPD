package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseChangeSetTestUtils.newChangeSet;
import static com.pmi.tpd.database.liquibase.LiquibaseChangeSetTestUtils.newColumnConfig;
import static com.pmi.tpd.database.liquibase.LiquibaseChangeSetTestUtils.newDeleteChange;
import static com.pmi.tpd.database.liquibase.LiquibaseChangeSetTestUtils.newInsertChange;
import static com.pmi.tpd.database.liquibase.LiquibaseMatchers.columnConfig;
import static com.pmi.tpd.database.liquibase.LiquibaseMatchers.hasTableName;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.xml.sax.SAXException;

import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * Tests the operation of the {@link DatabaseUpdater} class;
 */
public class DatabaseUpdaterTest extends MockitoTestCase {

    private static final int COUNT_NON_EMPTY_CHANGES = 42;

    @Mock
    private ILiquibaseAccessor dao;

    @Mock
    private Iterator<LiquibaseChangeSetMetaData> outline;

    @Mock
    private ChangeLogOutline changeLogOutline;

    @Mock
    private ILiquibaseRestoreMonitor monitor;

    @Captor
    private ArgumentCaptor<InsertDataChange> insertCaptor;

    @Captor
    private ArgumentCaptor<DeleteDataChange> deleteCaptor;

    /** An instance of the class under test */
    private DatabaseUpdater updater;

    @BeforeEach
    public void setUp() {
        when(changeLogOutline.nonEmptyChangeSetCount()).thenReturn(COUNT_NON_EMPTY_CHANGES);
        when(changeLogOutline.iterator()).thenReturn(outline);

        updater = new DatabaseUpdater(dao, monitor, changeLogOutline);
    }

    /**
     * Checks that an insert operation is not performed when there are no columns.
     */
    @Test
    public void testNoColumnsNoInsert() throws SAXException {
        final LiquibaseChangeSetMetaData summary = new LiquibaseChangeSetMetaData(1, 0);
        when(outline.hasNext()).thenReturn(true);
        when(outline.next()).thenReturn(summary);

        final ChangeSet changeSet = newChangeSet("1", "test", "test", "hsql");
        updater.onChangesetBegin(changeSet);
        updater.onChangesetComplete(changeSet);

        final InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao, never()).insert(any(InsertDataChange.class));
        inOrder.verify(dao).endChangeSet();
        inOrder.verifyNoMoreInteractions();
    }

    /**
     * Tests that an insert operation will go ahead despite the fact that it's containing changelog has zero weight.
     */
    @Test
    public void testZeroWeight() throws SAXException {
        final LiquibaseChangeSetMetaData summary = new LiquibaseChangeSetMetaData(1, 0);
        when(outline.hasNext()).thenReturn(true);
        when(outline.next()).thenReturn(summary);

        final ChangeSet changeSet = newChangeSet("1", "author", "test", "hsql");
        final InsertDataChange insert = newInsertChange("MyTable", newColumnConfig("age", 32L));
        updater.onChangesetBegin(changeSet);
        updater.onChangesetContent(insert);
        updater.onChangesetComplete(changeSet);

        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).insert(insertCaptor.capture());
        inOrder.verify(dao).endChangeSet();
        inOrder.verifyNoMoreInteractions();

        final InsertDataChange change = insertCaptor.getValue();
        assertThat(change, hasTableName("MyTable"));
        assertThat(change.getColumns(), hasSize(1));
        assertThat(change.getColumns(), hasItem(columnConfig("age", 32L)));

        inOrder = inOrder(monitor);
        inOrder.verify(monitor).onBeginChangeset(eq(summary), eq(1), eq(COUNT_NON_EMPTY_CHANGES));
        inOrder.verify(monitor).onAppliedChange();
        inOrder.verify(monitor).onFinishedChangeset();
    }

    /**
     * Tests that advancing progress is reported when processing an insert.
     */
    @Test
    public void testInsertAdvancesProgress() throws SAXException {
        final LiquibaseChangeSetMetaData summary = new LiquibaseChangeSetMetaData(1, 100);
        when(outline.hasNext()).thenReturn(true);
        when(outline.next()).thenReturn(summary);

        final ChangeSet changeSet = newChangeSet("1", "author", "test", "hsql");
        final InsertDataChange insert = newInsertChange("MyTable", newColumnConfig("age", 32L));
        updater.onChangesetBegin(changeSet);
        updater.onChangesetContent(insert);
        updater.onChangesetComplete(changeSet);

        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).insert(insertCaptor.capture());
        inOrder.verify(dao).endChangeSet();
        inOrder.verifyNoMoreInteractions();

        final InsertDataChange change = insertCaptor.getValue();
        assertThat(change, hasTableName("MyTable"));
        assertThat(change.getColumns(), hasSize(1));
        assertThat(change.getColumns(), hasItem(columnConfig("age", 32L)));

        inOrder = inOrder(monitor);
        inOrder.verify(monitor).onBeginChangeset(eq(summary), eq(1), eq(COUNT_NON_EMPTY_CHANGES));
        inOrder.verify(monitor).onAppliedChange();
        inOrder.verify(monitor).onFinishedChangeset();
    }

    /**
     * Tests that advancing progress is reported when processing an insert.
     */
    @Test
    public void testDeleteAdvancesProgress() throws SAXException {
        final LiquibaseChangeSetMetaData summary = new LiquibaseChangeSetMetaData(1, 100);
        when(outline.hasNext()).thenReturn(true);
        when(outline.next()).thenReturn(summary);

        final ChangeSet changeSet = newChangeSet("1", "author", "test", "hsql");
        final DeleteDataChange delete = newDeleteChange("MyTable");
        updater.onChangesetBegin(changeSet);
        updater.onChangesetContent(delete);
        updater.onChangesetComplete(changeSet);

        InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).deleteAllRows(eq("MyTable"));
        inOrder.verify(dao).endChangeSet();
        inOrder.verifyNoMoreInteractions();

        inOrder = inOrder(monitor);
        inOrder.verify(monitor).onBeginChangeset(eq(summary), eq(1), eq(COUNT_NON_EMPTY_CHANGES));
        inOrder.verify(monitor).onAppliedChange();
    }

    /**
     * Tests that column values are decoded before insertion into the database
     */
    @Test
    public void testDecodeColumnValue() throws SAXException {
        final LiquibaseChangeSetMetaData summary = new LiquibaseChangeSetMetaData(1, 100);
        when(outline.hasNext()).thenReturn(true);
        when(outline.next()).thenReturn(summary);

        final ChangeSet changeSet = newChangeSet("1", "author", "test", "hsql");
        final InsertDataChange insert = newInsertChange("MyTable", newColumnConfig("verticaltab", "blah\u000Cblah"));
        updater.onChangesetBegin(changeSet);
        updater.onChangesetContent(insert);
        updater.onChangesetComplete(changeSet);

        final InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).insert(insertCaptor.capture());
        inOrder.verify(dao).endChangeSet();
        inOrder.verifyNoMoreInteractions();

        final InsertDataChange change = insertCaptor.getValue();
        assertThat(change, hasTableName("MyTable"));
        assertThat(change.getColumns(), hasSize(1));
        assertThat(change.getColumns(), hasItem(columnConfig("verticaltab", "blah\u000Cblah")));
    }

    /**
     * Tests that an empty changeset is not applied.
     */
    @Test
    public void testEmptyChangeset() throws SAXException {
        final LiquibaseChangeSetMetaData summary = new LiquibaseChangeSetMetaData(0, 100);
        when(outline.hasNext()).thenReturn(true);
        when(outline.next()).thenReturn(summary);

        final ChangeSet changeSet = newChangeSet("1", "author", "test", "hsql");
        updater.onChangesetBegin(changeSet);
        updater.onChangesetComplete(changeSet);

        verifyZeroInteractions(dao);
    }

}
