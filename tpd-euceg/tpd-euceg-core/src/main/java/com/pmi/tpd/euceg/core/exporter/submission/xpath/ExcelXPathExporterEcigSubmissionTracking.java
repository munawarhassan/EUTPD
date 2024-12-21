package com.pmi.tpd.euceg.core.exporter.submission.xpath;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXPathExporterEcigSubmissionTracking extends BaseExcelXPathExporterSubmissionReport {

    public ExcelXPathExporterEcigSubmissionTracking(
            @Nonnull final IDataProvider<BaseRequestExportSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.EcigSubmissionTracking.DESCRIPTORS,
                EucegExcelSubmissionReport.EcigSubmissionTracking.TRACKING, ExportOption.builder().build(),
                dataProvider);
    }

}
