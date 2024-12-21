package com.pmi.tpd.euceg.core.exporter.submission.xpath;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXPathExporterNovelTobaccoSubmissionTracking extends BaseExcelXPathExporterSubmissionReport {

    public ExcelXPathExporterNovelTobaccoSubmissionTracking(
            @Nonnull final IDataProvider<BaseRequestExportSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.NovelTobaccoSubmissionTracking.DESCRIPTORS,
                EucegExcelSubmissionReport.NovelTobaccoSubmissionTracking.TRACKING, ExportOption.builder().build(),
                dataProvider);

    }

}
