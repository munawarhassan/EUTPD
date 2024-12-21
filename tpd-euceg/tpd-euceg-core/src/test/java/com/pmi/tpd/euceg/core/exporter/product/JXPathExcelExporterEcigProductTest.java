package com.pmi.tpd.euceg.core.exporter.product;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import org.eu.ceg.EcigProduct;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.support.SimpleI18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.euceg.core.BulkRequest;
import com.pmi.tpd.euceg.core.BulkRequest.BulkAction;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.importer.BaseExcelImporterEcigaretteProducts;
import com.pmi.tpd.euceg.core.importer.IImporterResult;
import com.pmi.tpd.testing.junit5.TestCase;

public class JXPathExcelExporterEcigProductTest extends TestCase {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(JXPathExcelExporterEcigProductTest.class);

    public static Path targetFolder;

    @BeforeAll
    public static void start(@TempDir final Path tempDir) {
        targetFolder = tempDir;
    }

    @Test
    public void shouldntExportFailed() throws Throwable {
        final List<EucegProduct> list = importExcel("ecig-product-submission.xlsx");

        // Write the output to a file
        final String filePath = targetFolder.resolve("ExcelExporterEcigProductTest.xlsx").toFile().getAbsolutePath();
        // final String filePath = "target/ExcelExporterEcigProductTest.xlsx";
        LOGGER.info("Write export tobacco product excel file: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);

        final var request = BulkRequest.builder().action(BulkAction.exportExcel).filters(Maps.newHashMap()).build();
        try {

            final var dataProvider = new IDataProvider<EucegProduct>() {

                @Override
                @Nonnull
                public String getAttachementFilename(@Nonnull final String uuid) {
                    return "attachment-" + uuid;
                }

                @Override
                public @Nonnull Page<EucegProduct> findAll(final @Nonnull Pageable request) {
                    return PageUtils.createPage(list, request);
                };

                @Override
                @Nonnull
                public Pageable getInitialPageableRequest() {
                    return PageUtils.newRequest(0, 10);
                }

            };
            final JXPathExcelExporterEcigProduct exporter = new JXPathExcelExporterEcigProduct(dataProvider, request,
                    null);
            exporter.export(stream);
        } catch (final Exception e) {
            Closeables.close(stream, false);
            throw e;
        }

    }

    private List<EucegProduct> importExcel(final String filename) throws Exception {

        try (InputStream in = getResourceAsStream(filename)) {

            final IImporterResult<EucegProduct> result = new SimpleImporterEcigProducts(new SimpleI18nService())
                    .importFromExcel(in, null);

            if (result.getValidationResult().hasFailures()) {
                LOGGER.error(result.getValidationResult().toString());
                fail("the import has failed");
            }

            return result.getResults();
        }
    }

    public static class SimpleImporterEcigProducts extends BaseExcelImporterEcigaretteProducts {

        public SimpleImporterEcigProducts(final I18nService i18nService) {
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
