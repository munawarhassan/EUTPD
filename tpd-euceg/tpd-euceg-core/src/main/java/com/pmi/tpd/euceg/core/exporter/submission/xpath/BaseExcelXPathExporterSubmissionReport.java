package com.pmi.tpd.euceg.core.exporter.submission.xpath;

import java.io.OutputStream;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.jxpath.JXPathContext;
import org.springframework.data.domain.Pageable;

import com.google.common.base.Preconditions;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.exporter.BaseExcelXPathExporter;
import com.pmi.tpd.euceg.core.exporter.ICallbackExport;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.BaseRequestExportSubmission;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;

public class BaseExcelXPathExporterSubmissionReport extends BaseExcelXPathExporter<BaseRequestExportSubmission> {

    /** */
    @Nonnull
    private final ExcelSheet excelSheet;

    protected BaseExcelXPathExporterSubmissionReport(@Nonnull final ListDescriptor root,
            @Nonnull final ExcelSheet excelSheet, final ExportOption options,
            @Nonnull final IDataProvider<BaseRequestExportSubmission> dataProvider) {
        super(root, Arrays.asList(excelSheet), options, dataProvider);
        this.excelSheet = Preconditions.checkNotNull(excelSheet);
    }

    public void export(@Nonnull final OutputStream stream,
        @Nonnull final Pageable request,
        @Nullable final String usedFilter) throws Throwable {
        super.export(stream, null, new ICallbackExport<BaseRequestExportSubmission>() {

            @Override
            public void forEach(final @Nonnull BaseRequestExportSubmission submission) {
                addSubmission(submission);
            }

            @Override
            public void summarize() {
                // noop
            }
        });
    }

    private void addSubmission(final BaseRequestExportSubmission submission) {
        entrySheet(excelSheet);
        final int size = getValue("fmt:size(/submission/product/presentations/presentation)", Integer.class);

        for (int i = 1; i <= size; i++) {
            setVariable("$i", i);
            createRow();
            relative("/submission/product/presentations/presentation[$i]", () -> {
                setValues("Tracking");
            });
        }

    }

    @Override
    protected void declareContext() {
        super.declareContext();
        final JXPathContext context = getSharedContext();
        context.getVariables().declareVariable("internalProductNumber", "");

    }

    @Override
    protected JXPathContext initialContext(final BaseRequestExportSubmission submission) {
        final JXPathContext context = super.initialContext(submission);
        context.setValue("$internalProductNumber", submission.internalProductNumber());
        return context;
    }

}
