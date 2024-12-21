package com.pmi.tpd.euceg.core.exporter.submission.xpath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.testing.junit5.TestCase;

public class ExcelXpathExporterSubmissionTest extends TestCase {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelXpathExporterSubmissionTest.class);

    public static Path targetFolder = Paths.get("target/results/" + ExcelXpathExporterSubmissionTest.class.getName());

    @BeforeAll
    public static void beforeAll() {
        if (targetFolder.toFile().exists()) {
            Arrays.stream(targetFolder.toFile().listFiles()).forEach(File::delete);
            targetFolder.toFile().delete();
        }
        targetFolder.toFile().mkdirs();

    }

    @Test
    public void shouldExportSubmissionTracking() throws Throwable {

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);
        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<BaseRequestExportSubmission> page = PageUtils.createPage(Arrays.asList(
            createSubmission("submission-tobacco.xml").internalProductNumber("7BBBB-99962-POM.060000")
                    .productNumber("POM.060000")
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .pirStatus(ProductPirStatus.ACTIVE.toString()),
            createSubmission("submission-ecig.xml").internalProductNumber("99962-POM.nnnnn")
                    .productNumber("POM.nnnnn")
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .pirStatus(ProductPirStatus.ACTIVE.toString())),
            request);

        final var dataProvider = new IDataProvider<BaseRequestExportSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<BaseRequestExportSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };

        final ExcelXPathExporterSubmissionTracking exporter = new ExcelXPathExporterSubmissionTracking(dataProvider);
        exporter.export(stream, request, "");
    }

    @Test
    public void shouldExportTobaccoSubmissionTracking() throws Throwable {

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);

        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<BaseRequestExportSubmission> page = PageUtils.createPage(Arrays.asList(
            createSubmission("submission-tobacco.xml").internalProductNumber("7BBBB-99962-POM.060000")
                    .productNumber("POM.060000")
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .pirStatus(ProductPirStatus.ACTIVE.toString()),
            createSubmission("submission-tobacco-POM.recieved.xml").internalProductNumber("99962-POM.recieved")
                    .productNumber("POM.recieved")
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .pirStatus(ProductPirStatus.ACTIVE.toString())),
            request);
        final var dataProvider = new IDataProvider<BaseRequestExportSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<BaseRequestExportSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };
        final var exporter = new ExcelXPathExporterTobaccoSubmissionTracking(dataProvider);
        exporter.export(stream, request, "");
    }

    @Test
    public void shouldExportEcigSubmissionTracking() throws Throwable {
        final BaseRequestExportSubmission submission = createSubmission("submission-ecig.xml")
                .internalProductNumber("99962-POM.nnnnn")
                .productNumber("POM.nnnnn")
                .status(SubmissionStatus.SUBMITTED.toString())
                .lastModifiedDate(DateTime.now())
                .lastSubmission(true)
                .pirStatus(ProductPirStatus.ACTIVE.toString());

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);
        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<BaseRequestExportSubmission> page = PageUtils.createPage(Arrays.asList(submission), request);

        final var dataProvider = new IDataProvider<BaseRequestExportSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<BaseRequestExportSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };

        final var exporter = new ExcelXPathExporterEcigSubmissionTracking(dataProvider);
        exporter.export(stream, request, "");
    }

    @Test
    public void shouldExportNovelTobaccoSubmissionTracking() throws Throwable {
        final BaseRequestExportSubmission submission = createSubmission("submission-novel-tobacco.xml")
                .internalProductNumber("7BBBB-99962-POM.060000")
                .productNumber("POM.060000")
                .status(SubmissionStatus.SUBMITTED.toString())
                .lastModifiedDate(DateTime.now())
                .lastSubmission(true)
                .pirStatus(ProductPirStatus.ACTIVE.toString());

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);

        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<BaseRequestExportSubmission> page = PageUtils.createPage(Arrays.asList(submission), request);

        final var dataProvider = new IDataProvider<BaseRequestExportSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<BaseRequestExportSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };

        final var exporter = new ExcelXPathExporterNovelTobaccoSubmissionTracking(dataProvider);
        exporter.export(stream, request, "");
    }

    private BaseRequestExportSubmission createSubmission(final String filename) throws Exception {
        final BaseRequestExportSubmission builder = new BaseRequestExportSubmission();

        try (InputStream in = getResourceAsStream(filename)) {
            builder.submission(Eucegs.unmarshal(in));

            return builder;
        }
    }
}
