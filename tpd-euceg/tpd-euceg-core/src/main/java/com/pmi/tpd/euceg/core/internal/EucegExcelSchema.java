package com.pmi.tpd.euceg.core.internal;

import static com.pmi.tpd.euceg.core.excel.ColumnDescriptor.createColumn;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.eu.ceg.CountryValue;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.EmissionNameEnum;
import org.eu.ceg.IngredientCategoryEnum;
import org.eu.ceg.IngredientFunctionEnum;
import org.eu.ceg.LeafCureMethodEnum;
import org.eu.ceg.LeafTypeEnum;
import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.PackageTypeEnum;
import org.eu.ceg.PartTypeEnum;
import org.eu.ceg.ReachRegistrationEnum;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.eu.ceg.ToxicityStatusEnum;
import org.eu.ceg.ToxicologicalDataAvailableEnum;
import org.eu.ceg.VoltageWattageAdjustableEnum;

import com.pmi.tpd.euceg.core.excel.ColumnMetadata;
import com.pmi.tpd.euceg.core.excel.ExcelSheet;
import com.pmi.tpd.euceg.core.excel.ExcelSheet.ConvertType;
import com.pmi.tpd.euceg.core.excel.GroupDescriptor;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;

public final class EucegExcelSchema {

    public static boolean isExportedSheets(final ExcelSheet sheet) {
        return ConvertType.exportExcel.equals(sheet.getConvertType())
                || ConvertType.both.equals(sheet.getConvertType());
    }

    public static boolean isImportedSheets(final ExcelSheet sheet) {
        return ConvertType.importExcel.equals(sheet.getConvertType())
                || ConvertType.both.equals(sheet.getConvertType());
    }

    public interface TobaccoProduct {

        /** */
        @Nonnull
        public static final ExcelSheet PRODUCT_DETAILS_SHEET = ExcelSheet.create("Product Detail", 0, true);

        /** */
        @Nonnull
        public static final ExcelSheet MARKET_RESEARCH_SHEET = ExcelSheet.create("Market Research", 1);

        /** */
        @Nonnull
        public static final ExcelSheet PRESENTATION_SHEET = ExcelSheet.create("Presentation", 2);

        /** */
        @Nonnull
        public static final ExcelSheet INGREDIENT_SHEET = ExcelSheet.create("Ingredient", 3);

        /** */
        @Nonnull
        public static final ExcelSheet OTHER_INGREDIENT_SHEET = ExcelSheet.create("Other Ingredient", 4);

        /** */
        @Nonnull
        public static final ExcelSheet OTHER_EMISSION_SHEET = ExcelSheet.create("Other Emission", 5);

        /** */
        @Nonnull
        public static final ExcelSheet CIGARETTE_SPEC_SHEET = ExcelSheet.create("Cigarette Specification", 6);

        /** */
        @Nonnull
        public static final ExcelSheet SMOKELESS_SPEC_SHEET = ExcelSheet.create("Smokeless Specification", 7);

        /** */
        @Nonnull
        public static final ExcelSheet ROLL_OWN_SPEC_SHEET = ExcelSheet.create("Roll Own Specification", 8);

        /** */
        @Nonnull
        public static final ExcelSheet NOVEL_SPEC_SHEET = ExcelSheet.create("Novel Specification", 9);

        /** */
        @Nonnull
        public static final ExcelSheet ADDITIONNAL_SHEET = ExcelSheet
                .create("Additionnal Information", 10, false, ConvertType.exportExcel);

        /** */
        @Nonnull
        public static final ExcelSheet SUMMARIZE_SHEET = ExcelSheet
                .create("Export Summarize", 11, false, ConvertType.exportExcel);

        /** */
        @Nonnull
        public static ExcelSheet[] SHEETS = { PRODUCT_DETAILS_SHEET, MARKET_RESEARCH_SHEET, PRESENTATION_SHEET,
                INGREDIENT_SHEET, OTHER_INGREDIENT_SHEET, OTHER_EMISSION_SHEET, CIGARETTE_SPEC_SHEET,
                SMOKELESS_SPEC_SHEET, ROLL_OWN_SPEC_SHEET, NOVEL_SPEC_SHEET, ADDITIONNAL_SHEET, SUMMARIZE_SHEET };

        @Nonnull
        public static List<ExcelSheet> getExportedSheets() {
            return Stream.of(SHEETS)
                    .filter(EucegExcelSchema::isExportedSheets)
                    .collect(Collectors.toUnmodifiableList());
        }

        @Nonnull
        public static List<ExcelSheet> getImportedSheets() {
            return Stream.of(SHEETS)
                    .filter(EucegExcelSchema::isImportedSheets)
                    .collect(Collectors.toUnmodifiableList());
        }

        /** Root group descriptor for tobacco product. */
        @Nonnull
        public static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(
            GroupDescriptor.builder("ProductDetail", PRODUCT_DETAILS_SHEET)
                    // TODO: normally submitterid is primary key, but MarketResearchFile group doesn't contain this
                    // field.
                    .keys(
                        // createColumn("Submitter_ID",
                        // ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Submission_Type",
                            ColumnMetadata.builder(SubmissionTypeEnum.class).xpath("$submissionType").build()),
                        createColumn("Submission_General_Comment",
                            ColumnMetadata.builder(String.class).xpath("$generalComment").build()),
                        createColumn("TPD_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$productNumber").build()),
                        createColumn("Previous_TPD_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$previousProductNumber").build()),
                        createColumn("Product_ID_Other_Exist",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:has(otherProducts/productIdentification/value)")
                                    .defaultValue(false)
                                    .build()),
                        createColumn("Product_ID_Other",
                            ColumnMetadata.builder(String.class)
                                    .xpath("otherProducts/productIdentification[1]/value")
                                    .build()),
                        createColumn("Product_Same_Composition_Exist",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:has(sameCompositionProducts/productIdentification/value)")
                                    .defaultValue(false)
                                    .build()),
                        createColumn("Product_Same_Composition_Other",
                            ColumnMetadata.builder(String.class)
                                    .xpath("sameCompositionProducts/productIdentification[1]/value")
                                    .build()),
                        createColumn("Product Type",
                            ColumnMetadata.builder(TobaccoProductTypeEnum.class)
                                    .xpath("fmt:fromEnum(productType/value)")
                                    .build()),
                        createColumn("Product_Length",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(length)").build()),
                        createColumn("Product_Diameter",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(diameter)").build()),
                        createColumn("Product_Weight",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(weight)").build()),
                        createColumn("Product_Tobacco Weight",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(tobaccoWeight)").build()),
                        createColumn("Product_Filter",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(filter)").build()),
                        createColumn("Product_Filter_Length",
                            ColumnMetadata.builder(Integer.class).xpath("fmt:integer(filterLength)").build()),
                        createColumn("Product_Technical_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, technicalFiles/attachment)")
                                    .build()))
                    .children(GroupDescriptor.builder("Manufacturer", PRODUCT_DETAILS_SHEET)
                            .keys(
                                createColumn("Manufacturer_ID",
                                    ColumnMetadata.builder(String.class).nullable(true).xpath("submitterID").build()),
                                createColumn("Company_Name",
                                    ColumnMetadata.builder(String.class).nullable(true).xpath("name").build()))
                            .columns(createColumn(
                                "Manufacturer_Has_ID",
                                ColumnMetadata
                                        .builder(Boolean.class)
                                        .xpath("fmt:has(submitterID)")
                                        .defaultValue(false)
                                        .build()),
                                createColumn("Company_Address",
                                    ColumnMetadata.builder(String.class).xpath("address").build()),
                                createColumn("Company_Country",
                                    ColumnMetadata.builder(CountryValue.class).xpath("fmt:country(country)").build()),
                                createColumn("Company_Phone",
                                    ColumnMetadata.builder(String.class).xpath("phoneNumber").build()),
                                createColumn("Company_Email",
                                    ColumnMetadata.builder(String.class).xpath("email").build()))
                            .child(GroupDescriptor.builder("ProductionSite", PRODUCT_DETAILS_SHEET)
                                    .keys(createColumn("Production_Site_Address",
                                        ColumnMetadata.builder(String.class).xpath("address").build()))
                                    .columns(
                                        createColumn("Production_Site_Country",
                                            ColumnMetadata.builder(CountryValue.class)
                                                    .xpath("fmt:country(country)")
                                                    .build()),
                                        createColumn("Production_Site_Phone",
                                            ColumnMetadata.builder(String.class).xpath("phoneNumber").build()),
                                        createColumn("Production_Site_Email",
                                            ColumnMetadata.builder(String.class).xpath("email").build()))
                                    .build())
                            .build())
                    .build(),
            GroupDescriptor.builder("MarketResearchFile", MARKET_RESEARCH_SHEET)
                    .keys(createColumn("Internal_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .column(createColumn("Product_Market_Research_File",
                        ColumnMetadata.builder(String.class)
                                .xpath("fmt:att($ctxt, marketResearchFiles/attachment[$i]/attachmentID)")
                                .build()))
                    .build(),
            GroupDescriptor.builder("Presentation", PRESENTATION_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                        // product submitter number can be different for same national market
                        createColumn("Product_Submitter_Number",
                            ColumnMetadata.builder(String.class).xpath("productNumber/value").build()),
                        createColumn("Product_National_Market",
                            ColumnMetadata.builder(NationalMarketValue.class)
                                    .xpath("fmt:fromEnum(nationalMarket/value)")
                                    .build()))
                    .columns(
                        createColumn("Product_National_Comment",
                            ColumnMetadata.builder(String.class).xpath("nationalComment/value").build()),
                        createColumn("Product_Brand_Name",
                            ColumnMetadata.builder(String.class).xpath("brandName/value").build()),
                        createColumn("Product_Brand_Subtype_Name",
                            ColumnMetadata.builder(String.class).xpath("brandSubtypeName/value").build()),
                        createColumn("Product_Launch_Date",
                            ColumnMetadata.builder(String.class).xpath("fmt:fromDate(launchDate)").build()),
                        createColumn("Product_Withdrawal_Indication",
                            ColumnMetadata.builder(String.class).xpath("fmt:bool(withdrawalIndication)").build()),
                        createColumn("Product_Withdrawal_Date",
                            ColumnMetadata.builder(String.class).xpath("fmt:fromDate(withdrawalDate)").build()),
                        createColumn("Product_UPC_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_EAN_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_GTIN_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_SKU_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_Package_Type",
                            ColumnMetadata.builder(PackageTypeEnum.class)
                                    .xpath("fmt:fromEnum(packageType/value)")
                                    .build()),
                        createColumn("Product_Package_Units",
                            ColumnMetadata.builder(Integer.class).xpath("fmt:integer(packageUnits)").build()),
                        createColumn("Product_Package_Net_Weight",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(packageNetWeight)").build()),
                        createColumn("Product_Other_Market_Has_Data",
                            ColumnMetadata.builder(String.class).xpath("fmt:has(otherMarketData/attachment)").build()),
                        createColumn("Product_Other_Market_Data",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, otherMarketData/attachment)")
                                    .build()),
                        createColumn("Product_Unit_Packet_Picture_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, unitPacketPictureFiles/attachment)")
                                    .build()))
                    .child(GroupDescriptor.builder("SaleData", PRESENTATION_SHEET)
                            .keys(createColumn("Product_Sales_Volume_Year",
                                ColumnMetadata.builder(Integer.class).xpath("year/value").build()))
                            .columns(
                                createColumn("Product_Maximum_Sales_Price",
                                    ColumnMetadata.builder(BigDecimal.class)
                                            .xpath("fmt:decimal(maximumSalesPrice)")
                                            .build()),
                                createColumn("Product_Sales_Volume",
                                    ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(salesVolume)").build()))
                            .build())
                    .build(),
            GroupDescriptor.builder("Ingredient", INGREDIENT_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                        createColumn("Tobacco_Part_Type",
                            ColumnMetadata.builder(PartTypeEnum.class).xpath("fmt:fromEnum(partType/value)").build()),
                        createColumn("Tobacco_Leaf_Type",
                            ColumnMetadata.builder(LeafTypeEnum.class).xpath("fmt:fromEnum(leafType/value)").build()),
                        createColumn("Tobacco_Leaf_Cure_Method",
                            ColumnMetadata.builder(LeafCureMethodEnum.class)
                                    .xpath("fmt:fromEnum(leafCureMethod/value)")
                                    .build()))
                    .columns(
                        createColumn("Tobacco_Part_Type_Other",
                            ColumnMetadata.builder(String.class).xpath("partTypeOther/value").build()),
                        createColumn("Tobacco_Leaf_Type_Other",
                            ColumnMetadata.builder(String.class).xpath("leafTypeOther/value").build()),
                        createColumn("Tobacco_Leaf_Cure_Method _Other",
                            ColumnMetadata.builder(String.class).xpath("leafCureMethodOther/value").build()),
                        createColumn("Tobacco_Quantity",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(quantity)").build()),
                        createColumn("Tobacco_Part_Description_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, partDescriptionFiles/attachment)")
                                    .build()))
                    .children(GroupDescriptor.builder("Supplier", INGREDIENT_SHEET)
                            .keys(
                                createColumn("Supplier_ID",
                                    ColumnMetadata.builder(String.class).nullable(true).xpath("submitterID").build()),
                                createColumn("Company_Name",
                                    ColumnMetadata.builder(String.class).nullable(true).xpath("name").build()))
                            .columns(
                                createColumn("Supplier_Has_ID",
                                    ColumnMetadata.builder(String.class).xpath("fmt:has(submitterID)").build()),
                                createColumn("Company_Address",
                                    ColumnMetadata.builder(String.class).xpath("address").build()),
                                createColumn("Company_Country",
                                    ColumnMetadata.builder(CountryValue.class).xpath("fmt:country(country)").build()),
                                createColumn("Company_Phone",
                                    ColumnMetadata.builder(String.class).xpath("phoneNumber").build()),
                                createColumn("Company_Email",
                                    ColumnMetadata.builder(String.class).xpath("email").build()))
                            .build())
                    .build(),
            GroupDescriptor.builder("OtherIngredient", OTHER_INGREDIENT_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                        createColumn("Ingredient_Name",
                            ColumnMetadata.builder(String.class).xpath("name/value").build()))
                    .columns(
                        createColumn("Confidential",
                            ColumnMetadata.builder(Boolean.class).xpath("name/confidential").build()),
                        createColumn("Ingredient_Category",
                            ColumnMetadata.builder(IngredientCategoryEnum.class)
                                    .xpath("fmt:fromEnum(category/value)")
                                    .build()),
                        createColumn("Ingredient_Category_Other",
                            ColumnMetadata.builder(String.class).xpath("categoryOther/value").build()),
                        createColumn("Ingredient_CAS_Exist",
                            ColumnMetadata.builder(String.class).xpath("fmt:bool(casNumberExists)").build()),
                        createColumn("Ingredient_CAS",
                            ColumnMetadata.builder(String.class).xpath("casNumber/value").build()),
                        createColumn("Ingredient_CAS_Additional",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:join(additionalCasNumbers/casNumber/value)")
                                    .build()),
                        createColumn("Ingredient_FEMA_Number",
                            ColumnMetadata.builder(String.class).xpath("femaNumber/value").build()),
                        createColumn("Ingredient_Additive_Number",
                            ColumnMetadata.builder(String.class).xpath("additiveNumber/value").build()),
                        createColumn("Ingredient_FL_Number",
                            ColumnMetadata.builder(String.class).xpath("flNumber/value").build()),
                        createColumn("Ingredient_EC_Number",
                            ColumnMetadata.builder(String.class).xpath("ecNumber/value").build()),
                        createColumn("Ingredient_Other_Number",
                            ColumnMetadata.builder(String.class).xpath("otherNumber/value").build()),
                        createColumn("Ingredient_Quantity_Fluctuate",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(quantityFluctuate)").build()),
                        createColumn("Ingredient_Recipe_Quantity",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(recipeQuantity)").build()),
                        createColumn("Ingredient_Recipe_Range_Min_Level",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(recipeRangeMinLevel)").build()),
                        createColumn("Ingredient_Recipe_Range_Max_Level",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(recipeRangeMaxLevel)").build()),
                        createColumn("Ingredient_Measured_Mean_Quantity",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(measuredMeanQuantity)")
                                    .build()),
                        createColumn("Ingredient_Measured_SD",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(measuredSd)").build()),
                        createColumn("Ingredient_Measured_Min_Level",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(measuredMinLevel)").build()),
                        createColumn("Ingredient_Measured_Max_Level",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(measuredMaxLevel)").build()),
                        createColumn("Ingredient_Measured_Number",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(measuredNumber)").build()),
                        createColumn("Ingredient_Function",
                            ColumnMetadata.builder(IngredientFunctionEnum.class)
                                    .xpath("fmt:fromEnum(functions/function/value)")
                                    .build()),
                        createColumn("Ingredient_Function_Other",
                            ColumnMetadata.builder(String.class).xpath("functionOther/value").build()),
                        createColumn("Ingredient_Priority_Additive",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(priorityAdditive)").build()),
                        createColumn("Ingredient_Priority_Additive_Files",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef( $ctxt, priorityAdditiveFiles/attachment )")
                                    .build()),
                        createColumn("Ingredient_Unburnt_Status",
                            ColumnMetadata.builder(ToxicityStatusEnum.class)
                                    .xpath("fmt:fromEnum(toxicityStatus/value)")
                                    .build()),
                        createColumn("Ingredient_REACH_Registration",
                            ColumnMetadata.builder(ReachRegistrationEnum.class)
                                    .xpath("fmt:fromEnum(reachRegistration/value)")
                                    .build()),
                        createColumn("Ingredient_REACH_Registration_Number",
                            ColumnMetadata.builder(String.class).xpath("reachRegistrationNumber/value").build()),
                        createColumn("Ingredient_CLP_Whether_Classification",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(clpWhetherClassification)").build()),
                        createColumn("Ingredient_CLP_Acute_Tox_Oral",
                            ColumnMetadata.builder(String.class).xpath("clpAcuteToxOral/value").build()),
                        createColumn("Ingredient_CLP_Acute_Tox_Dermal",
                            ColumnMetadata.builder(String.class).xpath("clpAcuteToxDermal/value").build()),
                        createColumn("Ingredient_CLP_Acute_Tox_Inhalation",
                            ColumnMetadata.builder(String.class).xpath("clpAcuteToxInhalation/value").build()),
                        createColumn("Ingredient_CLP_Skin_Corrosive/Irritant",
                            ColumnMetadata.builder(String.class).xpath("clpSkinCorrosiveIrritant/value").build()),
                        createColumn("Ingredient_CLP_Eye_Damage/Irritation",
                            ColumnMetadata.builder(String.class).xpath("clpEyeDamageIrritation/value").build()),
                        createColumn("Ingredient_CLP_Respiratory_Sensitisation",
                            ColumnMetadata.builder(String.class).xpath("clpRespiratorySensitisation/value").build()),
                        createColumn("Ingredient_CLP_Skin_Sensitisation",
                            ColumnMetadata.builder(String.class).xpath("clpSkinSensitisation/value").build()),
                        createColumn("Ingredient_CLP_Mutagen/Genotox",
                            ColumnMetadata.builder(String.class).xpath("clpMutagenGenotox/value").build()),
                        createColumn("Ingredient_CLP_Carcinogenity",
                            ColumnMetadata.builder(String.class).xpath("clpCarcinogenicity/value").build()),
                        createColumn("Ingredient_CLP_Reproductive_Tox",
                            ColumnMetadata.builder(String.class).xpath("clpReproductiveTox/value").build()),
                        createColumn("Ingredient_CLP_STOT",
                            ColumnMetadata.builder(String.class).xpath("clpStot/value").build()),
                        createColumn("Ingredient_CLP_STOT_Description",
                            ColumnMetadata.builder(String.class).xpath("clpStotDescription/value").build()),
                        createColumn("Ingredient_CLP_Aspiration_Tox",
                            ColumnMetadata.builder(String.class).xpath("clpAspirationTox/value").build()),
                        createColumn("Ingredient_Tox_Data",
                            ColumnMetadata.builder(ToxicologicalDataAvailableEnum.class)
                                    .xpath("fmt:fromEnum(toxicologicalDetails/toxicologicalDataAvailable/value)")
                                    .build()),
                        createColumn("Ingredient_Tox_Emission",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxEmission)")
                                    .build()),
                        createColumn("Ingredient_Tox_CMR",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxCmr)")
                                    .build()),
                        createColumn("Ingredient_Tox_CardioPulmonary",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxCardioPulmonary)")
                                    .build()),
                        createColumn("Ingredient_Tox_Addictive",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxAddictive)")
                                    .build()),
                        createColumn("Ingredient_Tox_Other",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxOther)")
                                    .build()),
                        createColumn("Ingredient_Tox_Emission_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef($ctxt, toxicologicalDetails/toxEmissionFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_CMR_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, toxicologicalDetails/toxCmrFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_CardioPulmonary_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef($ctxt, toxicologicalDetails/toxCardioPulmonaryFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_Addictive_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef($ctxt, toxicologicalDetails/toxAddictiveFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_Other_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef($ctxt, toxicologicalDetails/toxOtherFiles/attachment)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("Emission", OTHER_EMISSION_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                        createColumn("Emission_Other_Name",
                            ColumnMetadata.builder(String.class).nullable(true).xpath("name/value").build()))
                    .columns(
                        createColumn("Emission_Tar",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("root$fmt:decimal(/product/tncoEmission/tar)")
                                    .build()),
                        createColumn("Emission_Nicotine",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("root$fmt:decimal(/product/tncoEmission/nicotine)")
                                    .build()),
                        createColumn("Emission_CO",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("root$fmt:decimal(/product/tncoEmission/co)")
                                    .build()),
                        createColumn("Emission_TNCO_Lab",
                            ColumnMetadata.builder(String.class)
                                    .xpath("root$/product/tncoEmission/laboratories/laboratory[1]/value")
                                    .build()),
                        createColumn("Emission_Other_Available",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:has(../emission)").build()),
                        createColumn("Emission_Other_CAS",
                            ColumnMetadata.builder(String.class).xpath("casNumber/value").build()),
                        createColumn("Emission_Other_IUPAC",
                            ColumnMetadata.builder(String.class).xpath("iupacName/value").build()),
                        createColumn("Emission_Other_Quantity",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(quantity)").build()),
                        createColumn("Emission_Other_Units",
                            ColumnMetadata.builder(String.class).xpath("unit/value").build()),
                        createColumn("Emission_Other_Method_Files",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, methodsFile/attachment)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("Cigarette", CIGARETTE_SPEC_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("Cigarette_Charecterising_Flavour",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(characterisingFlavour)").build()),
                        createColumn("Cigarette_Filter_Ventilation",
                            ColumnMetadata.builder(Integer.class).xpath("fmt:percentage(filterVentilation)").build()),
                        createColumn("Cigarette_Filter_Drop_Pressure_Closed",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(filterDropPressureClosed)")
                                    .build()),
                        createColumn("Cigarette_Filter_Drop_Pressure_Open",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(filterDropPressureOpen)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("Smokeless", SMOKELESS_SPEC_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("Smokeless_pH",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(ph)").build()),
                        createColumn("Smokeless_Total_Moisture",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(totalMoisture)").build()),
                        createColumn("Smokeless_Nicotine_Content",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(totalNicotineContent)")
                                    .build()),
                        createColumn("Smokeless_Unionised_Nicotine_Content",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(unionisedNicotineContent)")
                                    .build()),
                        createColumn("Smokeless_Analysis_Methods",
                            ColumnMetadata.builder(String.class).xpath("analysisMethods/value").build()))
                    .build(),
            GroupDescriptor.builder("RollOwn", ROLL_OWN_SPEC_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(createColumn("Roll-your-own/pipe_Total_ Nicotine_Content",
                        ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(totalNicotineContent)").build()),
                        createColumn("Roll-your-own/pipe_Unionised_Nicotine_Content",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(unionisedNicotineContent)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("Novel", NOVEL_SPEC_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("Novel_Details_Description_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att($ctxt, detailsDescriptionFile/attachmentID)")
                                    .build()),
                        createColumn("Novel_Use_Instructions_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att($ctxt, useInstructionsFile/attachmentID)")
                                    .build()),
                        createColumn("Novel_Risk/Benefit_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att($ctxt, riskBenefitFile/attachmentID)")
                                    .build()),
                        createColumn("Novel_Study",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, studyFiles/attachment)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("AdditionnalInformation", ADDITIONNAL_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(createColumn("STATUS", ColumnMetadata.builder(String.class).xpath("$status").build()),
                        createColumn("PIR_STATUS", ColumnMetadata.builder(String.class).xpath("$pirStatus").build()),
                        createColumn("CREATED_BY", ColumnMetadata.builder(String.class).xpath("$createdBy").build()),
                        createColumn("CREATED_DATE",
                            ColumnMetadata.builder(String.class).xpath("$createdDate").build()),
                        createColumn("LAST_MODIFIED_BY",
                            ColumnMetadata.builder(String.class).xpath("$lastModifiedBy").build()),
                        createColumn("LAST_MODIFIED_DATE",
                            ColumnMetadata.builder(String.class).xpath("$lastModifiedDate").build()))
                    .build(),
            GroupDescriptor.builder("ExportSummarize", SUMMARIZE_SHEET)
                    .columns(createColumn("NUMBER_OF_PRODUCT", ColumnMetadata.builder(Integer.class).build()),
                        createColumn("USED_FILTER", ColumnMetadata.builder(String.class).build()))
                    .build()));
    }

    public interface EcigProduct {

        /** */
        @Nonnull
        public static final ExcelSheet PRODUCT_DETAILS_SHEET = ExcelSheet.create("Product Details", 0, true);

        /** */
        @Nonnull
        public static final ExcelSheet ATTACHMENT_SHEET = ExcelSheet.create("Attachment", 1);

        /** */
        @Nonnull
        public static final ExcelSheet PRESENTATION_SHEET = ExcelSheet.create("Presentation", 2);

        /** */
        @Nonnull
        public static final ExcelSheet INGREDIENT_SHEET = ExcelSheet.create("Ingredient", 3);

        /** */
        @Nonnull
        public static final ExcelSheet EMISSION_SHEET = ExcelSheet.create("Emission", 4);

        /** */
        @Nonnull
        public static final ExcelSheet DESIGN_SHEET = ExcelSheet.create("Design", 5);

        /** */
        @Nonnull
        public static final ExcelSheet ADDITIONNAL_SHEET = ExcelSheet
                .create("Additionnal Information", 6, false, ConvertType.exportExcel);

        /** */
        @Nonnull
        public static final ExcelSheet SUMMARIZE_SHEET = ExcelSheet
                .create("Export Summarize", 7, false, ConvertType.exportExcel);

        @Nonnull
        public static ExcelSheet[] SHEETS = { PRODUCT_DETAILS_SHEET, ATTACHMENT_SHEET, PRESENTATION_SHEET,
                INGREDIENT_SHEET, EMISSION_SHEET, DESIGN_SHEET, ADDITIONNAL_SHEET, SUMMARIZE_SHEET };

        @Nonnull
        public static List<ExcelSheet> getExportedSheets() {
            return Stream.of(SHEETS)
                    .filter(sheet -> ConvertType.exportExcel.equals(sheet.getConvertType())
                            || ConvertType.both.equals(sheet.getConvertType()))
                    .collect(Collectors.toUnmodifiableList());
        }

        @Nonnull
        public static List<ExcelSheet> getImportedSheets() {
            return Stream.of(SHEETS)
                    .filter(sheet -> ConvertType.importExcel.equals(sheet.getConvertType())
                            || ConvertType.both.equals(sheet.getConvertType()))
                    .collect(Collectors.toUnmodifiableList());
        }

        /** Root group descriptor for e-cigarette product. */
        @Nonnull
        public static final ListDescriptor DESCRIPTORS = new ListDescriptor(Arrays.asList(
            GroupDescriptor.builder("ProductDetail", PRODUCT_DETAILS_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(createColumn(
                        "TPD_Product_Number",
                        ColumnMetadata.builder(String.class).xpath("$productNumber").build()),
                        createColumn("Previous_TPD_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$previousProductNumber").build()),
                        createColumn("Submission_Type",
                            ColumnMetadata.builder(SubmissionTypeEnum.class).xpath("$submissionType").build()),
                        createColumn("Submission_General_Comment",
                            ColumnMetadata.builder(String.class).xpath("$generalComment").build()),
                        createColumn("Product_ID_Other_Exist",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:has(otherProducts/productIdentification/value)")
                                    .defaultValue(false)
                                    .build()),
                        createColumn("Product_ID_Other",
                            ColumnMetadata.builder(String.class)
                                    .xpath("otherProducts/productIdentification[1]/value")
                                    .build()),
                        createColumn("Product_Same_Composition_Exist",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:has(sameCompositionProducts/productIdentification/value)")
                                    .defaultValue(false)
                                    .build()),
                        createColumn("Product_Same_Composition_Other",
                            ColumnMetadata.builder(String.class)
                                    .xpath("sameCompositionProducts/productIdentification[1]/value")
                                    .build()),
                        createColumn("Product Type",
                            ColumnMetadata.builder(EcigProductTypeEnum.class)
                                    .xpath("fmt:fromEnum(productType/value)")
                                    .build()),
                        createColumn("Product_Weight_E-Liquid",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(weight)").build()),
                        createColumn("Product_Volume_E-Liquid",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(volume)").build()),
                        createColumn("Product_CLP_Classification",
                            ColumnMetadata.builder(String.class).xpath("clpClassification/value").build()))
                    .children(GroupDescriptor.builder("Manufacturer", PRODUCT_DETAILS_SHEET)
                            .keys(
                                createColumn("Manufacturer_ID",
                                    ColumnMetadata.builder(String.class).nullable(true).xpath("submitterID").build()),
                                createColumn("Company_Name",
                                    ColumnMetadata.builder(String.class).nullable(true).xpath("name").build()))
                            .columns(createColumn(
                                "Manufacturer_Has_ID",
                                ColumnMetadata.builder(Boolean.class).xpath("fmt:has(submitterID)").build()),
                                createColumn("Company_Address",
                                    ColumnMetadata.builder(String.class).xpath("address").build()),
                                createColumn("Company_Country",
                                    ColumnMetadata.builder(CountryValue.class).xpath("fmt:country(country)").build()),
                                createColumn("Company_Phone",
                                    ColumnMetadata.builder(String.class).xpath("phoneNumber").build()),
                                createColumn("Company_Email",
                                    ColumnMetadata.builder(String.class).xpath("email").build()))
                            .child(GroupDescriptor.builder("ProductionSite", PRODUCT_DETAILS_SHEET)
                                    .keys(createColumn("Production_Site_Address",
                                        ColumnMetadata.builder(String.class).xpath("address").build()))
                                    .columns(
                                        createColumn("Production_Site_Country",
                                            ColumnMetadata.builder(CountryValue.class)
                                                    .xpath("fmt:country(country)")
                                                    .build()),
                                        createColumn("Production_Site_Phone",
                                            ColumnMetadata.builder(String.class).xpath("phoneNumber").build()),
                                        createColumn("Production_Site_Email",
                                            ColumnMetadata.builder(String.class).xpath("email").build()))
                                    .build())
                            .build())
                    .build(),
            GroupDescriptor.builder("Attachment", ATTACHMENT_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("Product_Studies_Summaries_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef( $ctxt, studySummaryFiles/attachment)")
                                    .build()),
                        createColumn("Product_Market_Research_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef( $ctxt, marketResearchFiles/attachment)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("Presentation", PRESENTATION_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                        // product submitter number can be different for same national market
                        createColumn("Product_Submitter_Number",
                            ColumnMetadata.builder(String.class).xpath("productNumber/value").build()),
                        createColumn("Product_National_Market",
                            ColumnMetadata.builder(NationalMarketValue.class)
                                    .xpath("fmt:fromEnum(nationalMarket/value)")
                                    .build()))
                    .columns(
                        createColumn("Product_National_Comment",
                            ColumnMetadata.builder(String.class).xpath("nationalComment/value").build()),
                        createColumn("Product_Brand_Name",
                            ColumnMetadata.builder(String.class).xpath("brandName/value").build()),
                        createColumn("Product_Brand_Subtype_Name_Exist",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(brandSubtypeNameExists)").build()),
                        createColumn("Product_Brand_Subtype_Name",
                            ColumnMetadata.builder(String.class).xpath("brandSubtypeName/value").build()),
                        createColumn("Product_Launch_Date",
                            ColumnMetadata.builder(String.class).xpath("fmt:fromDate(launchDate)").build()),
                        createColumn("Product_Withdrawal_Indication",
                            ColumnMetadata.builder(String.class).xpath("fmt:bool(withdrawalIndication)").build()),
                        createColumn("Product_Withdrawal_Date",
                            ColumnMetadata.builder(String.class).xpath("fmt:fromDate(withdrawalDate)").build()),
                        createColumn("Product_Submitter_Number",
                            ColumnMetadata.builder(String.class).xpath("productNumber/value").build()),
                        createColumn("Product_UPC_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_EAN_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_GTIN_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_SKU_Number",
                            ColumnMetadata.builder(String.class).xpath("fmt:empty()").build()),
                        createColumn("Product_Package_Units",
                            ColumnMetadata.builder(Integer.class).xpath("fmt:integer(packageUnits)").build()),
                        createColumn("Product_Unit_Packet_Picture_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att($ctxt, unitPacketPictureFile/attachmentID)")
                                    .build()))
                    .child(GroupDescriptor.builder("SaleData", PRESENTATION_SHEET)
                            .keys(createColumn("Product_Sales_Volume_Year",
                                ColumnMetadata.builder(Integer.class).xpath("year/value").build()))
                            .columns(
                                createColumn("Product_Mode_Of_Sales",
                                    ColumnMetadata.builder(String.class)
                                            .xpath("fmt:att($ctxt, salesMode/attachmentID)")
                                            .build()),
                                createColumn("Product_Sales_Volume",
                                    ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(salesVolume)").build()))
                            .build())
                    .build(),
            GroupDescriptor.builder("Ingredient", INGREDIENT_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()),
                        createColumn("Ingredient_Name",
                            ColumnMetadata.builder(String.class).xpath("name/value").build()))
                    .columns(
                        createColumn("Confidential",
                            ColumnMetadata.builder(Boolean.class).xpath("name/confidential").build()),
                        createColumn("Ingredient_Identification_Refill_Container_Cartridge",
                            ColumnMetadata.builder(String.class)
                                    .xpath("identificationRefillContainerCartridge/value")
                                    .build()),
                        createColumn("Ingredient_CAS_Exist",
                            ColumnMetadata.builder(String.class).xpath("fmt:bool(casNumberExists)").build()),
                        createColumn("Ingredient_CAS",
                            ColumnMetadata.builder(String.class).xpath("casNumber/value").build()),
                        createColumn("Ingredient_CAS_Additional",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:join(additionalCasNumbers/casNumber/value)")
                                    .build()),
                        createColumn("Ingredient_FEMA_Number",
                            ColumnMetadata.builder(String.class).xpath("femaNumber/value").build()),
                        createColumn("Ingredient_Additive_Number",
                            ColumnMetadata.builder(String.class).xpath("additiveNumber/value").build()),
                        createColumn("Ingredient_FL_Number",
                            ColumnMetadata.builder(String.class).xpath("flNumber/value").build()),
                        createColumn("Ingredient_EC_Number",
                            ColumnMetadata.builder(String.class).xpath("ecNumber/value").build()),
                        createColumn("Ingredient_Function",
                            ColumnMetadata.builder(IngredientFunctionEnum.class)
                                    .xpath("fmt:fromEnum(functions/function/value)")
                                    .build()),
                        createColumn("Ingredient_Function_Other",
                            ColumnMetadata.builder(String.class).xpath("functionOther/value").build()),
                        createColumn("Ingredient_Other_Number",
                            ColumnMetadata.builder(String.class).xpath("otherNumber/value").build()),
                        createColumn("Ingredient_Recipe_Quantity",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(recipeQuantity)").build()),
                        createColumn("Ingredient_Non_Vaporized_Status",
                            ColumnMetadata.builder(ToxicityStatusEnum.class)
                                    .xpath("fmt:fromEnum(toxicityStatus/value)")
                                    .build()),
                        createColumn("Ingredient_REACH_Registration",
                            ColumnMetadata.builder(ReachRegistrationEnum.class)
                                    .xpath("fmt:fromEnum(reachRegistration/value)")
                                    .build()),
                        createColumn("Ingredient_REACH_Registration_Number",
                            ColumnMetadata.builder(String.class).xpath("reachRegistrationNumber/value").build()),
                        createColumn("Ingredient_CLP_Whether_Classification",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(clpWhetherClassification)").build()),
                        createColumn("Ingredient_CLP_Acute_Tox_Oral",
                            ColumnMetadata.builder(String.class).xpath("clpAcuteToxOral/value").build()),
                        createColumn("Ingredient_CLP_Acute_Tox_Dermal",
                            ColumnMetadata.builder(String.class).xpath("clpAcuteToxDermal/value").build()),
                        createColumn("Ingredient_CLP_Acute_Tox_Inhalation",
                            ColumnMetadata.builder(String.class).xpath("clpAcuteToxInhalation/value").build()),
                        createColumn("Ingredient_CLP_Skin_Corrosive/Irritant",
                            ColumnMetadata.builder(String.class).xpath("clpSkinCorrosiveIrritant/value").build()),
                        createColumn("Ingredient_CLP_Eye_Damage/Irritation",
                            ColumnMetadata.builder(String.class).xpath("clpEyeDamageIrritation/value").build()),
                        createColumn("Ingredient_CLP_Respiratory_Sensitisation",
                            ColumnMetadata.builder(String.class).xpath("clpRespiratorySensitisation/value").build()),
                        createColumn("Ingredient_CLP_Skin_Sensitisation",
                            ColumnMetadata.builder(String.class).xpath("clpSkinSensitisation/value").build()),
                        createColumn("Ingredient_CLP_Mutagen/Genotox",
                            ColumnMetadata.builder(String.class).xpath("clpMutagenGenotox/value").build()),
                        createColumn("Ingredient_CLP_Carcinogenity",
                            ColumnMetadata.builder(String.class).xpath("clpCarcinogenicity/value").build()),
                        createColumn("Ingredient_CLP_Reproductive_Tox",
                            ColumnMetadata.builder(String.class).xpath("clpReproductiveTox/value").build()),
                        createColumn("Ingredient_CLP_STOT",
                            ColumnMetadata.builder(String.class).xpath("clpStot/value").build()),
                        createColumn("Ingredient_CLP_STOT_Description",
                            ColumnMetadata.builder(String.class).xpath("clpStotDescription/value").build()),
                        createColumn("Ingredient_CLP_Aspiration_Tox",
                            ColumnMetadata.builder(String.class).xpath("clpAspirationTox/value").build()),
                        createColumn("Ingredient_Tox_Data",
                            ColumnMetadata.builder(ToxicologicalDataAvailableEnum.class)
                                    .xpath("fmt:fromEnum(toxicologicalDetails/toxicologicalDataAvailable/value)")
                                    .build()),
                        createColumn("Ingredient_Tox_Emission",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxEmission)")
                                    .build()),
                        createColumn("Ingredient_Tox_CMR",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxCmr)")
                                    .build()),
                        createColumn("Ingredient_Tox_CardioPulmonary",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxCardioPulmonary)")
                                    .build()),
                        createColumn("Ingredient_Tox_Addictive",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxAddictive)")
                                    .build()),
                        createColumn("Ingredient_Tox_Other",
                            ColumnMetadata.builder(Boolean.class)
                                    .xpath("fmt:bool(toxicologicalDetails/toxOther)")
                                    .build()),
                        createColumn("Ingredient_Tox_Emission_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef( $ctxt, toxicologicalDetails/toxEmissionFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_CMR_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef( $ctxt, toxicologicalDetails/toxCmrFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_CardioPulmonary_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef( $ctxt, toxicologicalDetails/toxCardioPulmonaryFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_Addictive_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef( $ctxt, toxicologicalDetails/toxAddictiveFiles/attachment)")
                                    .build()),
                        createColumn("Ingredient_Tox_Other_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath(
                                        "fmt:joinAttachmentRef( $ctxt, toxicologicalDetails/toxOtherFiles/attachment)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("Emission", EMISSION_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("Emission_Name",
                            ColumnMetadata.builder(EmissionNameEnum.class).xpath("fmt:fromEnum(name/value)").build()),
                        createColumn("Emission_Test_Product_ID",
                            ColumnMetadata.builder(String.class)
                                    .xpath("/additionalProducts/productIdentification[1]/value")
                                    .build()),
                        createColumn("Emission_Product_Combination",
                            ColumnMetadata.builder(String.class).xpath("productCombination/value").build()),
                        createColumn("Emission_Method_Files",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:joinAttachmentRef($ctxt, methodsFile/attachment)")
                                    .build()),
                        createColumn("Emission_Other_Name",
                            ColumnMetadata.builder(String.class).xpath("nameOther/value").build()),
                        createColumn("Emission_CAS",
                            ColumnMetadata.builder(String.class).xpath("casNumber/value").build()),
                        createColumn("Emission_IUPAC",
                            ColumnMetadata.builder(String.class).xpath("iupacName/value").build()),
                        createColumn("Emission_Quantity",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(quantity)").build()),
                        createColumn("Emission_Units",
                            ColumnMetadata.builder(String.class).xpath("unit/value").build()))
                    .build(),
            GroupDescriptor.builder("Design", DESIGN_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(
                        createColumn("E-Cigarette_Description",
                            ColumnMetadata.builder(String.class).xpath("description/value").build()),
                        createColumn("E-Cigarette_Identification_E-Cigarette_Device",
                            ColumnMetadata.builder(String.class).xpath("identificationEcigDevice/value").build()),
                        createColumn("E-Cigarette_Liquid_Volume/Capacity",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(liquidVolumeCapacity)")
                                    .build()),
                        createColumn("E-Cigarette_Nicotine_Concentration",
                            ColumnMetadata.builder(BigDecimal.class)
                                    .xpath("fmt:decimal(nicotineConcentration)")
                                    .build()),
                        createColumn("E-Cigarette_Battery_Type",
                            ColumnMetadata.builder(String.class).xpath("batteryType/value").build()),
                        createColumn("E-Cigarette_Battery_Type_Capacity",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(batteryCapacity)").build()),
                        createColumn("E-Cigarette_Volt/Watt_Adjustable",
                            ColumnMetadata.builder(VoltageWattageAdjustableEnum.class)
                                    .xpath("fmt:fromEnum(voltageWattageAdjustable/value)")
                                    .build()),
                        createColumn("E-Cigarette_Voltage",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(voltage)").build()),
                        createColumn("E-Cigarette_Voltage_Lower_Range",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(voltageLowerRange)").build()),
                        createColumn("E-Cigarette_Voltage_Upper_Range",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(voltageUpperRange)").build()),
                        createColumn("E-Cigarette_Wattage",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(wattage)").build()),
                        createColumn("E-Cigarette_Wattage_Lower_Range",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(wattageLowerRange)").build()),
                        createColumn("E-Cigarette_Wattage_Upper_Range",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(wattageUpperRange)").build()),
                        createColumn("E-Cigarette_Airflow_Adjustable",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(airflowAdjustable)").build()),
                        createColumn("E-Cigarette_Wick_Changeable",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(wickChangeable)").build()),
                        createColumn("E-Cigarette_Microprocessor",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(microprocessor)").build()),
                        createColumn("E-Cigarette_Coil_Composition",
                            ColumnMetadata.builder(String.class).xpath("coilComposition/value").build()),
                        createColumn("E-Cigarette_Coil_Resistance",
                            ColumnMetadata.builder(BigDecimal.class).xpath("fmt:decimal(coilResistance)").build()),
                        createColumn("E-Cigarette_Nicotine_Dose/Uptake_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att( $ctxt, nicotineDoseUptakeFile/attachmentID)")
                                    .build()),
                        createColumn("E-Cigarette _Child_Tamper_Proof",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(childTamperProof)").build()),
                        createColumn("E-Cigarette_Production_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att( $ctxt, productionFile/attachmentID)")
                                    .build()),
                        createColumn("E-Cigarette_Production_Conformity",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(productionConformity)").build()),
                        createColumn("E-Cigarette_Quality_Safety",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(qualitySafety)").build()),
                        createColumn("E-Cigarette_High_Purity",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(highPurity)").build()),
                        createColumn("E-Cigarette_Non_Risk",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(nonRisk)").build()),
                        createColumn("E-Cigarette_Consistent_Dosing",
                            ColumnMetadata.builder(Boolean.class).xpath("fmt:bool(consistentDosing)").build()),
                        createColumn("E-Cigarette_Consistent_Dosing_Methods",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att( $ctxt, consistentDosingMethodsFile/attachmentID)")
                                    .build()),
                        createColumn("E-Cigarette_Opening/Refill_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att( $ctxt, openingRefillFile/attachmentID)")
                                    .build()),
                        createColumn("E-Cigarette_Leaflet_File",
                            ColumnMetadata.builder(String.class)
                                    .xpath("fmt:att( $ctxt, leafletFile/attachmentID)")
                                    .build()))
                    .build(),
            GroupDescriptor.builder("AdditionnalInformation", ADDITIONNAL_SHEET)
                    .keys(
                        createColumn("Submitter_ID",
                            ColumnMetadata.builder(String.class).xpath("$submitterId").build()),
                        createColumn("Internal_Product_Number",
                            ColumnMetadata.builder(String.class).xpath("$internalProductNumber").build()))
                    .columns(createColumn("STATUS", ColumnMetadata.builder(String.class).xpath("$status").build()),
                        createColumn("PIR_STATUS", ColumnMetadata.builder(String.class).xpath("$pirStatus").build()),
                        createColumn("CREATED_BY", ColumnMetadata.builder(String.class).xpath("$createdBy").build()),
                        createColumn("CREATED_DATE",
                            ColumnMetadata.builder(String.class).xpath("$createdDate").build()),
                        createColumn("LAST_MODIFIED_BY",
                            ColumnMetadata.builder(String.class).xpath("$lastModifiedBy").build()),
                        createColumn("LAST_MODIFIED_DATE",
                            ColumnMetadata.builder(String.class).xpath("$lastModifiedDate").build()))
                    .build(),
            GroupDescriptor.builder("ExportSummarize", SUMMARIZE_SHEET)
                    .columns(createColumn("NUMBER_OF_PRODUCT", ColumnMetadata.builder(Integer.class).build()),
                        createColumn("USED_FILTER", ColumnMetadata.builder(String.class).build()))
                    .build()));
    }
}
