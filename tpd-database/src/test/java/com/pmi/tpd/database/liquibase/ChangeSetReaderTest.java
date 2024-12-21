package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_AUTHOR;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_CONTEXT;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_DBMS;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.CHANGE_SET_ID;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_NAME;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_TYPE;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.DELETE;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.INSERT;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.TABLE_NAME;
import static com.pmi.tpd.database.liquibase.LiquibaseMatchers.columnConfig;
import static com.pmi.tpd.database.liquibase.LiquibaseMatchers.hasTableName;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.pmi.tpd.api.lifecycle.ICancelState;
import com.pmi.tpd.database.liquibase.backup.processor.IChangeSetProcessor;
import com.pmi.tpd.database.liquibase.backup.xml.XmlEncoder;
import com.pmi.tpd.testing.junit5.MockitoTestCase;
import com.pmi.tpd.testing.mockito.MockitoUtils;

import liquibase.change.core.DeleteDataChange;
import liquibase.change.core.InsertDataChange;
import liquibase.changelog.ChangeSet;

/**
 * Tests the reading of a Liquibase changeset log from a backup file.
 */
public class ChangeSetReaderTest extends MockitoTestCase {

    private static final String URI = "http://www.liquibase.org/xml/ns/dbchangelog";

    private static final String CDATA = "CDATA";

    @Mock
    private ICancelState cancelState;

    @Mock
    private IChangeSetProcessor monitor;

    @Mock(lenient = true)
    private XmlEncoder xmlEncoder;

    @Captor
    private ArgumentCaptor<ChangeSet> changeSetCaptor;

    @Captor
    private ArgumentCaptor<InsertDataChange> insertCaptor;

    @Captor
    private ArgumentCaptor<DeleteDataChange> deleteCaptor;

    @InjectMocks
    private ChangeSetReader reader;

    @BeforeEach
    public void setUp() {
        when(xmlEncoder.decode(anyString())).thenAnswer(MockitoUtils.returnFirst());
    }

    /**
     * Checks that an insert operation is not performed when there are no columns.
     */
    @Test
    public void testNoColumnsNoInsert() throws SAXException {
        reader.startElement(URI, CHANGE_SET, CHANGE_SET, changeSetAttributes("1", "author", "test", "hsql"));
        reader.startElement(URI, INSERT, INSERT, insertionAttributes("MyTable"));
        reader.endElement(URI, INSERT, INSERT);
        reader.endElement(URI, CHANGE_SET, CHANGE_SET);

        final InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).onChangesetBegin(changeSetCaptor.capture());
        inOrder.verify(monitor, never()).onChangesetContent(any(InsertDataChange.class));
        inOrder.verify(monitor).onChangesetComplete(changeSetCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        final ChangeSet changeSet = changeSetCaptor.getValue();
        assertThat(changeSetCaptor.getAllValues(), everyItem(equalTo(changeSet)));
        assertThat(changeSet.getAuthor(), equalTo("author"));
        assertThat(changeSet.getDbmsSet(), contains("hsql"));
        assertThat(changeSet.getId(), equalTo("1"));
    }

    /**
     * Tests a changeset with one insert.
     */
    @Test
    public void testInsert() throws SAXException {
        reader.startElement(URI, CHANGE_SET, CHANGE_SET, changeSetAttributes("1", "author", "test", "hsql"));
        reader.startElement(URI, INSERT, INSERT, insertionAttributes("MyTable"));
        reader.startElement(URI, COLUMN, COLUMN, columnAttributes("age", ColumnSerialisationType.NUMERIC));
        reader.characters("32".toCharArray(), 0, 2);
        reader.endElement(URI, COLUMN, COLUMN);
        reader.endElement(URI, INSERT, INSERT);
        reader.endElement(URI, CHANGE_SET, CHANGE_SET);

        final InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).onChangesetBegin(changeSetCaptor.capture());
        inOrder.verify(monitor).onChangesetContent(insertCaptor.capture());
        inOrder.verify(monitor).onChangesetComplete(changeSetCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        final ChangeSet changeSet = changeSetCaptor.getValue();
        assertThat(changeSetCaptor.getAllValues(), everyItem(equalTo(changeSet)));
        assertThat(changeSet.getAuthor(), equalTo("author"));
        assertThat(changeSet.getDbmsSet(), contains("hsql"));
        assertThat(changeSet.getId(), equalTo("1"));

        final InsertDataChange change = insertCaptor.getValue();
        assertThat(change, hasTableName("MyTable"));
        assertThat(change.getColumns(), hasSize(1));
        assertThat(change.getColumns(), hasItem(columnConfig("age", 32L)));
    }

    /**
     * Tests a changeset with one delete.
     */
    @Test
    public void testDeleteAdvancesProgress() throws SAXException {
        reader.startElement(URI, CHANGE_SET, CHANGE_SET, changeSetAttributes("1", "author", "test", "hsql"));
        reader.startElement(URI, DELETE, DELETE, deletionAttributes("MyTable"));
        reader.endElement(URI, DELETE, DELETE);
        reader.endElement(URI, CHANGE_SET, CHANGE_SET);

        final InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).onChangesetBegin(changeSetCaptor.capture());
        inOrder.verify(monitor).onChangesetContent(deleteCaptor.capture());
        inOrder.verify(monitor).onChangesetComplete(changeSetCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        final ChangeSet changeSet = changeSetCaptor.getValue();
        assertThat(changeSetCaptor.getAllValues(), everyItem(equalTo(changeSet)));
        assertThat(changeSet.getAuthor(), equalTo("author"));
        assertThat(changeSet.getDbmsSet(), contains("hsql"));
        assertThat(changeSet.getId(), equalTo("1"));

        final DeleteDataChange change = deleteCaptor.getValue();
        assertThat(change.getTableName(), equalTo("MyTable"));
        assertThat(change.getWhere(), is(emptyOrNullString()));
    }

    /**
     * Tests that column values are decoded before insertion into the database
     */
    @Test
    public void testDecodeColumnValue() throws SAXException {
        reader.startElement(URI, CHANGE_SET, CHANGE_SET, changeSetAttributes("1", "author", "test", "hsql"));
        reader.startElement(URI, INSERT, INSERT, insertionAttributes("MyTable"));
        reader.startElement(URI, COLUMN, COLUMN, columnAttributes("verticaltab", ColumnSerialisationType.CHARACTER));
        reader.characters("blah\u000Cblah".toCharArray(), 0, 9);
        reader.endElement(URI, COLUMN, COLUMN);
        reader.endElement(URI, INSERT, INSERT);
        reader.endElement(URI, CHANGE_SET, CHANGE_SET);

        final InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).onChangesetBegin(changeSetCaptor.capture());
        inOrder.verify(monitor).onChangesetContent(insertCaptor.capture());
        inOrder.verify(monitor).onChangesetComplete(changeSetCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        final ChangeSet changeSet = changeSetCaptor.getValue();
        assertThat(changeSetCaptor.getAllValues(), everyItem(equalTo(changeSet)));
        assertThat(changeSet.getAuthor(), equalTo("author"));
        assertThat(changeSet.getDbmsSet(), contains("hsql"));
        assertThat(changeSet.getId(), equalTo("1"));

        final InsertDataChange change = insertCaptor.getValue();
        assertThat(change, hasTableName("MyTable"));
        assertThat(change.getColumns(), hasSize(1));
        assertThat(change.getColumns(), hasItem(columnConfig("verticaltab", "blah\u000Cblah")));
    }

    /**
     * Tests on an empty changeset.
     */
    @Test
    public void testEmptyChangeset() throws SAXException {
        reader.startElement(URI, CHANGE_SET, CHANGE_SET, changeSetAttributes("1", "author", "test", "hsql"));
        reader.endElement(URI, CHANGE_SET, CHANGE_SET);

        final InOrder inOrder = inOrder(monitor);
        inOrder.verify(monitor).onChangesetBegin(changeSetCaptor.capture());
        inOrder.verify(monitor).onChangesetComplete(changeSetCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        final ChangeSet changeSet = changeSetCaptor.getValue();
        assertThat(changeSetCaptor.getAllValues(), everyItem(equalTo(changeSet)));
        assertThat(changeSet.getAuthor(), equalTo("author"));
        assertThat(changeSet.getDbmsSet(), contains("hsql"));
        assertThat(changeSet.getId(), equalTo("1"));
    }

    private static Attributes changeSetAttributes(final String id,
        final String author,
        final String context,
        final String dbms) {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(URI, CHANGE_SET_ID, CHANGE_SET_ID, CDATA, id);
        attributes.addAttribute(URI, CHANGE_SET_AUTHOR, CHANGE_SET_AUTHOR, CDATA, author);
        attributes.addAttribute(URI, CHANGE_SET_CONTEXT, CHANGE_SET_CONTEXT, CDATA, context);
        attributes.addAttribute(URI, CHANGE_SET_DBMS, CHANGE_SET_DBMS, CDATA, dbms);
        return attributes;
    }

    private static Attributes columnAttributes(final String name,
        final ColumnSerialisationType columnSerialisationType) {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(URI, COLUMN_NAME, COLUMN_NAME, CDATA, name);
        attributes.addAttribute(URI, COLUMN_TYPE, COLUMN_TYPE, CDATA, columnSerialisationType.toString());
        return attributes;
    }

    private static Attributes deletionAttributes(final String tableName) {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(URI, TABLE_NAME, TABLE_NAME, CDATA, tableName);
        return attributes;
    }

    private static Attributes insertionAttributes(final String tableName) {
        final AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(URI, TABLE_NAME, TABLE_NAME, CDATA, tableName);
        return attributes;
    }
}
