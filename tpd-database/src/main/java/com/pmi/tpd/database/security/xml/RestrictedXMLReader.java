package com.pmi.tpd.database.security.xml;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * XMLReader which does not allow the entity resolver to be set, delegates down to the usual XMLReader.
 *
 * @author Christophe Friederich
 * @since 1.3
 */

class RestrictedXMLReader implements XMLReader {

    /** */
    private final XMLReader delegate;

    RestrictedXMLReader(final XMLReader innerReader) {
        this.delegate = innerReader;
    }

    @Override
    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return delegate.getFeature(name);
    }

    @Override
    public void setFeature(final String name, final boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        if (RestrictedSAXParserFactory.checkFeatures(name, value)) {
            delegate.setFeature(name, value);
        }
    }

    @Override
    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return delegate.getProperty(name);
    }

    @Override
    public void setProperty(final String name, final Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
        delegate.setProperty(name, value);
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        // silenty fail -- there are lots of users who try to reset the stream e.g. Digester.class
    }

    @Override
    public EntityResolver getEntityResolver() {
        return delegate.getEntityResolver();
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
        delegate.setDTDHandler(handler);
    }

    @Override
    public DTDHandler getDTDHandler() {
        return delegate.getDTDHandler();
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        delegate.setContentHandler(handler);
    }

    @Override
    public ContentHandler getContentHandler() {
        return delegate.getContentHandler();
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
        delegate.setErrorHandler(handler);
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return delegate.getErrorHandler();
    }

    @Override
    public void parse(final InputSource input) throws IOException, SAXException {
        delegate.parse(input);
    }

    @Override
    public void parse(final String systemId) throws IOException, SAXException {
        delegate.parse(systemId);
    }
}
