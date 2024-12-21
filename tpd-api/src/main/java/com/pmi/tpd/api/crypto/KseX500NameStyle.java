package com.pmi.tpd.api.crypto;

import java.util.Hashtable;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;

/**
 * X.500 name style. Supports the same DN components as the BC versions but implements the reverse ordering of DN
 * components as the RFC 4519 style.
 */
public final class KseX500NameStyle extends BCStyle {

    /** */
    public static final KseX500NameStyle INSTANCE = new KseX500NameStyle();

    /** */
    private static final ASN1ObjectIdentifier DNQ = new ASN1ObjectIdentifier("2.5.4.46");

    /** */
    private static final Hashtable<ASN1ObjectIdentifier, String> DEFAULT_SYMBOLS = new Hashtable<>();

    static {
        DEFAULT_SYMBOLS.put(C, "C");
        DEFAULT_SYMBOLS.put(O, "O");
        DEFAULT_SYMBOLS.put(T, "T");
        DEFAULT_SYMBOLS.put(OU, "OU");
        DEFAULT_SYMBOLS.put(CN, "CN");
        DEFAULT_SYMBOLS.put(L, "L");
        DEFAULT_SYMBOLS.put(ST, "ST");
        DEFAULT_SYMBOLS.put(SERIALNUMBER, "SERIALNUMBER");
        DEFAULT_SYMBOLS.put(EmailAddress, "E");
        DEFAULT_SYMBOLS.put(DC, "DC");
        DEFAULT_SYMBOLS.put(UID, "UID");
        DEFAULT_SYMBOLS.put(STREET, "STREET");
        DEFAULT_SYMBOLS.put(SURNAME, "SURNAME");
        DEFAULT_SYMBOLS.put(GIVENNAME, "GIVENNAME");
        DEFAULT_SYMBOLS.put(INITIALS, "INITIALS");
        DEFAULT_SYMBOLS.put(GENERATION, "GENERATION");
        DEFAULT_SYMBOLS.put(UnstructuredAddress, "unstructuredAddress");
        DEFAULT_SYMBOLS.put(UnstructuredName, "unstructuredName");
        DEFAULT_SYMBOLS.put(UNIQUE_IDENTIFIER, "UniqueIdentifier");
        DEFAULT_SYMBOLS.put(DN_QUALIFIER, "DN");
        DEFAULT_SYMBOLS.put(PSEUDONYM, "Pseudonym");
        DEFAULT_SYMBOLS.put(POSTAL_ADDRESS, "PostalAddress");
        DEFAULT_SYMBOLS.put(NAME_AT_BIRTH, "NameAtBirth");
        DEFAULT_SYMBOLS.put(COUNTRY_OF_CITIZENSHIP, "CountryOfCitizenship");
        DEFAULT_SYMBOLS.put(COUNTRY_OF_RESIDENCE, "CountryOfResidence");
        DEFAULT_SYMBOLS.put(GENDER, "Gender");
        DEFAULT_SYMBOLS.put(PLACE_OF_BIRTH, "PlaceOfBirth");
        DEFAULT_SYMBOLS.put(DATE_OF_BIRTH, "DateOfBirth");
        DEFAULT_SYMBOLS.put(POSTAL_CODE, "PostalCode");
        DEFAULT_SYMBOLS.put(BUSINESS_CATEGORY, "BusinessCategory");
        DEFAULT_SYMBOLS.put(TELEPHONE_NUMBER, "TelephoneNumber");
        DEFAULT_SYMBOLS.put(NAME, "Name");
    }

    private KseX500NameStyle() {
    }

    @Override
    public ASN1ObjectIdentifier attrNameToOID(final String attrName) {
        // Add support for 'DNQ', BCStyle only supports 'DN'
        if (attrName.equalsIgnoreCase("DNQ")) {
            return DNQ;
        }

        return super.attrNameToOID(attrName);
    }

    @Override
    public RDN[] fromString(final String name) {
        // Parse backwards
        final RDN[] tmp = IETFUtils.rDNsFromString(name, this);
        final RDN[] res = new RDN[tmp.length];

        for (int i = 0; i != tmp.length; i++) {
            res[res.length - i - 1] = tmp[i];
        }

        return res;
    }

    @Override
    public String toString(final X500Name name) {
        // Convert in reverse
        final StringBuffer buf = new StringBuffer();
        boolean first = true;

        final RDN[] rdns = name.getRDNs();

        for (int i = rdns.length - 1; i >= 0; i--) {
            if (first) {
                first = false;
            } else {
                buf.append(',');
            }

            if (rdns[i].isMultiValued()) {
                final AttributeTypeAndValue[] atv = rdns[i].getTypesAndValues();
                boolean firstAtv = true;

                for (int j = 0; j != atv.length; j++) {
                    if (firstAtv) {
                        firstAtv = false;
                    } else {
                        buf.append('+');
                    }

                    IETFUtils.appendTypeAndValue(buf, atv[j], DEFAULT_SYMBOLS);
                }
            } else {
                IETFUtils.appendTypeAndValue(buf, rdns[i].getFirst(), DEFAULT_SYMBOLS);
            }
        }

        return buf.toString();
    }
}
