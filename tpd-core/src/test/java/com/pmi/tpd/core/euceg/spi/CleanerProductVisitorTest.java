package com.pmi.tpd.core.euceg.spi;

import org.eu.ceg.CigaretteSpecific;
import org.eu.ceg.TobaccoProduct;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.core.euceg.spi.IProductStore.CleanerProductVisitor;
import com.pmi.tpd.testing.junit5.TestCase;

public class CleanerProductVisitorTest extends TestCase {

    @Test
    public void shouldReplaceEmptyElementByNull() {
        final CleanerProductVisitor visitor = new CleanerProductVisitor();
        final TobaccoProduct product = visitor
                .cleanEmptyValue(new TobaccoProduct().withCigaretteSpecific(new CigaretteSpecific())
                        .withDiameter(new org.eu.ceg.Double().withConfidential(false)));

        assertNull(product.getCigaretteSpecific());
        assertNull(product.getDiameter());
    }
}
