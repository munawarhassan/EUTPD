package com.pmi.tpd.database.liquibase.backup.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

/**
 * Makes XML output readable by humans by inserting newlines and indentation spaces to produce an XML document in a
 * nicely formatted style.
 * 
 * @author Christophe Friederich
 * @since 1.3
 */
public class PrettyXmlWriter extends DelegatingXmlStreamWriter<XMLStreamWriter> {

    /** */
    private static final int INDENT_SIZE = 4;

    /**
     * How far from the left margin we are indenting.
     * <p>
     * This value starts at zero, meaning on the left margin, and is incremented and decremented as elements are opened
     * and closed.
     */
    private int indentLevel;

    /** */
    private boolean previousElementClosed = true;

    /** */
    public PrettyXmlWriter(final XMLStreamWriter delegate) {
        super(delegate);
    }

    public void setIndentLevel(final int level) {
        Preconditions.checkArgument(level > -1);
        this.indentLevel = level;
    }

    public int getIndentLevel() {
        return indentLevel;
    }

    @Override
    public void writeStartElement(final String s) throws XMLStreamException {
        writeIndentationShim();
        super.writeStartElement(s);
        indentLevel++;
        previousElementClosed = false;
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        indentLevel = Math.max(0, indentLevel - 1);
        // Only begin on a new line with indenting if we've
        // just closed another element; otherwise, just close the current element
        // on the same line.
        if (previousElementClosed) {
            writeIndentationShim();
        }
        super.writeEndElement();
        previousElementClosed = true;
    }

    @Override
    public void writeEmptyElement(final String s) throws XMLStreamException {
        writeIndentationShim();
        super.writeEmptyElement(s);
        previousElementClosed = true;
    }

    private void writeIndentationShim() throws XMLStreamException {
        writeCharacters(StringUtils.rightPad("\n", INDENT_SIZE * indentLevel + 1));
    }
}
