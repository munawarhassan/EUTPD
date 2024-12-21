package com.pmi.tpd.database.security.xml.libs;

import org.jdom2.input.SAXBuilder;
import org.xml.sax.XMLReader;

import com.pmi.tpd.database.security.xml.SecureXmlParserFactory;

/**
 * A class with a utility method to produce a <a href='http://www.jdom.org/'>JDOM</a> parser suitable for untrusted XML.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SecureJdomFactory {

    private SecureJdomFactory() {
    }

    /**
     * @return Create a JDOM {@link SAXBuilder} using {@link SecureXmlParserFactory}, suitable for parsing XML from an
     *         untrusted source.
     */
    public static SAXBuilder newSaxBuilder() {
        return new SAXBuilder() {

            @Override
            protected XMLReader createParser() {
                return SecureXmlParserFactory.newNamespaceAwareXmlReader();
            }
        };
    }
}
