package com.pmi.tpd.database.liquibase.backup.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.pmi.tpd.testing.junit5.MockitoTestCase;

/**
 * Tests the operation of the {@link PrettyXmlWriter} class.
 */
public class PrettyXmlWriterTest extends MockitoTestCase {

    @Mock
    private XMLStreamWriter delegate;

    // =================================================================================================================
    // Tests for correct incrementation and decrementation of indent level.
    // =================================================================================================================

    @Test
    public void indentZeroAfterConstruction() throws XMLStreamException {
        assertEquals(0, new PrettyXmlWriter(delegate).getIndentLevel());
    }

    @Test
    public void firstStartElementIncrementsIndentLevel() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.writeStartElement("foo");
        assertEquals(1, writer.getIndentLevel());
    }

    @Test
    public void subsequentStartElementIncrementsIndentLevel() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.writeStartElement("foo");
        writer.writeStartElement("bar");
        assertEquals(2, writer.getIndentLevel());
    }

    @Test
    public void endElementDecrementsIndentLevel() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.setIndentLevel(2);
        writer.writeEndElement();
        assertEquals(1, writer.getIndentLevel());
    }

    @Test
    public void emptyElementMaintainsIndentLevel() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.setIndentLevel(2);
        writer.writeEmptyElement("foo");
        assertEquals(2, writer.getIndentLevel());
    }

    @Test
    public void noDecrementingIndentLevelToNegative() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.writeEndElement();
        assertEquals(0, writer.getIndentLevel());
    }

    // =================================================================================================================
    // Tests for formatting of start elements at various indent levels
    // =================================================================================================================

    @Test
    public void formatStartElementLevelZero() throws XMLStreamException {
        new PrettyXmlWriter(delegate).writeStartElement("foo");
        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeCharacters("\n");
        inOrder.verify(delegate).writeStartElement("foo");
    }

    @Test
    public void formatStartElementLevelOne() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.setIndentLevel(1);
        writer.writeStartElement("foo");

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeCharacters("\n    ");
        inOrder.verify(delegate).writeStartElement("foo");
    }

    @Test
    public void formatStartElementLevelTwo() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.setIndentLevel(2);
        writer.writeStartElement("foo");

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeCharacters("\n        ");
        inOrder.verify(delegate).writeStartElement("foo");
    }

    // =================================================================================================================
    // Tests for formatting of end elements at various indent levels
    // =================================================================================================================

    @Test
    public void formatEndElementLevelZero() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.writeStartElement("foo");
        writer.writeEndElement();

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeCharacters("\n");
        inOrder.verify(delegate).writeStartElement("foo");
        inOrder.verify(delegate).writeEndElement(); // no preceding line break or indent
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void formatEndElementLevelOne() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.writeStartElement("foo");
        writer.writeStartElement("bar");
        writer.writeEndElement();
        writer.writeEndElement();

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeCharacters("\n");
        inOrder.verify(delegate).writeStartElement("foo");
        inOrder.verify(delegate).writeCharacters("\n    ");
        inOrder.verify(delegate).writeStartElement("bar");
        inOrder.verify(delegate).writeEndElement(); // no preceding line break or indent
        inOrder.verify(delegate).writeCharacters("\n");
        inOrder.verify(delegate).writeEndElement();
        verifyNoMoreInteractions(delegate);
    }

    // =================================================================================================================
    // Tests for formatting of empty elements
    // =================================================================================================================

    @Test
    public void formatEmptyElement() throws XMLStreamException {
        final PrettyXmlWriter writer = new PrettyXmlWriter(delegate);
        writer.setIndentLevel(2);
        writer.writeEmptyElement("foo");

        final InOrder inOrder = inOrder(delegate);
        inOrder.verify(delegate).writeCharacters("\n        ");
        inOrder.verify(delegate).writeEmptyElement("foo");
    }

}
