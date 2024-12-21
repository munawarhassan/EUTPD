package com.pmi.tpd.database.liquibase.backup.xml;

import javax.annotation.Nonnull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author Christophe Friederich
 * @since 1.3
 * @param <T>
 */
public abstract class DelegatingXmlStreamWriter<T extends XMLStreamWriter> implements XMLStreamWriter {

  @Nonnull
  private final T delegate;

  protected DelegatingXmlStreamWriter(final T delegate) {
    this.delegate = delegate;
  }

  @Override
  public void writeStartElement(final String s) throws XMLStreamException {
    delegate.writeStartElement(s);
  }

  @Override
  public void writeStartElement(final String s, final String s1) throws XMLStreamException {
    delegate.writeStartElement(s, s1);
  }

  @Override
  public void writeStartElement(final String s, final String s1, final String s2) throws XMLStreamException {
    delegate.writeStartElement(s, s1, s2);
  }

  @Override
  public void writeEmptyElement(final String s, final String s1) throws XMLStreamException {
    delegate.writeEmptyElement(s, s1);
  }

  @Override
  public void writeEmptyElement(final String s, final String s1, final String s2) throws XMLStreamException {
    delegate.writeEmptyElement(s, s1, s2);
  }

  @Override
  public void writeEmptyElement(final String s) throws XMLStreamException {
    delegate.writeEmptyElement(s);
  }

  @Override
  public void writeEndElement() throws XMLStreamException {
    delegate.writeEndElement();
  }

  @Override
  public void writeEndDocument() throws XMLStreamException {
    delegate.writeEndDocument();
  }

  @Override
  public void close() throws XMLStreamException {
    delegate.close();
  }

  @Override
  public void flush() throws XMLStreamException {
    delegate.flush();
  }

  @Override
  public void writeAttribute(final String s, final String s1) throws XMLStreamException {
    delegate.writeAttribute(s, s1);
  }

  @Override
  public void writeAttribute(final String s, final String s1, final String s2, final String s3)
      throws XMLStreamException {
    delegate.writeAttribute(s, s1, s2, s3);
  }

  @Override
  public void writeAttribute(final String s, final String s1, final String s2) throws XMLStreamException {
    delegate.writeAttribute(s, s1, s2);
  }

  @Override
  public void writeNamespace(final String s, final String s1) throws XMLStreamException {
    delegate.writeNamespace(s, s1);
  }

  @Override
  public void writeDefaultNamespace(final String s) throws XMLStreamException {
    delegate.writeDefaultNamespace(s);
  }

  @Override
  public void writeComment(final String s) throws XMLStreamException {
    delegate.writeComment(s);
  }

  @Override
  public void writeProcessingInstruction(final String s) throws XMLStreamException {
    delegate.writeProcessingInstruction(s);
  }

  @Override
  public void writeProcessingInstruction(final String s, final String s1) throws XMLStreamException {
    delegate.writeProcessingInstruction(s, s1);
  }

  @Override
  public void writeCData(final String s) throws XMLStreamException {
    delegate.writeCData(s);
  }

  @Override
  public void writeDTD(final String s) throws XMLStreamException {
    delegate.writeDTD(s);
  }

  @Override
  public void writeEntityRef(final String s) throws XMLStreamException {
    delegate.writeEntityRef(s);
  }

  @Override
  public void writeStartDocument() throws XMLStreamException {
    delegate.writeStartDocument();
  }

  @Override
  public void writeStartDocument(final String s) throws XMLStreamException {
    delegate.writeStartDocument(s);
  }

  @Override
  public void writeStartDocument(final String s, final String s1) throws XMLStreamException {
    delegate.writeStartDocument(s, s1);
  }

  @Override
  public void writeCharacters(final String s) throws XMLStreamException {
    delegate.writeCharacters(s);
  }

  @Override
  public void writeCharacters(final char[] chars, final int i, final int i1) throws XMLStreamException {
    delegate.writeCharacters(chars, i, i1);
  }

  @Override
  public String getPrefix(final String s) throws XMLStreamException {
    return delegate.getPrefix(s);
  }

  @Override
  public void setPrefix(final String s, final String s1) throws XMLStreamException {
    delegate.setPrefix(s, s1);
  }

  @Override
  public void setDefaultNamespace(final String s) throws XMLStreamException {
    delegate.setDefaultNamespace(s);
  }

  @Override
  public void setNamespaceContext(final NamespaceContext namespaceContext) throws XMLStreamException {
    delegate.setNamespaceContext(namespaceContext);
  }

  @Override
  public NamespaceContext getNamespaceContext() {
    return delegate.getNamespaceContext();
  }

  @Override
  public Object getProperty(final String s) throws IllegalArgumentException {
    return delegate.getProperty(s);
  }

  protected T getDelegate() {
    return delegate;
  }
}
