package com.pmi.tpd.euceg.core.exporter.submission.xml;

import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.pmi.tpd.euceg.core.exporter.Formats;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.submission.ExportOption;
import com.pmi.tpd.euceg.core.exporter.submission.xml.RequestEcigSubmission.Presentation;
import com.pmi.tpd.euceg.core.internal.EucegExcelSubmissionReport;

public class ExcelXmlExporterEcigSubmissionTracking
        extends BaseExcelXmlExporterSubmissionReport<RequestEcigSubmission> {

    public ExcelXmlExporterEcigSubmissionTracking(@Nonnull final IDataProvider<RequestEcigSubmission> dataProvider) {
        super(EucegExcelSubmissionReport.EcigSubmissionTracking.DESCRIPTORS,
                Arrays.asList(EucegExcelSubmissionReport.EcigSubmissionTracking.TRACKING),
                ExportOption.builder().build(), dataProvider);
    }

    @Override
    protected void addSubmission(final RequestEcigSubmission submission) {
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
            setValue("Product_Package_Units", p.packageUnits());

            // Detail

            setValue("Product_Weight", submission.productWeight());
            setValue("Product_Volume", submission.productVolume());
            setValue("Product_CLP_Classification", submission.clpClassification());
            setValue("Product_Studies_Summaries_File", submission.studySummaryFiles());
            setValue("Product_Market_Research_File", submission.marketResearchFiles());

            //
            // Design
            //
            setValue("E-Cigarette_Description", submission.designDescription());
            setValue("E-Cigarette_Identification_E-Cigarette_Device", submission.designIdentificationEcigDevice());
            setValue("E-Cigarette_Liquid_Volume/Capacity", submission.designLiquidVolumeCapacity());
            setValue("E-Cigarette_Nicotine_Concentration", submission.designNicotineConcentration());
            setValue("E-Cigarette_Battery_Type", submission.designBatteryType());
            setValue("E-Cigarette_Battery_Type_Capacity", submission.designBatteryCapacity());
            setValue("E-Cigarette_Volt/Watt_Adjustable", submission.designVoltageWattageAdjustable());
            setValue("E-Cigarette_Voltage", submission.designVoltage());
            setValue("E-Cigarette_Voltage_Lower_Range", submission.designVoltageLowerRange());
            setValue("E-Cigarette_Voltage_Upper_Range", submission.designVoltageUpperRange());
            setValue("E-Cigarette_Wattage", submission.designWattage());
            setValue("E-Cigarette_Wattage_Lower_Range", submission.designWattageLowerRange());
            setValue("E-Cigarette_Wattage_Upper_Range", submission.designWattageUpperRange());
            setValue("E-Cigarette_Airflow_Adjustable", submission.designAirflowAdjustable());
            setValue("E-Cigarette_Wick_Changeable", submission.designWickChangeable());
            setValue("E-Cigarette_Microprocessor", submission.designMicroprocessor());
            setValue("E-Cigarette_Coil_Composition", submission.designCoilComposition());
            setValue("E-Cigarette_Coil_Resistance", submission.designCoilResistance());
            setValue("E-Cigarette_Nicotine_Dose/Uptake_File", submission.designNicotineDoseUptakeFile());
            setValue("E-Cigarette _Child_Tamper_Proof", submission.designChildTamperProof());
            setValue("E-Cigarette_Production_File", submission.designProductionFile());
            setValue("E-Cigarette_Production_Conformity", submission.designProductionConformity());
            setValue("E-Cigarette_Quality_Safety", submission.designQualitySafety());
            setValue("E-Cigarette_High_Purity", submission.designHighPurity());
            setValue("E-Cigarette_Non_Risk", submission.designNonRisk());
            setValue("E-Cigarette_Consistent_Dosing", submission.designConsistentDosing());
            setValue("E-Cigarette_Consistent_Dosing_Methods", submission.designConsistentDosingMethodsFile());
            setValue("E-Cigarette_Opening/Refill_File", submission.designOpeningRefillFile());
            setValue("E-Cigarette_Leaflet_File", submission.designLeafletFile());

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
