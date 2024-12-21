package com.pmi.tpd.euceg.core.task;

import java.io.File;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Pageable;

import com.pmi.tpd.euceg.core.exporter.submission.SubmissionReportType;
import com.pmi.tpd.euceg.core.exporter.submission.xml.BaseExcelXmlExporterSubmissionReport;

public interface ITrackingReportState {

    /**
     * @return
     */
    @Nonnull
    BaseExcelXmlExporterSubmissionReport<?> getExporter();

    /**
     * @return
     */
    @Nonnull
    Pageable getPageRequest();

    /**
     * @return
     */
    @Nonnull
    SubmissionReportType getReportType();

    /**
     * @return
     */
    @Nonnull
    OutputStream getReportOutputStream();

    /**
     * @param backupFile
     */
    void setReportFile(@Nonnull File reportFile);

    /**
     * @param zipOutputStream
     */
    void setReportOutputStream(@Nonnull OutputStream outputStream);

}
