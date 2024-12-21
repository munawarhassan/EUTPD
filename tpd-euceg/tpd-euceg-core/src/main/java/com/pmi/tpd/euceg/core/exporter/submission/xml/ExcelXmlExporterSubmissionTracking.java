package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.Formats;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestOverviewSubmission.Presentation;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXmlExporterSubmissionTracking
        extends BaseExcelXmlExporterSubmissionReport<RequestOverviewSubmission> {

    public ExcelXmlExporterSubmissionTracking(@Nonnull final IDataProvider<RequestOverviewSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.SubmissionTracking.DESCRIPTORS,
                Arrays.asList(EucegExcelSubmissionReport.SubmissionTracking.TRACKING), ExportOption.builder().build(),
                dataProvider);
    }

    @Override
    protected void addSubmission(final RequestOverviewSubmission submission) {
        super.addSubmission(submission);
        submission.presentations().forEach(p -> {
            createRow();
            setValue("Internal_Product_Number", submission.internalProductNumber());
            setValue("Product_Category", submission.productCategoryLabel());
            setValue("Product_ID", submission.productId());
            setValue("Previous_Product_ID", submission.previousProductId());
            setValue("Submission_Type", submission.submissionType());
            setValue("Status", submission.status());
            setValue("TPD_Product_Number", submission.productNumber());
            setValue("Product_Type", submission.productType());
            setValue("National_Markets",
                Formats.joinCountryCode(submission.presentations()
                        .stream()
                        .map(Presentation::nationalMarket)
                        .collect(Collectors.toList())));

            //
            // Presentation
            //
            setValue("Product_Brand_Name", p.brandName());
            setValue("Product_Brand_Subtype_Name", p.brandSubtype());
            setValue("Product_Launch_Date", p.launchDate());
            setValue("Product_Withdrawal_Date", p.withdrawalDate());
            setValue("Product_Submitter_Number", p.productSubmitterNumber());
            setValue("Product_National_Market", Formats.countryName(p.nationalMarket()));

            //
            // Audit
            //
            setValue("Sent_By", submission.sentBy());
            setValue("PIR_Status", submission.pirStatus());
            setValue("Latest_PIR_Status", submission.latestPirStatus());
            setValue("Last_Modified", submission.lastModifiedDate());
            setValue("Year", Formats.year(submission.lastModifiedDate()));
            setValue("Month", Formats.month(submission.lastModifiedDate()));
            setValue("Latest_Submission_Flag", Formats.yesNo(submission.lastSubmittedSubmission()));
        });

    }

}
