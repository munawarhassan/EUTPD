package com.pmi.tpd.database.security.xml;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * SAXParserFactory which does not allow certain features to be set, delegates down to the usual SAXParserFactory.
 *
 * @author Christophe Friederich
 * @since 1.3
 */

class RestrictedSAXParserFactory extends SAXParserFactory {

    /** */
    private final SAXParserFactory delegate;

    RestrictedSAXParserFactory(final SAXParserFactory inner) {
        delegate = inner;
    }

    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        final SAXParser innerParser = delegate.newSAXParser();
        return new RestrictedSAXParser(innerParser);
    }

    @Override
    public void setNamespaceAware(final boolean awareness) {
        delegate.setNamespaceAware(awareness);
    }

    @Override
    public void setValidating(final boolean validating) {
        delegate.setValidating(validating);
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
    public void setFeature(final String name, final boolean value)
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        if (checkFeatures(name, value)) {
            delegate.setFeature(name, value);
        }
    }

    static boolean checkFeatures(final String name, final boolean value) {
        if (name.equals(FEATURE_SECURE_PROCESSING) && !value) {
            return false;
        }
        if (name.equals(SecureXmlParserFactory.ATTRIBUTE_LOAD_EXTERNAL) && value) {
            return false;
        }

        if (name.equals(SecureXmlParserFactory.FEATURE_EXTERNAL_GENERAL_ENTITIES) && value) {
            return false;
        }
        if (name.equals(SecureXmlParserFactory.FEATURE_EXTERNAL_PARAMETER_ENTITIES) && value) {
            return false;
        }
        return true;
    }

    @Override
    public boolean getFeature(final String name)
            throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        return delegate.getFeature(name);
    }

    @Override
    public Schema getSchema() {
        return delegate.getSchema();
    }

    @Override
    public void setSchema(final Schema schema) {
        delegate.setSchema(schema);
    }

    @Override
    public void setXIncludeAware(final boolean state) {
        delegate.setXIncludeAware(state);
    }

    @Override
    public boolean isXIncludeAware() {
        return delegate.isXIncludeAware();
    }

}
