package com.pmi.tpd.euceg.core.internal;

import static com.pmi.tpd.euceg.core.excel.ColumnDescriptor.createColumn;

import java.math.BigDecimal;
import java.util.Arrays;

import javax.annotation.Nonnull;

import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.PackageTypeEnum;
import org.eu.ceg.VoltageWattageAdjustableEnum;
import org.joda.time.DateTime;

import com.pmi.tpd.euceg.core.excel.ColumnMetadata;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.GroupDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;

public class EucegExcelSubmissionReport {

    private static final String DATETIME_FORMAT = "yyyy-mm-dd hh:mm:ss";

    public interface SubmissionTracking {

        /** */
        @Nonnull
        public static final ExcelSheet TRACKING = ExcelSheet.create("Submission", 0);

        /** */
        @Nonnull
        public static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(GroupDescriptor
                .builder("Tracking", TRACKING)
                .columns(
                    createColumn("Internal_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                    createColumn("Product_Category",
                        ColumnMetadata.builder(String.class).xpath("fmt:productCategory(/submission/product)").build()),
                    createColumn("Product_ID",
                        ColumnMetadata.builder(String.class).xpath("/submission/product/productID/value").build()),
                    createColumn("Previous_Product_ID",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/previousProductID/value")
                                .build()),
                    createColumn("Submission_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/submissionType/value)")
                                .build()),
                    createColumn("Status", ColumnMetadata.builder(String.class).xpath("/status").build()),
                    createColumn("TPD_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("/productNumber").build()),
                    createColumn("Product_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/product/productType/value)")
                                .build()),
                    createColumn("National_Markets",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinCountry(/submission/product/presentations/presentation/nationalMarket/value)")
                                .build()),

                    //
                    // Presentation
                    createColumn("Product_Brand_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandName/value)").build()),
                    createColumn("Product_Brand_Subtype_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandSubtypeName/value)").build()),
                    createColumn("Product_Launch_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(launchDate)").build()),
                    createColumn("Product_Withdrawal_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(withdrawalDate)").build()),
                    createColumn("Product_Submitter_Number",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(productNumber/value)").build()),
                    createColumn("Product_National_Market",
                        ColumnMetadata.builder(NationalMarketValue.class)
                                .xpath("fmt:countryName(fmt:fromEnum(nationalMarket/value))")
                                .build()),

                    //
                    //
                    createColumn("Sent_By", ColumnMetadata.builder(String.class).xpath("/sentBy").build()),
                    createColumn("PIR_Status", ColumnMetadata.builder(String.class).xpath("/pirStatus").build()),
                    createColumn("Latest_PIR_Status",
                        ColumnMetadata.builder(String.class).xpath("/latestPirStatus").build()),
                    createColumn("Last_Modified",
                        ColumnMetadata.builder(DateTime.class)
                                .xpath("fmt:fromDateTime(/lastModifiedDate)")
                                .format(DATETIME_FORMAT)
                                .build()),
                    createColumn("Year",
                        ColumnMetadata.builder(String.class).xpath("fmt:year(/lastModifiedDate)").build()),
                    createColumn("Month",
                        ColumnMetadata.builder(String.class).xpath("fmt:month(/lastModifiedDate)").build()),
                    createColumn("Latest_Submission_Flag",
                        ColumnMetadata.builder(Boolean.class).xpath("fmt:yesNo(/lastSubmittedSubmission)").build()))
                .build()));

    }

    public interface TobaccoSubmissionTracking {

        /** */
        @Nonnull
        public static final ExcelSheet TRACKING = ExcelSheet.create("Tobacco Submission", 0);

        /** */
        @Nonnull
        public static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(GroupDescriptor
                .builder("Tracking", TRACKING)
                .columns(
                    createColumn("Internal_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                    // createColumn("Product_Category",
                    // ColumnMetadata.builder(String.class).xpath("fmt:productCategory(/submission/product)").build()),
                    createColumn("Product_ID",
                        ColumnMetadata.builder(String.class).xpath("/submission/product/productID/value").build()),
                    createColumn("Previous_Product_ID",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/previousProductID/value")
                                .build()),
                    // createColumn("Submitter_ID",
                    // ColumnMetadata.builder(String.class).xpath("/submission/submitter/submitterID").build()),
                    createColumn("Submission_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/submissionType/value)")
                                .build()),
                    createColumn("Status", ColumnMetadata.builder(String.class).xpath("/status").build()),
                    createColumn("TPD_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("/productNumber").build()),
                    createColumn("Product_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/product/productType/value)")
                                .build()),
                    createColumn("National_Markets",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinCountry(/submission/product/presentations/presentation/nationalMarket/value)")
                                .build()),

                    //
                    // Presentation
                    createColumn("Product_Brand_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandName/value)").build()),
                    createColumn("Product_Brand_Subtype_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandSubtypeName/value)").build()),
                    createColumn("Product_Launch_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(launchDate)").build()),
                    createColumn("Product_Withdrawal_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(withdrawalDate)").build()),
                    createColumn("Product_Submitter_Number",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(productNumber/value)").build()),
                    createColumn("Product_National_Market",
                        ColumnMetadata.builder(NationalMarketValue.class)
                                .xpath("fmt:countryName(fmt:fromEnum(nationalMarket/value))")
                                .build()),
                    createColumn("Product_National_Comment",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(nationalComment/value)").build()),
                    createColumn("Product_Package_Type",
                        ColumnMetadata.builder(PackageTypeEnum.class).xpath("fmt:fromEnum(packageType/value)").build()),
                    createColumn("Product_Package_Units",
                        ColumnMetadata.builder(Integer.class).xpath("fmt:integer(packageUnits)").build()),
                    createColumn("Product_Package_Net_Weight",
                        ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(packageNetWeight)").build()),
                    createColumn("Product_Unit_Packet_Picture_File",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:joinAttachmentRef($ctxt, unitPacketPictureFiles/attachment)")
                                .build()),

                    // tnco
                    createColumn("Emission_Tar",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/tncoEmission/tar)")
                                .build()),
                    createColumn("Emission_Nicotine",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/tncoEmission/nicotine)")
                                .build()),
                    createColumn("Emission_CO",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/tncoEmission/co)")
                                .build()),
                    //
                    // Detail
                    createColumn("Submission_General_Comment",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:trim(/submission/generalComment/value)")
                                .build()),
                    createColumn("Product_Same_Composition_Other",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/sameCompositionProducts/productIdentification[1]/value")
                                .build()),
                    createColumn("Product_Length",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/length)")
                                .build()),
                    createColumn("Product_Diameter",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/diameter)")
                                .build()),
                    createColumn("Product_Weight",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/weight)")
                                .build()),
                    createColumn("Product_Tobacco_Weight",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/tobaccoWeight)")
                                .build()),
                    createColumn("Product_Filter",
                        ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(/submission/product/filter)").build()),
                    createColumn("Product_Filter_Length",
                        ColumnMetadata.builder(Integer.class)
                                .xpath("fmt:integer(/submission/product/filterLength)")
                                .build()),
                    createColumn("Product_Technical_File",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:joinAttachmentRef($ctxt, /submission/product/technicalFiles/attachment)")
                                .build()),
                    createColumn("Product_Market_Research_File",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinAttachmentRef( $ctxt, /submission/product/marketResearchFiles/attachment)")
                                .build()),

                    //
                    // Cigarette Specific
                    createColumn("Cigarette_Charecterising_Flavour",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/cigaretteSpecific/characterisingFlavour)")
                                .build()),
                    createColumn("Cigarette_Filter_Ventilation",
                        ColumnMetadata.builder(Integer.class)
                                .xpath("fmt:percentage(/submission/product/cigaretteSpecific/filterVentilation)")
                                .build()),
                    createColumn("Cigarette_Filter_Drop_Pressure_Closed",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/cigaretteSpecific/filterDropPressureClosed)")
                                .build()),
                    createColumn("Cigarette_Filter_Drop_Pressure_Open",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/cigaretteSpecific/filterDropPressureOpen)")
                                .build()),
                    // Ryo/Pipe
                    createColumn("Roll-your-own/pipe_Total_ Nicotine_Content",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/ryoPipeSpecific/totalNicotineContent)")
                                .build()),

                    // smokeless
                    createColumn("Smokeless_pH",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/smokelessSpecific/ph)")
                                .build()),
                    createColumn("Smokeless_Nicotine_Content",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/smokelessSpecific/totalNicotineContent)")
                                .build()),
                    //
                    //
                    createColumn("Sent_By", ColumnMetadata.builder(String.class).xpath("/sentBy").build()),
                    createColumn("PIR_Status", ColumnMetadata.builder(String.class).xpath("/pirStatus").build()),
                    createColumn("Latest_PIR_Status",
                        ColumnMetadata.builder(String.class).xpath("/latestPirStatus").build()),
                    createColumn("Last_Modified",
                        ColumnMetadata.builder(DateTime.class)
                                .xpath("fmt:fromDateTime(/lastModifiedDate)")
                                .format(DATETIME_FORMAT)
                                .build()),
                    createColumn("Year",
                        ColumnMetadata.builder(String.class).xpath("fmt:year(/lastModifiedDate)").build()),
                    createColumn("Month",
                        ColumnMetadata.builder(String.class).xpath("fmt:month(/lastModifiedDate)").build()),
                    createColumn("Latest_Submission_Flag",
                        ColumnMetadata.builder(Boolean.class).xpath("fmt:yesNo(/lastSubmittedSubmission)").build()))
                .build()));

    }

    public interface EcigSubmissionTracking {

        /** */
        @Nonnull
        public static final ExcelSheet TRACKING = ExcelSheet.create("E-Cigarettes Submission", 0);

        /** */
        @Nonnull
        public static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(GroupDescriptor
                .builder("Tracking", TRACKING)
                .columns(
                    createColumn("Internal_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                    // createColumn("Product_Category",
                    // ColumnMetadata.builder(String.class).xpath("fmt:productCategory(/submission/product)").build()),
                    createColumn("Product_ID",
                        ColumnMetadata.builder(String.class).xpath("/submission/product/productID/value").build()),
                    createColumn("Previous_Product_ID",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/previousProductID/value")
                                .build()),
                    // createColumn("Submitter_ID",
                    // ColumnMetadata.builder(String.class).xpath("/submission/submitter/submitterID").build()),
                    createColumn("Submission_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/submissionType/value)")
                                .build()),
                    createColumn("Status", ColumnMetadata.builder(String.class).xpath("/status").build()),
                    createColumn("TPD_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("/productNumber").build()),
                    createColumn("Product_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/product/productType/value)")
                                .build()),
                    createColumn("National_Markets",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinCountry(/submission/product/presentations/presentation/nationalMarket/value)")
                                .build()),

                    // Presentation
                    createColumn("Product_Brand_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandName/value)").build()),
                    createColumn("Product_Brand_Subtype_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandSubtypeName/value)").build()),
                    createColumn("Product_Launch_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(launchDate)").build()),
                    createColumn("Product_Withdrawal_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(withdrawalDate)").build()),
                    createColumn("Product_Submitter_Number",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(productNumber/value)").build()),
                    createColumn("Product_National_Market",
                        ColumnMetadata.builder(NationalMarketValue.class)
                                .xpath("fmt:countryName(fmt:fromEnum(nationalMarket/value))")
                                .build()),

                    //
                    //
                    createColumn("Product_National_Comment",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(nationalComment/value)").build()),
                    createColumn("Product_Package_Units",
                        ColumnMetadata.builder(Integer.class).xpath("fmt:integer(packageUnits)").build()),

                    // Detail
                    createColumn("Product_Weight",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/weight)")
                                .build()),
                    createColumn("Product_Volume",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/volume)")
                                .build()),
                    createColumn("Product_CLP_Classification",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:trim(/submission/product/clpClassification/value)")
                                .build()),
                    createColumn("Product_Studies_Summaries_File",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinAttachmentRef( $ctxt, /submission/product/studySummaryFiles/attachment)")
                                .build()),
                    createColumn("Product_Market_Research_File",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinAttachmentRef( $ctxt, /submission/product/marketResearchFiles/attachment)")
                                .build()),

                    //
                    // Design
                    createColumn("E-Cigarette_Description",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/design/description/value")
                                .build()),
                    createColumn("E-Cigarette_Identification_E-Cigarette_Device",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/design/identificationEcigDevice/value")
                                .build()),
                    createColumn("E-Cigarette_Liquid_Volume/Capacity",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/liquidVolumeCapacity)")
                                .build()),
                    createColumn("E-Cigarette_Nicotine_Concentration",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/nicotineConcentration)")
                                .build()),
                    createColumn("E-Cigarette_Battery_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:trim(/submission/product/design/batteryType/value)")
                                .build()),
                    createColumn("E-Cigarette_Battery_Type_Capacity",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/batteryCapacity)")
                                .build()),
                    createColumn("E-Cigarette_Volt/Watt_Adjustable",
                        ColumnMetadata.builder(VoltageWattageAdjustableEnum.class)
                                .xpath("fmt:fromEnum(/submission/product/design/voltageWattageAdjustable/value)")
                                .build()),
                    createColumn("E-Cigarette_Voltage",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/voltage)")
                                .build()),
                    createColumn("E-Cigarette_Voltage_Lower_Range",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/voltageLowerRange)")
                                .build()),
                    createColumn("E-Cigarette_Voltage_Upper_Range",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/voltageUpperRange)")
                                .build()),
                    createColumn("E-Cigarette_Wattage",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/wattage)")
                                .build()),
                    createColumn("E-Cigarette_Wattage_Lower_Range",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/wattageLowerRange)")
                                .build()),
                    createColumn("E-Cigarette_Wattage_Upper_Range",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/wattageUpperRange)")
                                .build()),
                    createColumn("E-Cigarette_Airflow_Adjustable",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/airflowAdjustable)")
                                .build()),
                    createColumn("E-Cigarette_Wick_Changeable",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/wickChangeable)")
                                .build()),
                    createColumn("E-Cigarette_Microprocessor",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/microprocessor)")
                                .build()),
                    createColumn("E-Cigarette_Coil_Composition",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:trim(/submission/product/design/coilComposition/value)")
                                .build()),
                    createColumn("E-Cigarette_Coil_Resistance",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/design/coilResistance)")
                                .build()),
                    createColumn("E-Cigarette_Nicotine_Dose/Uptake_File",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:att( $ctxt, /submission/product/design/nicotineDoseUptakeFile/attachmentID)")
                                .build()),
                    createColumn("E-Cigarette _Child_Tamper_Proof",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/childTamperProof)")
                                .build()),
                    createColumn("E-Cigarette_Production_File",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:att( $ctxt, /submission/product/design/productionFile/attachmentID)")
                                .build()),
                    createColumn("E-Cigarette_Production_Conformity",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/productionConformity)")
                                .build()),
                    createColumn("E-Cigarette_Quality_Safety",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/qualitySafety)")
                                .build()),
                    createColumn("E-Cigarette_High_Purity",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/highPurity)")
                                .build()),
                    createColumn("E-Cigarette_Non_Risk",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/nonRisk)")
                                .build()),
                    createColumn("E-Cigarette_Consistent_Dosing",
                        ColumnMetadata.builder(Boolean.class)
                                .xpath("fmt:bool(/submission/product/design/consistentDosing)")
                                .build()),
                    createColumn("E-Cigarette_Consistent_Dosing_Methods",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:att( $ctxt, /submission/product/design/consistentDosingMethodsFile/attachmentID)")
                                .build()),
                    createColumn("E-Cigarette_Opening/Refill_File",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:att( $ctxt, /submission/product/design/openingRefillFile/attachmentID)")
                                .build()),
                    createColumn("E-Cigarette_Leaflet_File",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:att( $ctxt, /submission/product/design/leafletFile/attachmentID)")
                                .build()),

                    //
                    // audit
                    createColumn("Sent_By", ColumnMetadata.builder(String.class).xpath("/sentBy").build()),
                    createColumn("PIR_Status", ColumnMetadata.builder(String.class).xpath("/pirStatus").build()),
                    createColumn("Latest_PIR_Status",
                        ColumnMetadata.builder(String.class).xpath("/latestPirStatus").build()),
                    createColumn("Last_Modified",
                        ColumnMetadata.builder(DateTime.class)
                                .xpath("fmt:fromDateTime(/lastModifiedDate)")
                                .format(DATETIME_FORMAT)
                                .build()),
                    createColumn("Year",
                        ColumnMetadata.builder(String.class).xpath("fmt:year(/lastModifiedDate)").build()),
                    createColumn("Month",
                        ColumnMetadata.builder(String.class).xpath("fmt:month(/lastModifiedDate)").build()),
                    createColumn("Latest_Submission_Flag",
                        ColumnMetadata.builder(Boolean.class).xpath("fmt:yesNo(/lastSubmittedSubmission)").build()))
                .build()));

    }

    public interface NovelTobaccoSubmissionTracking {

        /** */
        @Nonnull
        public static final ExcelSheet TRACKING = ExcelSheet.create("Novel Tobacco Submission", 0);

        /** */
        @Nonnull
        public static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(GroupDescriptor
                .builder("Tracking", TRACKING)
                .columns(
                    createColumn("Internal_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                    // createColumn("Product_Category",
                    // ColumnMetadata.builder(String.class).xpath("fmt:productCategory(/submission/product)").build()),
                    createColumn("Product_ID",
                        ColumnMetadata.builder(String.class).xpath("/submission/product/productID/value").build()),
                    createColumn("Previous_Product_ID",
                        ColumnMetadata.builder(String.class)
                                .xpath("/submission/product/previousProductID/value")
                                .build()),
                    // createColumn("Submitter_ID",
                    // ColumnMetadata.builder(String.class).xpath("/submission/submitter/submitterID").build()),
                    createColumn("Submission_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/submissionType/value)")
                                .build()),
                    createColumn("Status", ColumnMetadata.builder(String.class).xpath("/status").build()),
                    createColumn("TPD_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("/productNumber").build()),
                    createColumn("Product_Type",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:fromEnumToString(/submission/product/productType/value)")
                                .build()),
                    createColumn("National_Markets",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:joinCountry(/submission/product/presentations/presentation/nationalMarket/value)")
                                .build()),

                    // Detail
                    createColumn("Product_Weight",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/weight)")
                                .build()),
                    createColumn("Product_Tobacco_Weight",
                        ColumnMetadata.builder(BigDecimal.class)
                                .xpath("fmt:decimal(/submission/product/tobaccoWeight)")
                                .build()),

                    //
                    // Presentation
                    createColumn("Product_Brand_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandName/value)").build()),
                    createColumn("Product_Brand_Subtype_Name",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(brandSubtypeName/value)").build()),
                    createColumn("Product_Launch_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(launchDate)").build()),
                    createColumn("Product_Withdrawal_Date",
                        ColumnMetadata.builder(String.class).xpath("fmt:fromDate(withdrawalDate)").build()),
                    createColumn("Product_Submitter_Number",
                        ColumnMetadata.builder(String.class).xpath("fmt:trim(productNumber/value)").build()),
                    createColumn("Product_National_Market",
                        ColumnMetadata.builder(NationalMarketValue.class)
                                .xpath("fmt:countryName(fmt:fromEnum(nationalMarket/value))")
                                .build()),
                    createColumn("Product_Package_Type",
                        ColumnMetadata.builder(PackageTypeEnum.class).xpath("fmt:fromEnum(packageType/value)").build()),
                    createColumn("Product_Package_Units",
                        ColumnMetadata.builder(Integer.class).xpath("fmt:integer(packageUnits)").build()),
                    createColumn("Product_Package_Net_Weight",
                        ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(packageNetWeight)").build()),

                    //
                    //
                    createColumn("Sent_By", ColumnMetadata.builder(String.class).xpath("/sentBy").build()),
                    createColumn("PIR_Status", ColumnMetadata.builder(String.class).xpath("/pirStatus").build()),
                    createColumn("Latest_PIR_Status",
                        ColumnMetadata.builder(String.class).xpath("/latestPirStatus").build()),
                    createColumn("Last_Modified",
                        ColumnMetadata.builder(DateTime.class)
                                .xpath("fmt:fromDateTime(/lastModifiedDate)")
                                .format(DATETIME_FORMAT)
                                .build()),
                    createColumn("Year",
                        ColumnMetadata.builder(String.class).xpath("fmt:year(/lastModifiedDate)").build()),
                    createColumn("Month",
                        ColumnMetadata.builder(String.class).xpath("fmt:month(/lastModifiedDate)").build()),
                    createColumn("Latest_Submission_Flag",
                        ColumnMetadata.builder(Boolean.class).xpath("fmt:yesNo(/lastSubmittedSubmission)").build()),

                    //
                    // Novel
                    createColumn("Novel_Details_Description_File",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:att($ctxt, /submission/product/novelSpecific/detailsDescriptionFile/attachmentID)")
                                .build()),
                    createColumn("Novel_Use_Instructions_File",
                        ColumnMetadata.builder(String.class)
                                .xpath(
                                    "fmt:att($ctxt, /submission/product/novelSpecific/useInstructionsFile/attachmentID)")
                                .build()))
                .build()));

    }
}
