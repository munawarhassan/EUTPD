package com.pmi.tpd.core.euceg;

import org.eu.ceg.Company;
import org.eu.ceg.CountryValue;
import org.eu.ceg.Submitter;
import org.eu.ceg.Submitter.Affiliates;
import org.eu.ceg.SubmitterDetails;
import org.eu.ceg.SubmitterType;

import com.pmi.tpd.core.model.euceg.SubmitterEntity;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class SubmitterHelper {

    private SubmitterHelper() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    private static SubmitterDetails okSubmitterDetail() {
        return new SubmitterDetails().withAddress("123 avenue de Lausanne")
                .withCountry(CountryValue.CH)
                .withEmail("contact@submitter.com")
                .withHasVatNumber(true)
                .withName("Submitter Detail")
                .withPhoneNumber("+41 6 02 65 84 41")
                .withSme(false)
                .withVatNumber("CH12345678941");

    }

    public static Submitter okSubmitter() {
        return new Submitter().withConfidential(false)
                .withSubmitterID("12345")
                .withSubmitterType(SubmitterType.MANUFACTURER)
                .withHasParent(false)
                .withHasAffiliates(true)
                .withAffiliates(new Affiliates().withAffiliate(okAffiliate()))
                .withHasEnterer(false);

    }

    public static Submitter okSimpleSubmitter() {
        return new Submitter().withConfidential(false)
                .withSubmitterID("99962")
                .withSubmitterType(SubmitterType.MANUFACTURER)
                .withHasParent(false)
                .withHasAffiliates(false)
                .withHasEnterer(false);

    }

    public static Company okAffiliate() {
        return new Company().withSubmitterID("00002")
                .withName("Affiliate")
                .withAddress("1 John Street")
                .withCountry(CountryValue.US)
                .withPhoneNumber("+1252123456")
                .withEmail("affilliates@us.com")
                .withConfidential(false);
    }

    public static Company okCompany() {
        return new Company().withSubmitterID("00001")
                .withName("Tobacco Company")
                .withAddress("1001 avenue de Lausanne")
                .withCountry(CountryValue.CH)
                .withPhoneNumber("+41 52 12 456")
                .withEmail("company@world.com")
                .withConfidential(false);

    }

    public static SubmitterEntity okSubmitterEntity() {
        final SubmitterDetails details = okSubmitterDetail();
        final Submitter submitter = okSubmitter();
        return SubmitterEntity.builder()
                .submitter(submitter)
                .details(details)
                .name(details.getName())
                .submitterId(submitter.getSubmitterID())
                .build();

    }

    public static SubmitterEntity fullSubmitterEntity() {
        final Submitter submitter = okSubmitter().withEnterer(okCompany().withName("Enterer Company"))
                .withHasEnterer(true)
                .withParent(okCompany().withName("Parent Company"))
                .withHasParent(true)
                .withHasAffiliates(true)
                .withAffiliates(new Affiliates().withAffiliate(okAffiliate().withName("Affiliate 1"),
                    okAffiliate().withName("affiliate 2")));
        final SubmitterDetails details = okSubmitterDetail();
        submitter.withSubmitterID("00001");
        details.withName("Full Submitter");
        return SubmitterEntity.builder()
                .submitter(submitter)
                .details(details)
                .name(details.getName())
                .submitterId(submitter.getSubmitterID())
                .build();
    }

}
