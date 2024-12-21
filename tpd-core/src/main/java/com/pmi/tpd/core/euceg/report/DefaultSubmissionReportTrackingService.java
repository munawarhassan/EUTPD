package com.pmi.tpd.core.euceg.report;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.eu.ceg.SubmissionTypeEnum;
import org.jvnet.hk2.annotations.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.config.IApplicationConfiguration;
import com.pmi.tpd.api.exception.ArgumentValidationException;
import com.pmi.tpd.api.exception.NoSuchEntityException;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.backup.task.BackupPhase;
import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.euceg.IAttachmentService;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.core.exporter.Formats;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.euceg.core.exporter.submission.xml.BaseExcelXmlExporterSubmissionReport;
import com.pmi.tpd.euceg.core.exporter.submission.xml.ExcelXmlExporterEcigSubmissionTracking;
import com.pmi.tpd.euceg.core.exporter.submission.xml.ExcelXmlExporterNovelSubmissionTracking;
import com.pmi.tpd.euceg.core.exporter.submission.xml.ExcelXmlExporterSubmissionTracking;
import com.pmi.tpd.euceg.core.exporter.submission.xml.ExcelXmlExporterTobaccoSubmissionTracking;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestEcigSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestNovelTobaccoSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestOverviewSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestTobaccoSubmission;
import com.pmi.tpd.euceg.core.task.TrackingReportState;
import com.pmi.tpd.scheduler.exec.ITaskMonitor;
import com.pmi.tpd.web.core.request.IRequestManager;

@Singleton
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultSubmissionReportTrackingService implements ISubmissionReportTrackingService {

    /** */
    private static final String EXTENSION_EXCEL = ".xlsx";

    /** */
    private static final int LENGTH_SUFFIX = (new SimpleDateFormat(BackupPhase.FORMAT_UTC_TIMESTAMP).format(new Date())
            + EXTENSION_EXCEL).length();

    /** */
    private static final int LENGTH_EXTENSION = EXTENSION_EXCEL.length();

    /** */
    private static final Pattern PATTERN_UTC_FILE_NAME = Pattern
            .compile("report-([^\\\\/\\.]+)-([^\\\\/\\.]+)-([0-9]{8}-[0-9]{6}-[0-9]{3})Z\\.xlsx");

    /**
     * Compares the filename of tracking report files based on the timestamp encoded in the filename itself. Last
     * modified date is not used because it is less stable and less explicit than the timestamp we encode in the name
     * itself.
     */
    private static final Comparator<File> FILE_NAME_TIMESTAMP_COMPARATOR = new Comparator<>() {

        @Override
        public int compare(final File left, final File right) {
            return fileNameTimestamp(right).compareTo(fileNameTimestamp(left));
        }

        /**
         * Returns the part of the filename that encodes the report timestamp e.g. if this method is passed a file with
         * {@link java.io.File#getName() name} "trackingreport-someuser-20130121-225321-347.xlsx" it will return
         * 20130121-225321-347
         */
        private String fileNameTimestamp(final File file) {
            final int length = file.getName().length();
            return file.getName().substring(length - LENGTH_SUFFIX, length - LENGTH_EXTENSION);
        }
    };

    /** */
    @Nonnull
    private final IApplicationConfiguration settings;

    /** */
    @Nonnull
    private final IEucegTaskExecutorManager taskExecutorManager;

    /** */
    @Nonnull
    private final IAttachmentService attachmentService;

    /** */
    @Nonnull
    private final IIndexerOperations indexerOperations;

    /** */
    @Nonnull
    private IProductSubmissionStore productSubmissionStore;

    /** */
    @Nonnull
    private final I18nService i18nService;

    /** */
    @Nonnull
    private IRequestManager requestManager;

    public DefaultSubmissionReportTrackingService(final @Nonnull IApplicationConfiguration settings,
            final @Nonnull IEucegTaskExecutorManager taskExecutorService,
            final @Nonnull IAttachmentService attachmentService, @Nonnull final IIndexerOperations indexerOperations,
            final IProductSubmissionStore productSubmissionStore, final IRequestManager requestManager,
            @Nonnull final I18nService i18nService) {
        this.settings = Assert.checkNotNull(settings, "settings");
        this.taskExecutorManager = Assert.checkNotNull(taskExecutorService, "taskExecutorService");
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
        this.requestManager = Assert.checkNotNull(requestManager, "requestManager");
        this.attachmentService = Assert.checkNotNull(attachmentService, "attachmentService");
        this.indexerOperations = Assert.checkNotNull(indexerOperations, "indexerOperations");
        this.productSubmissionStore = Assert.checkNotNull(productSubmissionStore, "productSubmissionStore");
    }

    @PreAuthorize("hasGlobalPermission('USER')")
    @Nonnull
    public ITaskMonitor trackingReport(@Nonnull final SubmissionReportType reportType,
        @Nonnull final Pageable pageable,
        final long maxElement) {
        BaseExcelXmlExporterSubmissionReport<?> exporter = null;

        try {
            switch (reportType) {
                case tobaccoProduct:
                    exporter = new ExcelXmlExporterTobaccoSubmissionTracking(
                            createDataProvider(pageable, RequestTobaccoSubmission::new, maxElement));
                    break;
                case novelTobaccoProduct:
                    exporter = new ExcelXmlExporterNovelSubmissionTracking(
                            createDataProvider(pageable, RequestNovelTobaccoSubmission::new, maxElement));
                    break;
                case ecigaretteProduct:
                    exporter = new ExcelXmlExporterEcigSubmissionTracking(
                            createDataProvider(pageable, RequestEcigSubmission::new, maxElement));
                    break;
                case submission:
                    exporter = new ExcelXmlExporterSubmissionTracking(
                            createDataProvider(pageable, RequestOverviewSubmission::new, maxElement));
                default:
                    break;
            }
        } catch (final Throwable e) {
            if (e instanceof EucegException) {
                throw e;
            }
            new EucegException(i18nService.createKeyedMessage("app.service.euceg.submission.report.failed"), e);
        }
        final var taskMonitor = this.taskExecutorManager
                .trackingReport(new TrackingReportState(exporter, reportType, pageable));

        return taskMonitor;
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public Optional<ITaskMonitor> getTaskMonitor(final String name) {
        return taskExecutorManager.getTaskMonitor(name);
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public boolean delete(@Nonnull final ITrackingReport trackingReport) {
        final var name = validateName(checkNotNull(trackingReport, "trackingReport").getName());
        final var file = settings.getReportDirectory().resolve(name).toFile();

        return file.isFile() && file.delete();
    }

    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public boolean delete(@Nonnull final String filename) {
        final var report = getByName(filename);
        return this.delete(report);
    }

    @Override
    @Nonnull
    @PreAuthorize("hasGlobalPermission('USER')")
    public Page<ITrackingReport> findAll(@Nonnull final Pageable pageRequest) {
        checkNotNull(pageRequest, "pageRequest");

        final List<File> files = listReportFiles();
        if (pageRequest.getOffset() > files.size()) {
            return PageUtils.createEmptyPage(pageRequest);
        }

        Iterable<File> page;
        if (pageRequest.getOffset() > 0) {
            page = Iterables.skip(files, (int) pageRequest.getOffset());
        } else {
            page = files;
        }

        return PageUtils.createPage(page, pageRequest).map(this::createTrackingReport);
    }

    @Override
    @Nonnull
    @PreAuthorize("hasGlobalPermission('USER')")
    public Optional<ITrackingReport> findByName(@Nonnull String name) {
        name = validateName(name);

        final File report = settings.getReportDirectory().resolve(name).toFile();
        // No sneaky escaping the report directory
        if (report.isFile()) {
            return Optional.of(createTrackingReport(report));
        }
        return Optional.empty();
    }

    @Override
    @Nonnull
    public ITrackingReport getByName(@Nonnull final String name) {
        return findByName(name).orElseThrow(() -> new NoSuchEntityException(
                i18nService.createKeyedMessage("app.service.euceg.submission.report.nosuchreport", name)));
    }

    @Override
    public Optional<ITrackingReport> findLatest() {
        final List<File> files = listReportFiles();
        if (files.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(createTrackingReport(files.get(0))); // First file in the list is the newest
    }

    @Override
    @Nonnull
    public ITrackingReport getLatest() {
        return findLatest().orElseThrow(() -> new NoSuchEntityException(
                i18nService.createKeyedMessage("app.service.euceg.submission.report.noreports")));
    }

    @Override
    public void cancelTask(@Nonnull final String cancelToken) {
        this.taskExecutorManager.cancelTask(cancelToken);

    }

    private List<File> listReportFiles(final IOFileFilter... fileFilters) {
        final AndFileFilter andFilter = new AndFileFilter();
        andFilter.addFileFilter(new RegexFileFilter(PATTERN_UTC_FILE_NAME));
        if (fileFilters != null && fileFilters.length > 0) {
            for (final IOFileFilter filter : fileFilters) {
                andFilter.addFileFilter(filter);
            }
        }

        final List<File> files = Lists.newArrayList(
            FileUtils.listFiles(settings.getReportDirectory().toFile(), andFilter, FalseFileFilter.INSTANCE));
        Collections.sort(files, FILE_NAME_TIMESTAMP_COMPARATOR);

        return files;
    }

    private String validateName(String name) {
        checkNotNull(name, "name");

        name = name.trim();
        if (!PATTERN_UTC_FILE_NAME.matcher(name).matches()) {
            throw new ArgumentValidationException(
                    i18nService.createKeyedMessage("app.service.euceg.submission.report.invalidname", name));
        }
        return name;
    }

    private <B extends BaseRequestExportSubmission> IDataProvider<B> createDataProvider(final Pageable request,
        final Supplier<B> creator,
        final long limit) {
        final var dataProvider = new IDataProvider<B>() {

            @Override
            @Nonnull
            public String getAttachementFilename(@Nonnull final String uuid) {
                return attachmentService.getAttachment(uuid).getFilename();
            }

            @Override
            @Nonnull
            public Page<B> findAll(@Nonnull final Pageable request) {
                return findAllSubmission(request, creator);
            }

            @Override
            @Nonnull
            public Pageable getInitialPageableRequest() {
                // TODO Auto-generated method stub
                return request;
            }

            @Override
            public Long getLimit() {
                return limit;
            }
        };
        return dataProvider;
    }

    private <B extends BaseRequestExportSubmission> Page<B> findAllSubmission(@Nonnull final Pageable pageable,
        final Supplier<B> creator) {
        return this.indexerOperations.findAllSubmission(pageable).map(submission -> {
            final SubmissionEntity entity = productSubmissionStore.get(submission.getId());
            final ProductEntity product = entity.getProduct();
            try {
                final B builder = creator.get();
                builder.productType(product.getProductType().name())
                        .productCategory(submission.getType())
                        .productCategoryLabel(submission.getType().getLabel())
                        .productId(submission.getProductId())
                        .previousProductId(submission.getPreviousProductId())
                        .submissionType(
                            Formats.fromEnumToString(SubmissionTypeEnum.fromValue(submission.getSubmissionType())))
                        .createdBy(submission.getCreatedBy())
                        .createdDate(submission.getCreatedDate())
                        .internalProductNumber(submission.getInternalProductNumber())
                        .lastModifiedBy(submission.getLastModifiedBy())
                        .lastModifiedDate(submission.getLastModifiedDate())
                        .lastSubmission(submission.isLatest())
                        .lastSubmittedSubmission(submission.isLatestSubmitted())
                        .latestPirStatus(product.getPirStatus() != null ? product.getPirStatus().toString() : null)
                        .pirStatus(submission.getPirStatus() != null ? submission.getPirStatus().toString() : null)
                        .productNumber(product.getProductNumber())
                        .productStatus(product.getStatus())
                        .status(submission.getSubmissionStatus() != null ? submission.getSubmissionStatus().toString()
                                : null)
                        .xmlSubmission(entity.getXmlSubmission())
                        .sentBy(submission.getSentBy());
                return builder;
            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Nonnull
    ITrackingReport createTrackingReport(final File file) {
        final var name = file.getName();
        return new FileTrackingReport(file, extractReportType(name), extractId(name), extractUsername(name));
    }

    @Nullable
    String extractId(final String name) {
        final Matcher matcher = PATTERN_UTC_FILE_NAME.matcher(name);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(3);
    }

    @Nullable
    String extractUsername(final String name) {
        final Matcher matcher = PATTERN_UTC_FILE_NAME.matcher(name);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(2);
    }

    @Nullable
    String extractReportType(final String name) {
        final Matcher matcher = PATTERN_UTC_FILE_NAME.matcher(name);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

}
