package com.pmi.tpd.database.security.xml.libs;

import com.pmi.tpd.database.security.xml.SecureXmlParserFactory;

import nu.xom.Builder;

/**
 * A class with a utility method to produce a <a href='http://www.xom.nu/'>XOM</a> parser suitable for untrusted XML.
 *
 * @since 3.0
 */
public final class SecureXomFactory {

    private SecureXomFactory() {
    }

    /**
     * @return Create a new XOM {@link Builder} using {@link SecureXmlParserFactory}, suitable for parsing XML from an
     *         untrusted source.
     */
    public static Builder newBuilder() {
        return new Builder(SecureXmlParserFactory.newXmlReader());
    }
}
