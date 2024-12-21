package com.pmi.tpd.euceg.core.exporter.submission.xpath;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXPathExporterTobaccoSubmissionTracking extends BaseExcelXPathExporterSubmissionReport {

    public ExcelXPathExporterTobaccoSubmissionTracking(
            @Nonnull final IDataProvider<BaseRequestExportSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.TobaccoSubmissionTracking.DESCRIPTORS,
                EucegExcelSubmissionReport.TobaccoSubmissionTracking.TRACKING, ExportOption.builder().build(),
                dataProvider);
    }

}
