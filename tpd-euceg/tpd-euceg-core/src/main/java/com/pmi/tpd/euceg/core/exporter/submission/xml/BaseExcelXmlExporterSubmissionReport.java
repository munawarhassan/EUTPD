package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.scheduler.ITaskMonitorProgress;
import com.pmi.tpd.euceg.core.excel.ColumnDescriptor;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.exporter.BaseExcelExporter;
import com.pmi.tpd.euceg.core.exporter.ICallbackExport;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.task.ITrackingReportState;

public class BaseExcelXmlExporterSubmissionReport<T extends BaseRequestExportSubmission> extends BaseExcelExporter<T> {

    protected BaseExcelXmlExporterSubmissionReport(final @Nonnull ListDescriptor root,
            final @Nonnull List<ExcelSheet> excelSheets, final ExportOption options,
            @Nonnull final IDataProvider<T> dataProvider) {
        super(root, excelSheets, options, dataProvider);
    }

    public void export(@Nonnull final ITrackingReportState state, @Nullable final ITaskMonitorProgress monitorProgress)
            throws Throwable {
        super.export(state.getReportOutputStream(), monitorProgress, new ICallbackExport<T>() {

            @Override
            public void forEach(final @Nonnull T builder) {
                builder.parse(BaseExcelXmlExporterSubmissionReport.this);
                addSubmission(builder);
            }

            @Override
            public void summarize() {
                // noop
            }
        });
    }

    @Override
    protected void setValue(final ColumnDescriptor<?> col) {
        throw new UnsupportedOperationException();
    }

    protected void addSubmission(final T submission) {
        entrySheet(getExcelSheets().stream().findFirst().get());

    }

}
