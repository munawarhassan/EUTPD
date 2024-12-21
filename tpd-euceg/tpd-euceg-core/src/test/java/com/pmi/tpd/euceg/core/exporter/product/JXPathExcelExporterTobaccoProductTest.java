package com.pmi.tpd.euceg.core.exporter.product;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.eu.ceg.TobaccoProduct;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.core.BulkRequest;
import com.pmi.tpd.euceg.core.BulkRequest.BulkAction;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.importer.BaseExcelmporterTobaccoProducts;
import com.pmi.tpd.euceg.core.importer.IImporterResult;
import com.pmi.tpd.testing.junit5.TestCase;

public class JXPathExcelExporterTobaccoProductTest extends TestCase {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(JXPathExcelExporterTobaccoProductTest.class);

    public static Path targetFolder;

    @BeforeAll
    public static void start(@TempDir final Path tempDir) {
        targetFolder = tempDir;
    }

    @Test
    public void shouldntExportFailed() throws Throwable {
        final List<EucegProduct> list = importResourceExcel("tobacco-product-submission.xlsx");

        // Write the output to a file
        final String filePath = targetFolder.resolve("tobacco-product-submission-result.xlsx")
                .toFile()
                .getAbsolutePath();
        // final String filePath = "target/ExcelExporterTobaccoProductTest.xlsx";
        LOGGER.info("Write export tobacco product excel file: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);
        try {
            final EucegProduct item = list.get(0);
            final EucegProduct entity = EucegProduct.builder()
                    .product(item.getProduct())
                    .submitterId(item.getSubmitterId())
                    .productNumber(item.getProductNumber())
                    .preferredSubmissionType(item.getPreferredSubmissionType())
                    .previousProductNumber(item.getPreviousProductNumber())
                    .generalComment(item.getGeneralComment())
                    .build();
            createExporter(BulkRequest.builder().action(BulkAction.exportExcel).filters(Maps.newHashMap()).build(),
                entity).export(stream);
        } catch (final Exception e) {
            Closeables.close(stream, false);
            throw e;
        }

    }

    @Test
    public void shouldExportTncoEmission() throws Throwable {
        final List<EucegProduct> list = importResourceExcel("tobacco-product-no-other-emission.xlsx");

        // Write the output to a file
        final String filePath = targetFolder.resolve("tobacco-product-no-other-emission-result.xlsx")
                .toFile()
                .getAbsolutePath();
        // final String filePath = "target/ExcelExporterTobaccoProductTest.xlsx";
        LOGGER.info("Write export tobacco product excel file: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);
        try {
            final EucegProduct item = list.get(0);
            final EucegProduct entity = EucegProduct.builder()
                    .product(item.getProduct())
                    .submitterId(item.getSubmitterId())
                    .productNumber(item.getProductNumber())
                    .preferredSubmissionType(item.getPreferredSubmissionType())
                    .previousProductNumber(item.getPreviousProductNumber())
                    .generalComment(item.getGeneralComment())
                    .status(ProductStatus.IMPORTED)
                    .pirStatus(ProductPirStatus.AWAITING)
                    .createdBy("userBy")
                    .createdDate(DateTime.now())
                    .lastModifiedBy("userModified")
                    .lastModifiedDate(DateTime.now())
                    .build();
            createExporter(BulkRequest.builder().action(BulkAction.exportExcel).filters(Maps.newHashMap()).build(),
                entity).export(stream);
        } catch (final Exception e) {
            Closeables.close(stream, false);
            throw e;
        }

        final List<EucegProduct> products = importExcel(filePath);
        assertEquals(1, products.size());
        final TobaccoProduct p = (TobaccoProduct) products.stream().findFirst().orElseThrow().getProduct();
        assertNotNull(p.getTncoEmission());
        assertEquals(new BigDecimal("2.2"), p.getTncoEmission().getCo().getValue());
        assertEquals(new BigDecimal("2.0"), p.getTncoEmission().getTar().getValue());
        assertEquals(new BigDecimal("2.1"), p.getTncoEmission().getNicotine().getValue());
        assertEquals("laboratory",
            p.getTncoEmission().getLaboratories().getLaboratory().stream().findFirst().orElseThrow().getValue());
    }

    @Test
    public void shouldExportAdditionnalInformation() throws Throwable {
        final List<EucegProduct> list = importResourceExcel("tobacco-product-no-other-emission.xlsx");

        // Write the output to a file
        // final String filePath = targetFolder.resolve("tobacco-product-no-other-emission-result.xlsx")
        // .toFile()
        // .getAbsolutePath();
        final String filePath = "target/ExcelExporterTobaccoProductTest.xlsx";
        LOGGER.info("Write export tobacco product excel file: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);
        try {
            final EucegProduct item = list.get(0);
            final EucegProduct entity = EucegProduct.builder()
                    .product(item.getProduct())
                    .submitterId(item.getSubmitterId())
                    .productNumber(item.getProductNumber())
                    .preferredSubmissionType(item.getPreferredSubmissionType())
                    .previousProductNumber(item.getPreviousProductNumber())
                    .generalComment(item.getGeneralComment())
                    .status(ProductStatus.IMPORTED)
                    .pirStatus(ProductPirStatus.AWAITING)
                    .createdBy("userBy")
                    .createdDate(DateTime.now())
                    .lastModifiedBy("userModified")
                    .lastModifiedDate(DateTime.now())
                    .build();
            createExporter(BulkRequest.builder().action(BulkAction.exportExcel).filters(Maps.newHashMap()).build(),
                entity).export(stream);
        } catch (final Exception e) {
            Closeables.close(stream, false);
            throw e;
        }

    }

    private JXPathExcelExporterTobaccoProduct createExporter(final BulkRequest request, final EucegProduct entity) {
        final var dataProvider = new IDataProvider<EucegProduct>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            public Page<EucegProduct> findAll(final @Nonnull Pageable pageable) {
                return PageUtils.createPage(Arrays.asList(entity), pageable);
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return PageUtils.newRequest(0, 10);
            };
        };
        return new JXPathExcelExporterTobaccoProduct(dataProvider, request, null);
    }

    private List<EucegProduct> importResourceExcel(final String filename) throws Exception {

        try (InputStream in = getResourceAsStream(filename)) {

            final IImporterResult<EucegProduct> result = new SimpleImporterTobaccoProducts(new SimpleI18nService())
                    .importFromExcel(in, null);

            if (result.getValidationResult().hasFailures()) {
                LOGGER.error(result.getValidationResult().toString());
                fail("the import has failed");
            }

            return result.getResults();
        }
    }

    private List<EucegProduct> importExcel(final String filename) throws Exception {

        try (InputStream in = Files.asByteSource(new File(filename)).openBufferedStream()) {

            final IImporterResult<EucegProduct> result = new SimpleImporterTobaccoProducts(new SimpleI18nService())
                    .importFromExcel(in, null);

            if (result.getValidationResult().hasFailures()) {
                LOGGER.error(result.getValidationResult().toString());
                fail("the import has failed");
            }

            return result.getResults();
        }
    }

    public static class SimpleImporterTobaccoProducts extends BaseExcelmporterTobaccoProducts {

        public SimpleImporterTobaccoProducts(final I18nService i18nService) {
            super(i18nService);
        }

        @Override
        protected boolean submitterExists(final @Nonnull String submitterId) {
            return true;
        }

        @Override
        protected String findAttachmentIdByFilename(final String filename) {
            return filename;
        }

        @Override
        public TobaccoProduct getCurrentProduct(final @Nonnull String productNumber) {
            return null;
        }

    }
}
