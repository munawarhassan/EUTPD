package com.pmi.tpd.api.util.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



/**
 * Delegating {@link XMLStreamWriter} that filters out UTF-8 characters that
 * are illegal in XML.
 *
 * @author Erik van Zijst (small change by Lennart Schedin)
 */
public class EscapingXMLStreamWriter implements XMLStreamWriter {

  private final XMLStreamWriter writer;
  public static final char SUBSTITUTE = '\uFFFD';

  public EscapingXMLStreamWriter(final XMLStreamWriter writer) {

    if (null == writer) {
      throw new IllegalArgumentException("null");
    } else {
      this.writer = writer;
    }
  }

  /**
   * Substitutes all illegal characters in the given string by the value of
   * {@link EscapingXMLStreamWriter#substitute}. If no illegal characters
   * were found, no copy is made and the given string is returned.
   *
   * @param string
   * @return
   */
  private String escapeCharacters(final String string) {

    char[] copy = null;
    boolean copied = false;
    for (int i = 0; i < string.length(); i++) {
      if (XMLChar.isInvalid(string.charAt(i))) {
        if (!copied) {
          copy = string.toCharArray();
          copied = true;
        }
        copy[i] = SUBSTITUTE;
      }
    }
    return copied ? new String(copy) : string;
  }

  public void writeStartElement(final String s) throws XMLStreamException {
    writer.writeStartElement(s);
  }

  public void writeStartElement(final String s, final String s1) throws XMLStreamException {
    writer.writeStartElement(s, s1);
  }

  public void writeStartElement(final String s, final String s1, final String s2)
      throws XMLStreamException {
    writer.writeStartElement(s, s1, s2);
  }

  public void writeEmptyElement(final String s, final String s1) throws XMLStreamException {
    writer.writeEmptyElement(s, s1);
  }

  public void writeEmptyElement(final String s, final String s1, final String s2)
      throws XMLStreamException {
    writer.writeEmptyElement(s, s1, s2);
  }

  public void writeEmptyElement(final String s) throws XMLStreamException {
    writer.writeEmptyElement(s);
  }

  public void writeEndElement() throws XMLStreamException {
    writer.writeEndElement();
  }

  public void writeEndDocument() throws XMLStreamException {
    writer.writeEndDocument();
  }

  public void close() throws XMLStreamException {
    writer.close();
  }

  public void flush() throws XMLStreamException {
    writer.flush();
  }

  public void writeAttribute(final String localName, final String value) throws XMLStreamException {
    writer.writeAttribute(localName, escapeCharacters(value));
  }

  public void writeAttribute(final String prefix, final String namespaceUri, final String localName, final String value)
      throws XMLStreamException {
    writer.writeAttribute(prefix, namespaceUri, localName, escapeCharacters(value));
  }

  public void writeAttribute(final String namespaceUri, final String localName, final String value)
      throws XMLStreamException {
    writer.writeAttribute(namespaceUri, localName, escapeCharacters(value));
  }

  public void writeNamespace(final String s, final String s1) throws XMLStreamException {
    writer.writeNamespace(s, s1);
  }

  public void writeDefaultNamespace(final String s) throws XMLStreamException {
    writer.writeDefaultNamespace(s);
  }

  public void writeComment(final String s) throws XMLStreamException {
    writer.writeComment(s);
  }

  public void writeProcessingInstruction(final String s) throws XMLStreamException {
    writer.writeProcessingInstruction(s);
  }

  public void writeProcessingInstruction(final String s, final String s1)
      throws XMLStreamException {
    writer.writeProcessingInstruction(s, s1);
  }

  public void writeCData(final String s) throws XMLStreamException {
    writer.writeCData(escapeCharacters(s));
  }

  public void writeDTD(final String s) throws XMLStreamException {
    writer.writeDTD(s);
  }

  public void writeEntityRef(final String s) throws XMLStreamException {
    writer.writeEntityRef(s);
  }

  public void writeStartDocument() throws XMLStreamException {
    writer.writeStartDocument();
  }

  public void writeStartDocument(final String s) throws XMLStreamException {
    writer.writeStartDocument(s);
  }

  public void writeStartDocument(final String s, final String s1)
      throws XMLStreamException {
    writer.writeStartDocument(s, s1);
  }

  public void writeCharacters(final String s) throws XMLStreamException {
    writer.writeCharacters(escapeCharacters(s));
  }

  public void writeCharacters(final char[] chars, final int start, final int len)
      throws XMLStreamException {
    writer.writeCharacters(escapeCharacters(new String(chars, start, len)));
  }

  public String getPrefix(final String s) throws XMLStreamException {
    return writer.getPrefix(s);
  }

  public void setPrefix(final String s, final String s1) throws XMLStreamException {
    writer.setPrefix(s, s1);
  }

  public void setDefaultNamespace(final String s) throws XMLStreamException {
    writer.setDefaultNamespace(s);
  }

  public void setNamespaceContext(final NamespaceContext namespaceContext)
      throws XMLStreamException {
    writer.setNamespaceContext(namespaceContext);
  }

  public NamespaceContext getNamespaceContext() {
    return writer.getNamespaceContext();
  }

  public Object getProperty(final String s) throws IllegalArgumentException {
    return writer.getProperty(s);
  }
}