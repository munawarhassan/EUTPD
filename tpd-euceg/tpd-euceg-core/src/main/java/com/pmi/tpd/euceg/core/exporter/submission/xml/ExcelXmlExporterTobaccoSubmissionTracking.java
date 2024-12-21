package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.Formats;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestTobaccoSubmission.Presentation;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXmlExporterTobaccoSubmissionTracking
        extends BaseExcelXmlExporterSubmissionReport<RequestTobaccoSubmission> {

    public ExcelXmlExporterTobaccoSubmissionTracking(
            @Nonnull final IDataProvider<RequestTobaccoSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.TobaccoSubmissionTracking.DESCRIPTORS,
                Arrays.asList(EucegExcelSubmissionReport.TobaccoSubmissionTracking.TRACKING),
                ExportOption.builder().build(), dataProvider);
    }

    @Override
    protected void addSubmission(final RequestTobaccoSubmission submission) {
        super.addSubmission(submission);
        submission.presentations().forEach(p -> {
            createRow();
            setValue("Internal_Product_Number", submission.internalProductNumber());
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
            setValue("Product_National_Comment", Formats.trim(p.nationalComment()));
            setValue("Product_Package_Type", p.packageType());
            setValue("Product_Package_Units", p.packageUnits());
            setValue("Product_Package_Net_Weight", p.packageNetWeight());
            setValue("Product_Unit_Packet_Picture_File", p.unitPacketPictureFiles());

            //
            // tnco
            //
            setValue("Emission_Tar", submission.tncoEmissionTar());
            setValue("Emission_Nicotine", submission.tncoEmissionNicotine());
            setValue("Emission_CO", submission.tncoEmissionCo());

            // Detail

            setValue("Submission_General_Comment", submission.generalComment());
            setValue("Product_Same_Composition_Other", submission.sameCompositionOther());
            setValue("Product_Length", submission.productLength());
            setValue("Product_Diameter", submission.productDiameter());
            setValue("Product_Weight", submission.productWeight());
            setValue("Product_Tobacco_Weight", submission.productTobaccoWeight());
            setValue("Product_Filter", submission.productFilter());
            setValue("Product_Filter_Length", submission.productFilterLength());
            setValue("Product_Technical_File", submission.technicalFiles());
            setValue("Product_Market_Research_File", submission.marketResearchFiles());

            //
            // Cigarette Specific
            //
            setValue("Cigarette_Charecterising_Flavour", submission.cigaretteCharacterisingFlavour());
            setValue("Cigarette_Filter_Ventilation", submission.cigaretteFilterVentilation());
            setValue("Cigarette_Filter_Drop_Pressure_Closed", submission.cigaretteFilterDropPressureClosed());
            setValue("Cigarette_Filter_Drop_Pressure_Open", submission.cigaretteFilterDropPressureOpen());

            //
            // Ryo/Pipe
            //
            setValue("Roll-your-own/pipe_Total_ Nicotine_Content", submission.ryoPipeSpecificTotalNicotineContent());

            //
            // smokeless
            //
            setValue("Smokeless_pH", submission.smokelessPh());
            setValue("Smokeless_Nicotine_Content", submission.smokelessTotalNicotineContent());

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
