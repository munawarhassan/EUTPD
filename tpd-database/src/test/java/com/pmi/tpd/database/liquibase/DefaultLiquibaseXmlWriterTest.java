package com.pmi.tpd.database.liquibase;

import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_NAME;
import static com.pmi.tpd.database.liquibase.LiquibaseConstants.COLUMN_TYPE;
import static org.mockito.ArgumentMatchers.anyString;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.pmi.tpd.database.liquibase.backup.xml.XmlEncoder;
import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests the operation of the {@link DefaultLiquibaseXmlWriter} class.
 */

public class DefaultLiquibaseXmlWriterTest extends MockitoTestCase {

    private static final String ENCODED = "encoded";

    @Mock
    private XMLStreamWriter delegate;

    @Mock(lenient = true)
    private IChangeSetIdGenerator idGenerator;

    @Mock(lenient = true)
    private XmlEncoder xmlEncoder;

    /** An instance of the class under test */
    private DefaultLiquibaseXmlWriter writer;

    @BeforeEach
    public void setUp() {
        when(idGenerator.next(anyString())).thenReturn("bogus");
        when(xmlEncoder.encode(anyString())).thenReturn(ENCODED);
        writer = new DefaultLiquibaseXmlWriter(delegate, idGenerator, "test", xmlEncoder);
    }

    @Test
    public void testWriteDatabaseChangeLogStart() throws XMLStreamException {
        writer.writeDatabaseChangeLogStartElement();
        verify(delegate).writeStartElement("databaseChangeLog");
    }

    @Test
    public void testWriteChangeSetToDeleteRowsFromTable() throws XMLStreamException {
        writer.writeChangeSetToDeleteRowsFromTable("foo");

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeStartElement("changeSet");
        inOrder.verify(delegate).writeAttribute("id", "bogus");
        inOrder.verify(delegate).writeAttribute("author", "test");
        inOrder.verify(delegate).writeStartElement("delete");
        inOrder.verify(delegate).writeAttribute("tableName", "foo");
        inOrder.verify(delegate).writeEmptyElement("where");
        inOrder.verify(delegate, times(2)).writeEndElement();
    }

    @Test
    public void testWriteChangeSetOpenTag() throws XMLStreamException {
        writer.writeChangeSetStartElement("table");

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeStartElement("changeSet");
        inOrder.verify(delegate).writeAttribute("id", "bogus");
        inOrder.verify(delegate).writeAttribute("author", "test");
    }

    // ==================================================================================================================
    // Tests for writing column values
    // ==================================================================================================================

    @Test
    public void testWriteString() throws XMLStreamException {
        writer.writeColumn("test", "abc");
        verify(xmlEncoder).encode("abc");
        verify(delegate).writeStartElement(COLUMN);
        verify(delegate).writeAttribute(COLUMN_NAME, "test");
        verify(delegate).writeAttribute(COLUMN_TYPE, ColumnSerialisationType.CHARACTER.toString());
        verify(delegate).writeCData(ENCODED);
        verify(delegate).writeEndElement();
    }

    @Test
    public void testWriteBooleanFalse() throws XMLStreamException {
        writer.writeColumn("test", false);
        verify(xmlEncoder).encode("false");
        verify(delegate).writeStartElement(COLUMN);
        verify(delegate).writeAttribute(COLUMN_NAME, "test");
        verify(delegate).writeAttribute(COLUMN_TYPE, ColumnSerialisationType.BOOLEAN.toString());
        verify(delegate).writeCData(ENCODED);
        verify(delegate).writeEndElement();
    }

    @Test
    public void testWriteBooleanTrue() throws XMLStreamException {
        writer.writeColumn("test", true);
        verify(xmlEncoder).encode("true");
        verify(delegate).writeStartElement(COLUMN);
        verify(delegate).writeAttribute(COLUMN_NAME, "test");
        verify(delegate).writeAttribute(COLUMN_TYPE, ColumnSerialisationType.BOOLEAN.toString());
        verify(delegate).writeCData(ENCODED);
        verify(delegate).writeEndElement();
    }

    @Test
    public void testWriteDate() throws XMLStreamException {
        final DateTime dateTime = new DateTime(DateTimeZone.UTC).withDate(2012, 8, 3).withTime(12, 0, 0, 0);
        final java.sql.Date sqlDate = new java.sql.Date(dateTime.getMillis());

        writer.writeColumn("test", sqlDate);
        verify(xmlEncoder).encode("2012-08-03");
        verify(delegate).writeStartElement(COLUMN);
        verify(delegate).writeAttribute(COLUMN_NAME, "test");
        verify(delegate).writeAttribute(COLUMN_TYPE, ColumnSerialisationType.DATE.toString());
        verify(delegate).writeCData(ENCODED);
        verify(delegate).writeEndElement();
    }

    @Test
    public void testWriteInteger() throws XMLStreamException {
        writer.writeColumn("test", 8475);
        verify(xmlEncoder).encode("8475");
        verify(delegate).writeStartElement(COLUMN);
        verify(delegate).writeAttribute(COLUMN_NAME, "test");
        verify(delegate).writeAttribute(COLUMN_TYPE, ColumnSerialisationType.INTEGER.toString());
        verify(delegate).writeCData(ENCODED);
        verify(delegate).writeEndElement();
    }

    @Test
    public void testWriteNumber() throws XMLStreamException {
        writer.writeColumn("test", 8475.0f);
        verify(xmlEncoder).encode("8475.0");
        verify(delegate).writeStartElement(COLUMN);
        verify(delegate).writeAttribute(COLUMN_NAME, "test");
        verify(delegate).writeAttribute(COLUMN_TYPE, ColumnSerialisationType.NUMERIC.toString());
        verify(delegate).writeCData(ENCODED);
        verify(delegate).writeEndElement();
    }
}
