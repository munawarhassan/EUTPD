package com.pmi.tpd.api.crypto;

import java.io.IOException;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;

/**
 * Utility class that handles distinguished names.
 */
public final class X500NameHelper {

    private X500NameHelper() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * Convert an X.500 Principal to an X.500 Name.
     *
     * @param principal
     *            X.500 Principal
     * @return X.500 Name
     */
    public static X500Name x500PrincipalToX500Name(final X500Principal principal) {
        return X500Name.getInstance(KseX500NameStyle.INSTANCE, principal.getEncoded());
    }

    /**
     * Convert an X.500 Name to an X.500 Principal.
     *
     * @param name
     *            X.500 Name
     * @return X.500 Principal
     * @throws IOException
     *             if an encoding error occurs (incorrect form for DN)
     */
    public static X500Principal x500NameToX500Principal(final X500Name name) throws IOException {
        return new X500Principal(name.getEncoded());
    }

    /**
     * Returns the (first) value of the (first) RDN of type rdnOid
     *
     * @param dn
     *            The X500Name
     * @param rdnOid
     *            OID of wanted RDN
     * @return Value of requested RDN
     */
    public static String getRdn(final X500Name dn, final ASN1ObjectIdentifier rdnOid) {

        if (dn == null || rdnOid == null) {
            return "";
        }

        final RDN[] rdns = dn.getRDNs(rdnOid);
        String value = "";

        if (rdns.length > 0) {
            final RDN rdn = rdns[0];
            value = rdn.getFirst().getValue().toString();
        }

        return value;
    }

    /**
     * Creates an X500Name object from the given components.
     *
     * @param commonName
     * @param organisationUnit
     * @param organisationName
     * @param localityName
     * @param stateName
     * @param countryCode
     * @param emailAddress
     * @return X500Name object from the given components
     */
    public static X500Name buildX500Name(final String commonName,
        final String organisationUnit,
        final String organisationName,
        final String localityName,
        final String stateName,
        final String countryCode,
        final String emailAddress) {

        final X500NameBuilder x500NameBuilder = new X500NameBuilder(KseX500NameStyle.INSTANCE);

        if (emailAddress != null) {
            x500NameBuilder.addRDN(BCStyle.E, emailAddress);
        }
        if (countryCode != null) {
            x500NameBuilder.addRDN(BCStyle.C, countryCode);
        }
        if (stateName != null) {
            x500NameBuilder.addRDN(BCStyle.ST, stateName);
        }
        if (localityName != null) {
            x500NameBuilder.addRDN(BCStyle.L, localityName);
        }
        if (organisationName != null) {
            x500NameBuilder.addRDN(BCStyle.O, organisationName);
        }
        if (organisationUnit != null) {
            x500NameBuilder.addRDN(BCStyle.OU, organisationUnit);
        }
        if (commonName != null) {
            x500NameBuilder.addRDN(BCStyle.CN, commonName);
        }

        return x500NameBuilder.build();
    }
}
