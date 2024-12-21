package com.pmi.tpd.core.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.ENCODING;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.notNull;

import java.io.OutputStream;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.core.database.DatabaseTable;
import com.pmi.tpd.core.database.DefaultDatabaseTables;
import com.pmi.tpd.database.liquibase.DefaultLiquibaseMigrationDao;
import com.pmi.tpd.database.liquibase.ILiquibaseXmlWriter;
import com.pmi.tpd.database.liquibase.ILiquibaseXmlWriterFactory;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseAccessor;
import com.pmi.tpd.database.liquibase.backup.ILiquibaseBackupMonitor;
import com.pmi.tpd.database.liquibase.backup.LiquibaseDataAccessException;
import com.pmi.tpd.database.spi.IDatabaseTable;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests backing up the current database.
 */
public class LiquibaseBackupTest extends MockitoTestCase {

    private static final String AUTHOR = "test.user";

    private static final long ROW_COUNT = 10L;

    @Mock
    private ICancelState cancelState;

    @Mock(lenient = true)
    private ILiquibaseAccessor dao;

    @Mock
    private ILiquibaseXmlWriter writer;

    @Mock
    private ILiquibaseXmlWriterFactory writerFactory;

    @Mock
    private ILiquibaseBackupMonitor monitor;

    @BeforeEach
    public void setUp() throws Exception {
        when(writerFactory.create(any(OutputStream.class), anyString())).thenReturn(writer);
        when(dao.getDatabaseTables()).thenReturn(new DefaultDatabaseTables());
    }

    @Test
    public void testBackupEmptyDatabase() throws XMLStreamException {
        when(dao.getTableNames()).thenReturn(Lists.<String> newArrayList());

        doBackup();

        final InOrder inOrder = inOrder(writerFactory, writer);
        inOrder.verify(writerFactory).create(any(OutputStream.class), eq(AUTHOR));
        inOrder.verify(writer).writeStartDocument(eq(ENCODING), anyString());
        inOrder.verify(writer).writeDatabaseChangeLogStartElement();
        inOrder.verify(writer, times(6)).writeChangeSetToDeleteRowsFromTable(anyString());
        inOrder.verify(writer).writeEndDocument();
        inOrder.verify(writer).flush();
    }

    @Test
    public void testBackupDatabaseWithOneHibernateUniqueKeyRecord() throws XMLStreamException {
        when(dao.getTableNames()).thenReturn(Lists.newArrayList(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()));
        when(dao.getColumnNames(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()))
                .thenReturn(Lists.newArrayList("next_hi"));

        doBackup();

        final InOrder inOrder = inOrder(writerFactory, writer, dao);
        inOrder.verify(writerFactory).create(any(OutputStream.class), eq(AUTHOR));
        inOrder.verify(writer).writeStartDocument(eq(ENCODING), anyString());
        inOrder.verify(writer).writeDatabaseChangeLogStartElement();
        inOrder.verify(writer).writeChangeSetToDeleteRowsFromTable(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());
        inOrder.verify(writer).writeChangeSetStartElement(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());
        inOrder.verify(dao)
                .forEachRow(eq(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()),
                    noOrder(),
                    eq(cancelState),
                    anyConsumer());
        inOrder.verify(writer).writeEndElement(); // end of change set
        inOrder.verify(writer).writeEndDocument();
        inOrder.verify(writer).flush();
    }

    @Test
    public void testBackupDatabaseWithRowsInTwoTables() throws XMLStreamException {
        mockDatabaseWithRowsInTwoTables();

        doBackup();

        final InOrder inOrder = inOrder(writerFactory, writer, dao);
        inOrder.verify(writerFactory).create(any(OutputStream.class), eq(AUTHOR));
        inOrder.verify(writer).writeStartDocument(eq(ENCODING), anyString());
        inOrder.verify(writer).writeDatabaseChangeLogStartElement();
        inOrder.verify(writer).writeChangeSetToDeleteRowsFromTable(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());

        inOrder.verify(writer).writeChangeSetStartElement(DatabaseTable.APP_USER.getTableName());
        inOrder.verify(dao)
                .forEachRow(eq(DatabaseTable.APP_USER.getTableName()), noOrder(), eq(cancelState), anyConsumer());
        inOrder.verify(writer).writeEndElement(); // end of change set

        inOrder.verify(writer).writeChangeSetStartElement(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName());
        inOrder.verify(dao)
                .forEachRow(eq(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()),
                    noOrder(),
                    eq(cancelState),
                    anyConsumer());
        inOrder.verify(writer).writeEndElement(); // end of change set

        inOrder.verify(writer).writeEndDocument();
        inOrder.verify(writer).flush();
    }

    private void mockDatabaseWithRowsInTwoTables() {
        when(dao.getTableNames()).thenReturn(Lists.newArrayList(DatabaseTable.APP_USER.getTableName(),
            DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()));
        when(dao.countRows(
            or(eq(DatabaseTable.APP_USER.getTableName()), eq(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()))))
                    .thenReturn(ROW_COUNT);
        when(dao.getColumnNames(DatabaseTable.APP_USER.getTableName())).thenReturn(Lists.newArrayList("id", "name"));
        when(dao.getColumnNames(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()))
                .thenReturn(Lists.newArrayList("next_hi"));
    }

    @Test
    public void testRowOrdersAreApplied() throws XMLStreamException {
        doBackup();
        for (final IDatabaseTable table : DatabaseTable.getTables()) {
            verify(dao).forEachRow(eq(table.getTableName()),
                eq(table.getOrderingColumn()),
                eq(cancelState),
                anyConsumer());
        }
    }

    /**
     * Tests that insert operations for tables beginning with "AO_" are not included in the change log.
     */
    @Test
    public void testBackupDatabaseWithOneAoTable() throws XMLStreamException {
        when(dao.getTableNames()).thenReturn(
            Lists.newArrayList(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName(), "ao_sample", "some_other_junk"));
        when(dao.getColumnNames(DatabaseTable.HIBERNATE_GENERATED_ID.getTableName()))
                .thenReturn(Lists.newArrayList("next_hi"));
        when(dao.getColumnNames("ao_sample")).thenReturn(Lists.newArrayList("test"));

        doBackup();

        // we expect to *not* write out the rows of the ao_sample table
        verify(dao, never()).forEachRow(eq("ao_sample"), noOrder(), eq(cancelState), anyConsumer());
    }

    @Test
    public void testProgressStarted() {
        mockDatabaseWithRowsInTwoTables();
        doBackup();
        verify(monitor).started(ROW_COUNT * Iterables.size(dao.getTableNames()));
        // Progress is updated per row but this cannot be tested here without multiple layers of mocks
        // Instead it is tested in WriteXmlFForRowEffectTest
    }

    @Test
    public void testExceptionWhileWritingXml() throws Exception {
        assertThrows(LiquibaseDataAccessException.class, () -> {
            mockDatabaseWithRowsInTwoTables();
            doThrow(XMLStreamException.class).when(writer).writeEndDocument();

            try {
                doBackup();
            } catch (final LiquibaseDataAccessException e) {
                assertSame(XMLStreamException.class, e.getCause().getClass());
                throw e;
            }
        });
    }

    private void doBackup() {
        final DefaultLiquibaseMigrationDao defaultLiquibaseMigrationDao = new DefaultLiquibaseMigrationDao(
                writerFactory);
        defaultLiquibaseMigrationDao.backup(dao, NullOutputStream.NULL_OUTPUT_STREAM, AUTHOR, monitor, cancelState);
    }

    private String noOrder() {
        return isNull();
    }

    private Consumer<Map<String, Object>> anyConsumer() {
        return notNull();
    }

}
