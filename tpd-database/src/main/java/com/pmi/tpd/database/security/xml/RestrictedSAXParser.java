package com.pmi.tpd.database.security.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.validation.Schema;

import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAXParser which uses a secure (empty) entity resolver, delegates down to the usual SAXParser.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
@SuppressWarnings("deprecation")
class RestrictedSAXParser extends SAXParser {

    /** */
    private final SAXParser delegate;

    /**
     * @param inner
     */
    RestrictedSAXParser(final SAXParser inner) {
        delegate = inner;
    }

    @Override
    public Parser getParser() throws SAXException {
        return delegate.getParser();
    }

    @Override
    public XMLReader getXMLReader() throws SAXException {
        final XMLReader innerReader = delegate.getXMLReader();
        innerReader.setEntityResolver(SecureXmlParserFactory.emptyEntityResolver());
        return new RestrictedXMLReader(innerReader);
    }

    @Override
    public boolean isNamespaceAware() {
        return delegate.isNamespaceAware();
    }

    @Override
    public boolean isValidating() {
        return delegate.isValidating();
    }

    @Override
    public void setProperty(final String name, final Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        delegate.setProperty(name, value);
    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return delegate.getProperty(name);
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void parse(final InputStream is, final HandlerBase hb) throws SAXException, IOException {
        delegate.parse(is, hb);
    }

    @Override
    public void parse(final InputStream is, final HandlerBase hb, final String systemId)
            throws SAXException, IOException {
        delegate.parse(is, hb, systemId);
    }

    @Override
    public void parse(final InputStream is, final DefaultHandler dh) throws SAXException, IOException {
        delegate.parse(is, dh);
    }

    @Override
    public void parse(final InputStream is, final DefaultHandler dh, final String systemId)
            throws SAXException, IOException {
        delegate.parse(is, dh, systemId);
    }

    @Override
    public void parse(final String uri, final HandlerBase hb) throws SAXException, IOException {
        delegate.parse(uri, hb);
    }

    @Override
    public void parse(final String uri, final DefaultHandler dh) throws SAXException, IOException {
        delegate.parse(uri, dh);
    }

    @Override
    public void parse(final File f, final HandlerBase hb) throws SAXException, IOException {
        delegate.parse(f, hb);
    }

    @Override
    public void parse(final File f, final DefaultHandler dh) throws SAXException, IOException {
        delegate.parse(f, dh);
    }

    @Override
    public void parse(final InputSource is, final HandlerBase hb) throws SAXException, IOException {
        delegate.parse(is, hb);
    }

    @Override
    public void parse(final InputSource is, final DefaultHandler dh) throws SAXException, IOException {
        delegate.parse(is, dh);
    }

    @Override
    public Schema getSchema() {
        return delegate.getSchema();
    }

    @Override
    public boolean isXIncludeAware() {
        return delegate.isXIncludeAware();
    }
}
