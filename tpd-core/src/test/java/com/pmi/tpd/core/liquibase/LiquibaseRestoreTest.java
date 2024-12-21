package com.pmi.tpd.core.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.ENCODING;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.TABLE_NAME;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

import com.google.common.io.Closeables;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.core.database.DatabaseTable;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseXmlWriterFactory;
import com.pmi.tpd.database.liquibase.ILiquibaseXmlWriter;
import com.pmi.tpd.database.liquibase.ILiquibaseXmlWriterFactory;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseChangeSet;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseRestoreMonitor;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

import liquibase.change.ColumnConfig;
import liquibase.change.core.InsertDataChange;

/**
 * Tests restoring the current database.
 */
public class LiquibaseRestoreTest extends MockitoTestCase {

    /**
     * This SAMPLE_XML_DATA is a torture test for the XML processing aspects of the migration process. If we can write
     * it to a file via the DefaultLiquibaseXmlWriter, and parse it via LiquibaseRestoreStrategy, we can be pretty sure
     * that we won't lose characters in the migration process.
     */
    static final String SAMPLE_XML_DATA = "      <?xml version=\"1.0\"?><note>\n"
            + "function matchwo(a,b)\n{     \nif (a < b && a < 0) then\n  {\n"
            + "  return 1; \\    \\u0043  \\\\ \\\\usdfgsfg  \n  }\r\n\telse\n"
            + "  {\n  return 0;\\n  }\n}&#40;\n</note>    ";

    @Mock
    private ICancelState cancelState;

    @Mock(lenient = true)
    private ILiquibaseAccessor dao;

    @Mock
    private ILiquibaseXmlWriterFactory factory;

    @Captor
    private ArgumentCaptor<InsertDataChange> insertCaptor;

    @Mock
    private ILiquibaseRestoreMonitor monitor;

    public Path tempFolder;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp(@TempDir final Path path) throws Exception {
        this.tempFolder = path;
        final Answer<Void> applyEffect = invocation -> {
            final Consumer<ILiquibaseAccessor> effect = (Consumer<ILiquibaseAccessor>) invocation.getArguments()[0];
            effect.accept(dao);
            return null;
        };
        doAnswer(applyEffect).when(dao).withLock(anyEffect());
        doAnswer(applyEffect).when(dao).withLock(anyEffect());
        doReturn("unknown").when(dao).getDatabaseType();
    }

    /**
     * This test checks in particular that change sets are executed to turn off, and then turn on again, referential
     * integrity constraint checking.
     * <p>
     * The tests that follow are less rigorous with respect to those checks.
     */
    @Test
    public void restoreEmptyChangeLog() throws XMLStreamException {
        doRestore(createEmptyChangeLog());
        final InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).withLock(anyEffect());
        inOrder.verify(dao).getDatabaseType();
        verifyNoMoreInteractions(dao);
    }

    /**
     * Tests the restore operation against a change log that is minimal; that is, it contains only one change, that
     * being the deletion of all rows from the {@code hibernate_unique_key} table.
     */
    @Test
    public void restoreMinimalChangeLog() throws XMLStreamException {
        doRestore(createMinimalChangeLog());
        final InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).withLock(anyEffect());
        inOrder.verify(dao).getDatabaseType();
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).deleteAllRows("test");
        inOrder.verify(dao).endChangeSet();
        verifyNoMoreInteractions(dao);
    }

    @Test
    public void restoreChangeLogWithMultipleTablesAndRows() {
        doRestore(createChangeLogWithMultipleTablesAndRows());

        final InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).withLock(anyEffect());
        inOrder.verify(dao).getDatabaseType();
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).deleteAllRows(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());
        inOrder.verify(dao).endChangeSet();
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao, times(2)).insert(insertCaptor.capture());
        inOrder.verify(dao).endChangeSet();
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).insert(insertCaptor.capture());
        inOrder.verify(dao).endChangeSet();
        verifyNoMoreInteractions(dao);

        final InsertDataChange insertion1 = insertCaptor.getAllValues().get(0);
        final InsertDataChange insertion2 = insertCaptor.getAllValues().get(1);
        final InsertDataChange insertion3 = insertCaptor.getAllValues().get(2);

        assertEquals(DatabaseTable.APP_USER.getTableName(), insertion1.getTableName());
        assertEquals(DatabaseTable.APP_USER.getTableName(), insertion2.getTableName());
        assertEquals(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName(), insertion3.getTableName());

        final List<ColumnConfig> userRow1 = insertion1.getColumns();
        final List<ColumnConfig> userRow2 = insertion2.getColumns();
        final List<ColumnConfig> hibernateRow1 = insertion3.getColumns();

        assertEquals(2, userRow1.size());
        assertEquals("id", userRow1.get(0).getName());
        assertEquals(1L, userRow1.get(0).getValueNumeric().longValue());
        assertEquals("name", userRow1.get(1).getName());
        assertEquals("sysadmin", userRow1.get(1).getValue());

        assertEquals(2, userRow2.size());
        assertEquals("id", userRow2.get(0).getName());
        assertEquals(2L, userRow2.get(0).getValueNumeric().longValue());
        assertEquals("name", userRow2.get(1).getName());
        assertEquals("admin", userRow2.get(1).getValue());

        assertEquals(1, hibernateRow1.size());
        assertEquals("next_hi", hibernateRow1.get(0).getName());
        assertEquals(0L, hibernateRow1.get(0).getValueNumeric().longValue());
    }

    @Test
    public void reportingProgressOfRestore() {
        doRestore(createChangeLogWithMultipleTablesAndRows());

        final InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).onBeginChangeset(any(ILiquibaseChangeSet.class), eq(1), eq(3));
        inOrder.verify(monitor).onAppliedChange();
        inOrder.verify(monitor).onFinishedChangeset();
        inOrder.verify(monitor).onBeginChangeset(any(ILiquibaseChangeSet.class), eq(2), eq(3));
        inOrder.verify(monitor, times(2)).onAppliedChange();
        inOrder.verify(monitor).onFinishedChangeset();
        inOrder.verify(monitor).onBeginChangeset(any(ILiquibaseChangeSet.class), eq(3), eq(3));
        inOrder.verify(monitor).onAppliedChange();
        inOrder.verify(monitor).onFinishedChangeset();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void restoreChangeLogWithXmlStuff() {
        doRestore(createChangeLogWithXmlData());

        final InOrder inOrder = inOrder(dao);
        inOrder.verify(dao).withLock(anyEffect());
        inOrder.verify(dao).getDatabaseType();
        inOrder.verify(dao).beginChangeSet();
        inOrder.verify(dao).insert(insertCaptor.capture());
        inOrder.verify(dao).endChangeSet();

        final InsertDataChange insert = insertCaptor.getValue();
        final ColumnConfig column = insert.getColumns().get(0);
        assertEquals(SAMPLE_XML_DATA, column.getValue());
    }

    // ==================================================================================================================
    // Private methods that create change logs for tests
    // ==================================================================================================================

    private String createChangeLogWithMultipleTablesAndRows() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();

            final ILiquibaseXmlWriter writer = createWriter(out);
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDatabaseChangeLogStartElement();
            writer.writeChangeSetToDeleteRowsFromTable(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());

            writer.writeChangeSetStartElement(DatabaseTable.APP_USER.getTableName());
            writer.writeStartElement("insert");
            writer.writeAttribute(TABLE_NAME, DatabaseTable.APP_USER.getTableName());
            writer.writeColumn("id", 1);
            writer.writeColumn("name", "sysadmin");
            writer.writeEndElement(); // end of insert
            writer.writeStartElement("insert");
            writer.writeAttribute(TABLE_NAME, DatabaseTable.APP_USER.getTableName());
            writer.writeColumn("id", 2);
            writer.writeColumn("name", "admin");
            writer.writeEndElement(); // end of insert
            writer.writeEndElement(); // end of change set

            writer.writeChangeSetStartElement(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());
            writer.writeStartElement("insert");
            writer.writeAttribute(TABLE_NAME, DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());
            writer.writeColumn("next_hi", 0);
            writer.writeEndElement(); // end of insert
            writer.writeEndElement(); // end of change set

            writer.writeEndElement(); // end of database change log
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            return out.toString();
        } catch (final XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private String createChangeLogWithXmlData() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ILiquibaseXmlWriter writer = createWriter(out);
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDatabaseChangeLogStartElement();
            writer.writeChangeSetStartElement("test");
            writer.writeStartElement("insert");
            writer.writeAttribute(TABLE_NAME, "test");
            writer.writeColumn("someXmlStuff", SAMPLE_XML_DATA);
            writer.writeEndElement(); // end of insert
            writer.writeEndElement(); // end of change set
            writer.writeEndElement(); // end of database change log
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            return out.toString();
        } catch (final XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private String createEmptyChangeLog() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ILiquibaseXmlWriter writer = createWriter(out);
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDatabaseChangeLogStartElement();
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            return out.toString();
        } catch (final XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private String createMinimalChangeLog() {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ILiquibaseXmlWriter writer = createWriter(out);
            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeDatabaseChangeLogStartElement();
            writer.writeChangeSetToDeleteRowsFromTable("test");
            writer.writeEndElement(); // end of database change log
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            return out.toString();
        } catch (final XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    // ==================================================================================================================
    // Private helper methods
    // ==================================================================================================================

    /** Runs a restore based on the given change log string */
    private void doRestore(final String changeLog) {
        final InputStream in = new ByteArrayInputStream(changeLog.getBytes());
        try {
            new DefaultLiquibaseMigrationDao(factory).restore(dao, in, tempFolder.toFile(), monitor, cancelState);
        } finally {
            Closeables.closeQuietly(in);
        }
    }

    private ILiquibaseXmlWriter createWriter(final ByteArrayOutputStream out) {
        return new DefaultLiquibaseXmlWriterFactory().create(out, "test.user");
    }

    @SuppressWarnings("unchecked")
    private Consumer<ILiquibaseAccessor> anyEffect() {
        return any(Consumer.class);
    }

}
