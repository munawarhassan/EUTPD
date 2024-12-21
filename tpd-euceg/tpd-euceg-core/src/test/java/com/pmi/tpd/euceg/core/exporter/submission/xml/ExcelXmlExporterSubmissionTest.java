package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.eu.ceg.SubmissionTypeEnum;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.google.common.io.CharStreams;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.euceg.core.task.TrackingReportState;
import com.pmi.tpd.testing.junit5.TestCase;

public class ExcelXmlExporterSubmissionTest extends TestCase {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelXmlExporterSubmissionTest.class);

    public static Path targetFolder = Paths.get("target/results/" + ExcelXmlExporterSubmissionTest.class.getName());

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
        final Page<RequestOverviewSubmission> page = PageUtils.createPage(Arrays.asList(
            (RequestOverviewSubmission) createSubmission("submission-tobacco.xml", RequestOverviewSubmission::new)
                    .internalProductNumber("7BBBB-99962-POM.060000")
                    .submissionType(SubmissionTypeEnum.NEW.toString())
                    .productNumber("POM.060000")
                    .productCategory(ProductType.TOBACCO)
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .lastSubmittedSubmission(true)
                    .latestPirStatus(ProductPirStatus.AWAITING.toString())
                    .pirStatus(ProductPirStatus.ACTIVE.toString())
                    .sentBy("user"),
            (RequestOverviewSubmission) createSubmission("submission-ecig.xml", RequestOverviewSubmission::new)
                    .internalProductNumber("99962-POM.nnnnn")
                    .submissionType(SubmissionTypeEnum.NEW.toString())
                    .productCategory(ProductType.ECIGARETTE)
                    .productNumber("POM.nnnnn")
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .lastSubmittedSubmission(true)
                    .latestPirStatus(ProductPirStatus.AWAITING.toString())
                    .pirStatus(ProductPirStatus.ACTIVE.toString())
                    .sentBy("user")),
            request);

        final var dataProvider = new IDataProvider<RequestOverviewSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<RequestOverviewSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };
        final var exporter = new ExcelXmlExporterSubmissionTracking(dataProvider);
        final var state = new TrackingReportState(exporter, SubmissionReportType.submission, request);
        state.setReportOutputStream(stream);
        exporter.export(state, null);
    }

    @Test
    public void shouldExportTobaccoSubmissionTracking() throws Throwable {

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);

        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<RequestTobaccoSubmission> page = PageUtils.createPage(
            Arrays.asList(
                (RequestTobaccoSubmission) createSubmission("submission-tobacco.xml", RequestTobaccoSubmission::new)
                        .internalProductNumber("7BBBB-99962-POM.060000")
                        .submissionType(SubmissionTypeEnum.NEW.toString())
                        .productNumber("POM.060000")
                        .productCategory(ProductType.TOBACCO)
                        .status(SubmissionStatus.SUBMITTED.toString())
                        .lastModifiedDate(DateTime.now())
                        .lastSubmission(true)
                        .lastSubmittedSubmission(true)
                        .latestPirStatus(ProductPirStatus.AWAITING.toString())
                        .pirStatus(ProductPirStatus.ACTIVE.toString())
                        .sentBy("user"),
                (RequestTobaccoSubmission) createSubmission("submission-tobacco-POM.recieved.xml",
                    RequestTobaccoSubmission::new).internalProductNumber("99962-POM.recieved")
                            .submissionType(SubmissionTypeEnum.NEW.toString())
                            .productNumber("POM.060000")
                            .productCategory(ProductType.TOBACCO)
                            .status(SubmissionStatus.SUBMITTED.toString())
                            .lastModifiedDate(DateTime.now())
                            .lastSubmission(true)
                            .lastSubmittedSubmission(true)
                            .latestPirStatus(ProductPirStatus.AWAITING.toString())
                            .pirStatus(ProductPirStatus.ACTIVE.toString())
                            .sentBy("user")),
            request);
        final var dataProvider = new IDataProvider<RequestTobaccoSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<RequestTobaccoSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };
        final var exporter = new ExcelXmlExporterTobaccoSubmissionTracking(dataProvider);
        final var state = new TrackingReportState(exporter, SubmissionReportType.ecigaretteProduct, request);
        state.setReportOutputStream(stream);
        exporter.export(state, null);
    }

    @Test
    public void shouldExportEcigSubmissionTracking() throws Throwable {
        final RequestEcigSubmission submission = (RequestEcigSubmission) createSubmission("submission-ecig.xml",
            RequestEcigSubmission::new).submissionType(SubmissionTypeEnum.NEW.toString())
                    .productCategory(ProductType.ECIGARETTE)
                    .internalProductNumber("99962-POM.nnnnn")
                    .productNumber("POM.nnnnn")
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmittedSubmission(true)
                    .lastSubmission(true)
                    .pirStatus(ProductPirStatus.ACTIVE.toString())
                    .latestPirStatus(ProductPirStatus.AWAITING.toString());

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);
        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<RequestEcigSubmission> page = PageUtils.createPage(Arrays.asList(submission), request);
        final var dataProvider = new IDataProvider<RequestEcigSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<RequestEcigSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };

        final var exporter = new ExcelXmlExporterEcigSubmissionTracking(dataProvider);
        final var state = new TrackingReportState(exporter, SubmissionReportType.ecigaretteProduct, request);
        state.setReportOutputStream(stream);
        exporter.export(state, null);
    }

    @Test
    public void shouldExportNovelTobaccoSubmissionTracking() throws Throwable {
        final RequestNovelTobaccoSubmission submission = (RequestNovelTobaccoSubmission) createSubmission(
            "submission-novel-tobacco.xml",
            RequestNovelTobaccoSubmission::new).internalProductNumber("7BBBB-99962-POM.060000")
                    .submissionType(SubmissionTypeEnum.NEW.toString())
                    .productNumber("POM.060000")
                    .productCategory(ProductType.TOBACCO)
                    .status(SubmissionStatus.SUBMITTED.toString())
                    .lastModifiedDate(DateTime.now())
                    .lastSubmission(true)
                    .lastSubmittedSubmission(true)
                    .latestPirStatus(ProductPirStatus.AWAITING.toString())
                    .pirStatus(ProductPirStatus.ACTIVE.toString())
                    .sentBy("user");

        // Write the output to a file
        final String filePath = targetFolder.resolve(getMethodName() + ".xlsx").toFile().getAbsolutePath();
        LOGGER.info("Write export Excel Submission Tracking File: " + filePath);
        final FileOutputStream stream = new FileOutputStream(filePath);

        final Pageable request = PageUtils.newRequest(0, 10);
        final Page<RequestNovelTobaccoSubmission> page = PageUtils.createPage(Arrays.asList(submission), request);

        final var dataProvider = new IDataProvider<RequestNovelTobaccoSubmission>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return "attachment-" + uuid;
            }

            @Override
            @Nonnull
            public Page<RequestNovelTobaccoSubmission> findAll(@Nonnull final Pageable request) {
                return page;
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                return request;
            }

        };

        final var exporter = new ExcelXmlExporterNovelSubmissionTracking(dataProvider);
        final var state = new TrackingReportState(exporter, SubmissionReportType.novelTobaccoProduct, request);
        state.setReportOutputStream(stream);
        exporter.export(state, null);
    }

    private <T extends BaseRequestExportSubmission> T createSubmission(final String filename, final Supplier<T> creator)
            throws Exception {
        final T builder = creator.get();

        try (InputStream in = getResourceAsStream(filename)) {
            builder.xmlSubmission(CharStreams.toString(Eucegs.openReader(in)));

            return builder;
        }
    }
}
