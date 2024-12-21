package com.pmi.tpd.euceg.core.exporter.submission.xpath;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXPathExporterSubmissionTracking extends BaseExcelXPathExporterSubmissionReport {

    public ExcelXPathExporterSubmissionTracking(
            @Nonnull final IDataProvider<BaseRequestExportSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.SubmissionTracking.DESCRIPTORS,
                EucegExcelSubmissionReport.SubmissionTracking.TRACKING, ExportOption.builder().build(), dataProvider);
    }

}
