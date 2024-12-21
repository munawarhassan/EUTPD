package com.pmi.tpd.euceg.core.diff;

import java.io.IOException;

import org.eu.ceg.Product;
import org.eu.ceg.TobaccoProduct;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.support.DiffChange;
import com.pmi.tpd.euceg.core.support.EucegXmlDiff;
import com.pmi.tpd.euceg.core.support.EucegXmlDiff.DiffResult;
import com.pmi.tpd.testing.junit5.TestCase;

public class EucegXmlDiffTest extends TestCase {

    Logger logger = LoggerFactory.getLogger(EucegXmlDiff.class);

    @Test
    public void shouldChangeStatusModified() throws IOException {
        final String productNumber = "POM.number";
        final TobaccoProduct original = new TobaccoProduct().withFilter(Eucegs.toBooleanNullable(false));
        final TobaccoProduct revised = new TobaccoProduct();
        original.copyTo(revised);
        original.withFilter(Eucegs.toBooleanNullable(true)).withFilterLength(Eucegs.toInteger(20));
        final DiffResult result = new EucegXmlDiff(productNumber, EucegXmlDiff.getXml(original, Product.class, true),
                EucegXmlDiff.getXml(revised, Product.class, true)).result();
        assertNotNull(result.getPatch());
        assertEquals(DiffChange.Modified, result.getChange());
        approve(result.getPatch());
    }

    @Test
    public void shouldChangeStatusModifiedWithNewNumber() throws IOException {
        final String productNumber = "POM.number";
        final String newProductNumber = "POM.newNumber";

        final TobaccoProduct original = new TobaccoProduct().withFilter(Eucegs.toBooleanNullable(false));
        final TobaccoProduct revised = new TobaccoProduct();
        original.copyTo(revised);
        original.withFilter(Eucegs.toBooleanNullable(true)).withFilterLength(Eucegs.toInteger(20));
        final DiffResult result = new EucegXmlDiff(productNumber, EucegXmlDiff.getXml(original, Product.class, true),
                EucegXmlDiff.getXml(revised, Product.class, true)).withRevisedId(newProductNumber).result();
        assertNotNull(result.getPatch());
        assertEquals(DiffChange.Modified, result.getChange());
        approve(result.getPatch());
    }

    @Test
    public void shouldChangeStatusUnchanged() throws IOException {
        final String productNumber = "POM.number";
        final TobaccoProduct original = new TobaccoProduct().withFilter(Eucegs.toBooleanNullable(false));
        final TobaccoProduct revised = new TobaccoProduct();
        original.copyTo(revised);
        final DiffResult result = new EucegXmlDiff(productNumber, EucegXmlDiff.getXml(original, Product.class, true),
                EucegXmlDiff.getXml(revised, Product.class, true)).result();
        assertNotNull(result.getPatch());
        assertEquals(DiffChange.Unchanged, result.getChange());
        approve(result.getPatch());
    }

    @Test
    public void shouldChangeStatusAdded() throws IOException {
        final String productNumber = "POM.number";
        final TobaccoProduct original = null;
        final TobaccoProduct revised = new TobaccoProduct().withFilter(Eucegs.toBooleanNullable(true))
                .withFilterLength(Eucegs.toInteger(20));
        final DiffResult result = new EucegXmlDiff(productNumber, EucegXmlDiff.getXml(original, Product.class, true),
                EucegXmlDiff.getXml(revised, Product.class, true)).result();
        assertNotNull(result.getPatch());
        assertEquals(DiffChange.Added, result.getChange());
        approve(result.getPatch());

    }
}
