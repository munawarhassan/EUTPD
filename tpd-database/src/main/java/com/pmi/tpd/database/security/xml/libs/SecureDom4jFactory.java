package com.pmi.tpd.database.security.xml.libs;

import org.dom4j.io.SAXReader;

import com.pmi.tpd.database.security.xml.SecureXmlParserFactory;

/**
 * A class with a utility method to produce a <a href='http://en.wikipedia.org/wiki/Dom4j'>dom4j</a> parser suitable for
 * untrusted XML.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SecureDom4jFactory {

    private SecureDom4jFactory() {
    }

    /**
     * @return Create a dom4j {@link SAXReader} using {@link SecureXmlParserFactory}, suitable for parsing XML from an
     *         untrusted source.
     */
    public static SAXReader newSaxReader() {
        final SAXReader saxReader = new SAXReader(SecureXmlParserFactory.newXmlReader());
        return saxReader;
    }
}
