package com.pmi.tpd.database.security.xml.libs;

import org.apache.commons.digester.Digester;

import com.pmi.tpd.database.security.xml.SecureXmlParserFactory;

/**
 * A class with a utility method to produce a <a href='http://commons.apache.org/digester/'>Commons Digester</a> parser
 * suitable for untrusted XML.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public final class SecureDigesterFactory {

    private SecureDigesterFactory() {
    }

    /**
     * Create a new {@link Digester} using {@link SecureXmlParserFactory}, suitable for parsing XML from an untrusted
     * source.
     */
    public static Digester newDigester() {
        final Digester digester = new Digester(SecureXmlParserFactory.newXmlReader());
        return digester;
    }
}
