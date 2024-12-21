package com.pmi.tpd.euceg.core.task;

import java.io.File;
import java.io.OutputStream;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.euceg.core.exporter.submission.xml.BaseExcelXmlExporterSubmissionReport;

@Immutable
public class TrackingReportState implements ITrackingReportState {

    private final Pageable request;

    @Nonnull
    private final BaseExcelXmlExporterSubmissionReport<?> exporter;

    @Nonnull
    private final SubmissionReportType reportType;

    private OutputStream outputStream;

    /** */
    private File reportFile;

    public TrackingReportState(final @Nonnull BaseExcelXmlExporterSubmissionReport<?> exporter,
            final @Nonnull SubmissionReportType reportType, @Nonnull final Pageable request) {
        this.exporter = Assert.checkNotNull(exporter, "exporter");
        this.reportType = Assert.checkNotNull(reportType, "reportType");
        this.request = Assert.checkNotNull(request, "reportType");
    }

    @Override
    @Nonnull
    public BaseExcelXmlExporterSubmissionReport<?> getExporter() {
        return exporter;
    }

    @Override
    @Nonnull
    public Pageable getPageRequest() {
        return request;
    }

    @Override
    @Nonnull
    public SubmissionReportType getReportType() {
        return reportType;
    }

    @Override
    @Nonnull
    public OutputStream getReportOutputStream() {
        return outputStream;
    }

    @Override
    public void setReportFile(@Nonnull final File reportFile) {
        this.reportFile = reportFile;
    }

    @Override
    public void setReportOutputStream(@Nonnull final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

}
