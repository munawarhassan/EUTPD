package com.pmi.tpd.euceg.core.exporter.product;

import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.jxpath.JXPathContext;

import com.google.common.base.Strings;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.exporter.BaseExcelXPathExporter;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum;

public class BaseJXPathExcelExporterProduct extends BaseExcelXPathExporter<EucegProduct> {

    protected BaseJXPathExcelExporterProduct(@Nonnull final ListDescriptor root,
            @Nonnull final List<ExcelSheet> excelSheets, final ExportOption options,
            @Nonnull final IDataProvider<EucegProduct> dataProvider) {
        super(root, excelSheets, options, dataProvider);
    }

    @Override
    protected void declareContext() {
        super.declareContext();
        final JXPathContext context = getSharedContext();
        context.getVariables().declareVariable("internalProductNumber", "");
        context.getVariables().declareVariable("productNumber", "");
        context.getVariables().declareVariable("submitterId", "");
        context.getVariables().declareVariable("generalComment", "");
        context.getVariables().declareVariable("previousProductNumber", "");
        context.getVariables().declareVariable("status", "");

        context.getVariables().declareVariable("pirStatus", "");
        context.getVariables().declareVariable("submissionType", "");

        context.getVariables().declareVariable("lastModifiedDate", "");
        context.getVariables().declareVariable("lastModifiedBy", "");
        context.getVariables().declareVariable("createdDate", "");
        context.getVariables().declareVariable("createdBy", "");
    }

    @Override
    protected JXPathContext initialContext(final EucegProduct product) {
        final JXPathContext context = super.initialContext(product);

        context.setValue("$internalProductNumber",
            Strings.isNullOrEmpty(product.getInternalProductNumber())
                    ? String.format("%s-%s", product.getSubmitterId(), product.getProductNumber())
                    : product.getInternalProductNumber());
        context.setValue("$productNumber", product.getProductNumber());
        context.setValue("$submitterId", product.getSubmitterId());
        context.setValue("$generalComment", product.getGeneralComment());
        context.setValue("$previousProductNumber", product.getPreviousProductNumber());
        context.setValue("$status", product.getStatus());
        if (product.getPirStatus() != null) {
            context.setValue("$pirStatus", product.getPirStatus().name());
        } else {
            context.setValue("$pirStatus", "");
        }
        if (product.getPreferredSubmissionType() != null) {
            context.setValue("$submissionType", product.getPreferredSubmissionType().value());
        } else {
            context.setValue("$submissionType", SubmissionTypeEnum.NEW_PRODUCT.getValue());
        }
        context.setValue("$lastModifiedDate", product.getLastModifiedDate());
        context.setValue("$lastModifiedBy", product.getLastModifiedBy());
        context.setValue("$createdDate", product.getCreatedDate());
        context.setValue("$createdBy", product.getCreatedBy());
        return context;
    }

}
