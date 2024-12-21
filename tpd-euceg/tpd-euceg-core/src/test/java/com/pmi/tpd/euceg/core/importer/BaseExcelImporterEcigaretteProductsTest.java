package com.pmi.tpd.euceg.core.importer;

import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;

import org.eu.ceg.EcigProduct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.excel.ColumnDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;
import com.pmi.tpd.testing.junit5.TestCase;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class BaseExcelImporterEcigaretteProductsTest extends TestCase {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseExcelImporterEcigaretteProductsTest.class);

    @BeforeEach
    public void forceIndent() throws JAXBException {
        Eucegs.indentMarshalling(true);
    }

    @AfterEach
    public void restoreIndentMarshalling() throws PropertyException {
        Eucegs.indentMarshalling(false);
    }

    @Test
    public void shouldEcigaretteGroupsHaveCorrectKeys() {
        final ListDescriptor root = EucegExcelSchema.EcigProduct.DESCRIPTORS;
        assertThat(transform(root.get("ProductDetail").orElseThrow().getForeignKeyColumns(), this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number"));

        assertThat(
            transform(root.get("ProductDetail").orElseThrow().getChildren("Manufacturer").getForeignKeyColumns(),
                this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number", "Manufacturer_ID", "Company_Name"));
        assertThat(
            transform(
                root.get("ProductDetail")
                        .orElseThrow()
                        .getChildren("Manufacturer")
                        .getChildren("ProductionSite")
                        .getForeignKeyColumns(),
                this::toColumnName),
            contains("Submitter_ID",
                "Internal_Product_Number",
                "Manufacturer_ID",
                "Company_Name",
                "Production_Site_Address"));

        assertThat(transform(root.get("Attachment").orElseThrow().getForeignKeyColumns(), this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number"));

        assertThat(transform(root.get("Presentation").orElseThrow().getForeignKeyColumns(), this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number", "Product_Submitter_Number", "Product_National_Market"));
        assertThat(
            transform(root.get("Presentation").orElseThrow().getChildren("SaleData").getForeignKeyColumns(),
                this::toColumnName),
            contains("Submitter_ID",
                "Internal_Product_Number",
                "Product_Submitter_Number",
                "Product_National_Market",
                "Product_Sales_Volume_Year"));

        assertThat(transform(root.get("Ingredient").orElseThrow().getForeignKeyColumns(), this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number", "Ingredient_Name"));

        assertThat(transform(root.get("Emission").orElseThrow().getForeignKeyColumns(), this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number"));

        assertThat(transform(root.get("Design").orElseThrow().getForeignKeyColumns(), this::toColumnName),
            contains("Submitter_ID", "Internal_Product_Number"));

    }

    private String toColumnName(final ColumnDescriptor<?> col) {
        return col.getName();
    }

    @Test
    public void importEcigaretteDisposable() throws IOException, Exception {
        approveExcel("ecig-product-submission-ecig-disposable.xls");
    }

    @Test
    public void importEcigaretteRechargeable() throws IOException, Exception {
        approveExcel("ecig-product-submission-ecig-rechargeable.xls");
    }

    @Test
    public void importEcigaretteRefillable() throws IOException, Exception {
        approveExcel("ecig-product-submission-ecig-refillable.xls");
    }

    @Test
    public void importEcigaretteRefillContainer() throws IOException, Exception {
        approveExcel("ecig-product-submission-ecig-refill-container.xls");
    }

    @Test
    public void importEcigaretteKit() throws IOException, Exception {
        approveExcel("ecig-product-submission-ecig-kit.xls");
    }

    @Test
    public void importEcigaretteIndividualPart() throws IOException, Exception {
        approveExcel("ecig-product-submission-ecig-individual-part.xls");
    }

    private void approveExcel(final String excelFile) throws Exception {

        try (InputStream in = getResourceAsStream(this.getClass(), excelFile)) {
            final IImporterResult<EucegProduct> result = new SimpleImporterEcigaretteProducts(new SimpleI18nService())
                    .importFromExcel(in, null);

            if (!result.getValidationResult().isEmpty()) {
                LOGGER.error(result.getValidationResult().toString());
                fail("the import has failed");
            }

            final List<EcigProduct> list = result.getResults()
                    .stream()
                    .map(p -> (EcigProduct) p.getProduct())
                    .collect(Collectors.toList());
            assertEquals(1, list.size());
            final EcigProduct product = Iterables.getFirst(list, null);
            final String xmlProduct = Eucegs.marshal(Eucegs.wrap(product));
            // Files.asCharSink(new File(excelFile + ".txt"), Charsets.UTF_8).write(xmlProduct);
            approve(xmlProduct);

        }
    }

    public static class SimpleImporterEcigaretteProducts extends BaseExcelImporterEcigaretteProducts {

        public SimpleImporterEcigaretteProducts(final I18nService i18nService) {
            super(i18nService);
        }

        @Override
        protected boolean submitterExists(final String submitterId) {
            return true;
        }

        @Override
        protected String findAttachmentIdByFilename(final String filename) {
            return filename;
        }

        @Override
        public EcigProduct getCurrentProduct(final String productNumber) {
            return null;
        }

    }

}
