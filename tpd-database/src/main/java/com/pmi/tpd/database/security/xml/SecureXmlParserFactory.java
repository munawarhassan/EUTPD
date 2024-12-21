package com.pmi.tpd.database.security.xml;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.stream.XMLInputFactory.newInstance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * <p>
 * Utility methods to produce parsers suitable for untrusted XML. These cover the core parsing APIs included in JDK 1.6.
 * Other factories in {@link com.atlassian.security.xml.libs} can create parsers for other libraries.
 * </p>
 * <p/>
 * <p>
 * Parsers will have FEATURE_SECURE_PROCESSING enabled and be configured to ignore external resources used for, or in,
 * DTDs.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SecureXmlParserFactory {

    // CHECKSTYLE:OFF
    /** */
    private static InputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    /** */
    public static final String ATTRIBUTE_LOAD_EXTERNAL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /** */
    public static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    /** */
    public static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

    // CHECKSTYLE:ON
    /** */
    private static final List<String> PROTECTED_FEATURES = Arrays.asList(FEATURE_SECURE_PROCESSING,
        FEATURE_EXTERNAL_GENERAL_ENTITIES,
        FEATURE_EXTERNAL_PARAMETER_ENTITIES);

    /** */
    private static final List<String> PROTECTED_ATTRIBUTES = Arrays.asList(ATTRIBUTE_LOAD_EXTERNAL);

    /** */
    private static EntityResolver emptyEntityResolver = new EntityResolver() {

        @Override
        public InputSource resolveEntity(final String publicId, final String systemId)
                throws SAXException, IOException {
            return new InputSource(EMPTY_INPUT_STREAM);
        }
    };

    /** */
    private static final XMLResolver EMPTY_XML_RESOLVER = new XMLResolver() {

        @Override
        public Object resolveEntity(final String publicID,
            final String systemID,
            final String baseURI,
            final String namespace) {
            return EMPTY_INPUT_STREAM;
        }
    };

    private SecureXmlParserFactory() {
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory() throws ParserConfigurationException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setNamespaceAware(false);

        // Only necessary for bundled non-JDK Xerces
        dbf.setFeature(FEATURE_SECURE_PROCESSING, true);

        dbf.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
        dbf.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);

        dbf.setAttribute(ATTRIBUTE_LOAD_EXTERNAL, false);

        return dbf;
    }

    /**
     * @return A locked-down DocumentBuilderFactory
     * @since 3.1.2
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        try {
            final DocumentBuilderFactory dbf = createDocumentBuilderFactory();
            return new DocumentBuilderFactory() {

                @Override
                public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
                    return dbf.newDocumentBuilder();
                }

                @Override
                public void setAttribute(final String name, final Object value) throws IllegalArgumentException {
                    if (PROTECTED_ATTRIBUTES.contains(name)) {
                        return; // Permission denied. No Soup For You!
                    } else {
                        dbf.setAttribute(name, value);
                    }
                }

                @Override
                public Object getAttribute(final String name) throws IllegalArgumentException {
                    return dbf.getAttribute(name);
                }

                @Override
                public void setFeature(final String name, final boolean value) throws ParserConfigurationException {
                    if (PROTECTED_FEATURES.contains(name)) {
                        return; // Permission denied. No Soup For You!
                    } else {
                        dbf.setAttribute(name, value);
                    }
                }

                @Override
                public boolean getFeature(final String name) throws ParserConfigurationException {
                    return dbf.getFeature(name);
                }

                @Override
                public void setExpandEntityReferences(final boolean expandEntityRef) {
                    // ? Permission denied. No Soup For You!
                }

                @Override
                public boolean isNamespaceAware() {
                    return dbf.isNamespaceAware();
                }

                @Override
                public void setNamespaceAware(final boolean isNamespaceAware) {
                    dbf.setNamespaceAware(isNamespaceAware);
                }
            };
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new DOM {@link DocumentBuilder} suitable for parsing XML from an untrusted source.
     *
     * @return a new parser
     */
    public static DocumentBuilder newDocumentBuilder() {
        try {
            final DocumentBuilderFactory dbf = createDocumentBuilderFactory();
            dbf.setNamespaceAware(false);
            return dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new SAX {@link SAXParserFactory} suitable for parsing XML from an untrusted source
     *
     * @return a new {@link SAXParserFactory}
     */
    public static SAXParserFactory createSAXParserFactory() throws SAXException, ParserConfigurationException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setFeature(FEATURE_SECURE_PROCESSING, true);
        spf.setFeature(ATTRIBUTE_LOAD_EXTERNAL, false);
        spf.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
        spf.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);

        return new RestrictedSAXParserFactory(spf);
    }

    /**
     * Create a new SAX {@link XMLReader} suitable for parsing XML from an untrusted source.
     *
     * @return a new parser
     */
    public static XMLReader newXmlReader() {
        try {
            final SAXParserFactory spf = createSAXParserFactory();
            spf.setNamespaceAware(false);
            return spf.newSAXParser().getXMLReader();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (final SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new namespace-aware SAX {@link XMLReader} suitable for parsing XML from an untrusted source.
     *
     * @return a new parser
     */
    public static XMLReader newNamespaceAwareXmlReader() {
        try {
            final SAXParserFactory spf = createSAXParserFactory();
            spf.setNamespaceAware(true);
            return spf.newSAXParser().getXMLReader();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (final SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new namespace-aware DOM {@link DocumentBuilder} suitable for parsing XML from an untrusted source.
     *
     * @return a new parser
     */
    public static DocumentBuilder newNamespaceAwareDocumentBuilder() {
        try {
            final DocumentBuilderFactory dbf = createDocumentBuilderFactory();
            dbf.setNamespaceAware(true);
            return dbf.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new StAX {@link XMLInputFactory} suitable for parsing XML from an untrusted source.
     *
     * @return a new parser
     */
    public static XMLInputFactory newXmlInputFactory() {
        final XMLInputFactory fac = newInstance();
        fac.setProperty(SUPPORT_DTD, Boolean.FALSE);
        fac.setXMLResolver(EMPTY_XML_RESOLVER);
        return fac;
    }

    /**
     * Create a new {@link EntityResolver} that will resolve every entity to an empty stream, rather than fetching
     * resources from the network.
     */
    public static EntityResolver emptyEntityResolver() {
        return emptyEntityResolver;
    }
}
