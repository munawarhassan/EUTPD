package com.pmi.tpd.core.elasticsearch.parser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import org.junit.jupiter.api.Test;

import com.pmi.tpd.core.elasticsearch.model.PresentationIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.testing.junit5.TestCase;

public class ProductXmlParserTest extends TestCase {

    @Test
    public void parseXmlProduct() throws FileNotFoundException, XMLStreamException, IOException {

        final ProductIndexed.ProductIndexedBuilder indexedProduct = ProductIndexed.builder();
        ProductXmlParser.parse(new InputStreamReader(getResourceAsStream("xmlproduct.xml")),
            indexedProduct,
            ProductType.TOBACCO);

        final ProductIndexed product = indexedProduct.build();

        assertThat(product.getType(), equalTo(11));
        assertThat(product.getTypeName(), equalTo("Novel tobacco product"));
        final List<PresentationIndexed> presentations = product.getPresentations();
        assertEquals(2, presentations.size());
        assertThat(presentations.stream().map(PresentationIndexed::getNationalMarket).collect(Collectors.toList()),
            hasItems("FR", "GB"));
        assertThat(presentations.stream().map(PresentationIndexed::getNationalMarketName).collect(Collectors.toList()),
            hasItems("France", "United Kingdom"));
    }

}
