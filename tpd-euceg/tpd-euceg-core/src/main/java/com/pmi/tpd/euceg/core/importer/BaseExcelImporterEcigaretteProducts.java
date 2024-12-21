package com.pmi.tpd.euceg.core.importer;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AttachmentRef;
import org.eu.ceg.CasNumber;
import org.eu.ceg.Design;
import org.eu.ceg.EcigAnnualSalesData;
import org.eu.ceg.EcigEmission;
import org.eu.ceg.EcigEmission.AdditionalProducts;
import org.eu.ceg.EcigIngredient;
import org.eu.ceg.EcigPresentation;
import org.eu.ceg.EcigPresentation.AnnualSalesDataList;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProduct.Emissions;
import org.eu.ceg.EcigProduct.Ingredients;
import org.eu.ceg.EcigProduct.Presentations;
import org.eu.ceg.EcigProduct.StudySummaryFiles;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.Emission.MethodsFile;
import org.eu.ceg.EmissionNameEnum;
import org.eu.ceg.Ingredient.AdditionalCasNumbers;
import org.eu.ceg.Ingredient.Functions;
import org.eu.ceg.IngredientFunction;
import org.eu.ceg.IngredientFunctionEnum;
import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.PresentationNumberType;
import org.eu.ceg.PresentationNumberTypeEnum;
import org.eu.ceg.Product.MarketResearchFiles;
import org.eu.ceg.Product.OtherProducts;
import org.eu.ceg.Product.SameCompositionProducts;
import org.eu.ceg.ProductIdentification;
import org.eu.ceg.ProductIdentificationType;
import org.eu.ceg.ReachRegistrationEnum;
import org.eu.ceg.ToxicityStatusEnum;
import org.eu.ceg.VoltageWattageAdjustableEnum;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;

/**
 * The Base class to import e-cigarette product.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public abstract class BaseExcelImporterEcigaretteProducts extends AbstractProductImporter<EcigProduct> {

    /**
     * @param i18nService
     *                    a localisation service.
     */
    public BaseExcelImporterEcigaretteProducts(@Nonnull final I18nService i18nService) {
        super(i18nService);
    }

    @Override
    @Nonnull
    protected ListDescriptor getListDescriptor() {
        return EucegExcelSchema.EcigProduct.DESCRIPTORS;
    }

    /**
     * @param productNumber
     *                      the product to get.
     * @return Returns the current product to update.
     */
    @Override
    @Nullable
    public abstract EcigProduct getCurrentProduct(@Nonnull String productNumber);

    /**
     * Create a tobacco product instance.
     *
     * @param product
     *                     a product to override.
     * @param objectMapper
     *                     the root object mapper.
     * @return Returns new instance of {@link EcigProduct}.
     * @throws EucegImportException
     *                              if the import has failed.
     */
    @Override
    protected @Nonnull EcigProduct createProduct(@Nullable EcigProduct product,
        final @Nonnull ObjectMapper objectMapper) throws EucegImportException {
        final OtherProducts otherProducts = createOtherProducts(objectMapper);
        final SameCompositionProducts sameCompositionOthers = createSameCompositionProducts(objectMapper);
        if (product == null) {
            product = new EcigProduct().withProductID(Eucegs.productNumber(Eucegs.UNDEFINED_PRODUCT_ID));
        } else {
            product = (EcigProduct) product.clone();
        }

        product.withProductType(
            Eucegs.ecigProductType(objectMapper.getValue("Product Type", EcigProductTypeEnum.class)))
                .withVolume(Eucegs.decimal(objectMapper.getValue("Product_Volume_E-Liquid", BigDecimal.class)))
                .withWeight(Eucegs.decimal(objectMapper.getValue("Product_Weight_E-Liquid", BigDecimal.class)))
                .withClpClassification(
                    Eucegs.string300(objectMapper.getValue("Product_CLP_Classification", String.class)))
                .withSameCompositionProductsExist(Eucegs.toBooleanNullable(sameCompositionOthers != null, true))
                .withSameCompositionProducts(sameCompositionOthers)
                .withManufacturers(createManufacturers(objectMapper))

                .withOtherProductsExist(Eucegs.toBooleanNullable(otherProducts != null, true))
                .withOtherProducts(otherProducts);

        createDesign(product, objectMapper);
        createEmissions(product, objectMapper);
        createIngredients(product, objectMapper);
        createPresentations(product, objectMapper);
        createMarketResearchFiles(product, objectMapper);
        createStudySummaryFiles(product, objectMapper);

        return product;
    }

    private OtherProducts createOtherProducts(final ObjectMapper mapper) {
        final String productIdOther = mapper.getValue("Product_ID_Other", String.class);
        if (Strings.isNullOrEmpty(productIdOther)) {
            return null;
        }
        return new OtherProducts().withProductIdentification(
            Eucegs.productIdentification(productIdOther, ProductIdentificationType.PRODUCT_ID).withConfidential(true));
    }

    private SameCompositionProducts createSameCompositionProducts(final ObjectMapper mapper) {
        final String productId = mapper.getValue("Product_Same_Composition_Other", String.class);
        if (Strings.isNullOrEmpty(productId)) {
            return null;
        }
        return new SameCompositionProducts().withProductIdentification(
            Eucegs.productIdentification(productId, ProductIdentificationType.TEXT).withConfidential(true));
    }

    private void createStudySummaryFiles(final EcigProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mapperAttachments = objectMapper.getObjectMappers("Attachment");
        if (!objectMapper.isSelected("Attachment")) {
            return;
        }
        if (mapperAttachments.isEmpty()) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mapperAttachments, null);
        product.withStudySummaryFiles(null);
        if (mapper != null) {
            final List<AttachmentRef> list = appendAttachments(mapper, "Product_Studies_Summaries_File");
            if (!list.isEmpty()) {
                product.withStudySummaryFiles(new StudySummaryFiles().withAttachment(list));
            }
        }
    }

    private void createMarketResearchFiles(final EcigProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mapperMarkets = objectMapper.getObjectMappers("Attachment");
        if (!objectMapper.isSelected("Attachment")) {
            return;
        }
        if (mapperMarkets.isEmpty()) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mapperMarkets, null);
        product.withMarketResearchFiles(null);
        if (mapper != null) {
            final List<AttachmentRef> list = appendAttachments(mapper, "Product_Market_Research_File");
            if (!list.isEmpty()) {
                product.withMarketResearchFiles(new MarketResearchFiles().withAttachment(list));
            }
        }
    }

    private void createPresentations(final EcigProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Presentation");
        if (!objectMapper.isSelected("Presentation")) {
            return;
        }

        product.withPresentations(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<EcigPresentation> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final String subtypeName = mapper.getValue("Product_Brand_Subtype_Name", String.class);
            final String launchDate = mapper.getValue("Product_Launch_Date", String.class);
            final String withdrawalDate = mapper.getValue("Product_Withdrawal_Date", String.class);
            final AttachmentRef packetPictureFile = appendAttachment(mapper, "Product_Unit_Packet_Picture_File");

            list.add(new EcigPresentation().withAnnualSalesDataList(createAnnualSalesDataList(mapper))
                    .withBrandName(Eucegs.string100(mapper.getValue("Product_Brand_Name", String.class)))
                    .withBrandSubtypeName(Eucegs.string100(subtypeName))
                    .withBrandSubtypeNameExists(Eucegs.toBoolean(!Strings.isNullOrEmpty(subtypeName)))

                    .withLaunchDate(Eucegs.toDate(launchDate))
                    .withNationalComment(Eucegs.string1000(mapper.getValue("Product_National_Comment", String.class)))
                    .withNationalMarket(
                        Eucegs.nationalMarket(mapper.getValue("Product_National_Market", NationalMarketValue.class)))
                    .withPackageUnits(Eucegs.toInteger(mapper.getValue("Product_Package_Units", Integer.class)))
                    .withProductNumber(Eucegs.string40(mapper.getValue("Product_Submitter_Number", String.class)))
                    .withProductNumberType(new PresentationNumberType().withValue(PresentationNumberTypeEnum.SUBMITTER)
                            .withConfidential(false))
                    .withWithdrawalDate(Eucegs.toDate(withdrawalDate))
                    .withUnitPacketPictureFile(packetPictureFile)
                    .withWithdrawalIndication(Eucegs.toBoolean(!Strings.isNullOrEmpty(withdrawalDate))));
        }
        if (!list.isEmpty()) {
            product.withPresentations(new Presentations().withPresentation(list));
        }
    }

    private AnnualSalesDataList createAnnualSalesDataList(final ObjectMapper objectMapper) throws EucegImportException {

        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("SaleData");
        if (mappers.isEmpty()) {
            return null;
        }
        final List<EcigAnnualSalesData> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final AttachmentRef saleMode = appendAttachment(mapper, "Product_Mode_Of_Sales");
            list.add(new EcigAnnualSalesData().withSalesMode(saleMode)
                    .withSalesVolume(Eucegs.decimal(mapper.getValue("Product_Sales_Volume", BigDecimal.class), true))
                    .withYear(Eucegs.year(mapper.getValue("Product_Sales_Volume_Year", Integer.class), true)));
        }

        return new AnnualSalesDataList().withAnnualSalesData(list);
    }

    private void createIngredients(final EcigProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Ingredient", true);
        if (!objectMapper.isSelected("Ingredient")) {
            return;
        }

        product.withIngredients(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<EcigIngredient> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final boolean confidential = mapper.getValue("Confidential", Boolean.class);

            final String casNumber = mapper.getValue("Ingredient_CAS", String.class);

            final IngredientFunctionEnum function = mapper.getValue("Ingredient_Function",
                IngredientFunctionEnum.class);
            list.add(new EcigIngredient().withAdditionalCasNumbers(createAdditionalCasNumbers(mapper, confidential))
                    .withAdditiveNumber(Eucegs
                            .additiveNumber(mapper.getValue("Ingredient_Additive_Number", String.class), confidential))
                    .withCasNumber(Eucegs.casNumber(casNumber, confidential))
                    .withCasNumberExists(Eucegs.toBooleanNullable(casNumber != null, confidential))
                    .withClpAcuteToxDermal(
                        Eucegs.acuteToxDermalCode(mapper.getValue("Ingredient_CLP_Acute_Tox_Dermal", String.class),
                            confidential))
                    .withClpAcuteToxInhalation(
                        Eucegs.acuteToxInhalationCode(mapper.getValue("Ingredient_CLP_Acute_Tox_Oral", String.class),
                            confidential))
                    .withClpAcuteToxOral(
                        Eucegs.acuteToxOralCode(mapper.getValue("Ingredient_CLP_Acute_Tox_Oral", String.class),
                            confidential))
                    .withClpAspirationTox(
                        Eucegs.aspirationToxCode(mapper.getValue("Ingredient_CLP_Aspiration_Tox", String.class),
                            confidential))
                    .withClpCarcinogenicity(
                        Eucegs.carcinogenicityCode(mapper.getValue("Ingredient_CLP_Carcinogenity", String.class),
                            confidential))
                    .withClpEyeDamageIrritation(Eucegs.eyeDamageIrritationCode(
                        mapper.getValue("Ingredient_CLP_Eye_Damage/Irritation", String.class),
                        confidential))
                    .withClpMutagenGenotox(
                        Eucegs.mutagenGenotoxCode(mapper.getValue("Ingredient_CLP_Mutagen/Genotox", String.class),
                            confidential))
                    .withClpReproductiveTox(
                        Eucegs.reproductiveToxCode(mapper.getValue("Ingredient_CLP_Reproductive_Tox", String.class),
                            confidential))
                    .withClpRespiratorySensitisation(Eucegs.respiratorySensitisationCode(
                        mapper.getValue("Ingredient_CLP_Respiratory_Sensitisation", String.class),
                        confidential))
                    .withClpSkinCorrosiveIrritant(Eucegs.skinCorrosiveIrritantCode(
                        mapper.getValue("Ingredient_CLP_Skin_Corrosive/Irritant", String.class),
                        confidential))
                    .withClpSkinSensitisation(
                        Eucegs.skinSensitisationCode(mapper.getValue("Ingredient_CLP_Skin_Sensitisation", String.class),
                            confidential))
                    .withClpStot(Eucegs.stotCode(mapper.getValue("Ingredient_CLP_STOT", String.class), confidential))
                    .withClpStotDescription(Eucegs
                            .string500(mapper.getValue("Ingredient_CLP_STOT_Description", String.class), confidential))
                    .withClpWhetherClassification(Eucegs.toBooleanNullable(
                        mapper.getValue("Ingredient_CLP_Whether_Classification", Boolean.class),
                        confidential))
                    .withEcNumber(Eucegs.ecNumber(mapper.getValue("Ingredient_EC_Number", String.class), confidential))
                    .withFemaNumber(
                        Eucegs.femaNumber(mapper.getValue("Ingredient_FEMA_Number", String.class), confidential))
                    .withFlNumber(Eucegs.flNumber(mapper.getValue("Ingredient_FL_Number", String.class), confidential))
                    .withFunctionOther(
                        Eucegs.string100(mapper.getValue("Ingredient_Function_Other", String.class), confidential))
                    .withFunctions(function == null ? null
                            : new Functions().withFunction(
                                new IngredientFunction().withValue(function).withConfidential(confidential)))
                    .withName(Eucegs.string300(mapper.getValue("Ingredient_Name", String.class), confidential))
                    .withIdentificationRefillContainerCartridge(Eucegs.string300(
                        mapper.getValue("Ingredient_Identification_Refill_Container_Cartridge", String.class),
                        confidential))
                    .withOtherNumber(
                        Eucegs.string40(mapper.getValue("Ingredient_Other_Number", String.class), confidential))
                    .withReachRegistration(Eucegs.reachRegistration(
                        mapper.getValue("Ingredient_REACH_Registration", ReachRegistrationEnum.class),
                        confidential))
                    .withReachRegistrationNumber(
                        Eucegs.string40(mapper.getValue("Ingredient_REACH_Registration_Number", String.class),
                            confidential))
                    .withRecipeQuantity(
                        Eucegs.decimal(mapper.getValue("Ingredient_Recipe_Quantity", BigDecimal.class), confidential))
                    .withToxicityStatus(Eucegs.toxicityStatus(
                        mapper.getValue("Ingredient_Non_Vaporized_Status", ToxicityStatusEnum.class),
                        confidential))
                    .withToxicologicalDetails(createToxicologicalDetails(mapper)));
        }
        if (!list.isEmpty()) {
            product.withIngredients(new Ingredients().withIngredient(list));
        }
    }

    private AdditionalCasNumbers createAdditionalCasNumbers(final ObjectMapper mapper, final boolean confidential) {
        final String casAdditional = mapper.getValue("Ingredient_CAS_Additional", String.class);
        if (Strings.isNullOrEmpty(casAdditional)) {
            return null;
        }
        final String[] casNumbers = casAdditional.split(";");
        final List<CasNumber> list = Lists.newArrayList();
        for (final String casNumber : casNumbers) {
            list.add(Eucegs.casNumber(casNumber.trim()).withConfidential(confidential));
        }
        return new AdditionalCasNumbers().withCasNumber(list);
    }

    private void createEmissions(final EcigProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Emission");
        if (!objectMapper.isSelected("Emission")) {
            return;
        }

        product.withEmissions(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<EcigEmission> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final List<AttachmentRef> methodsFiles = appendAttachments(mapper, "Emission_Method_Files");
            final String productId = mapper.getValue("Emission_Test_Product_ID", String.class);
            list.add(
                new EcigEmission()
                        .withAdditionalProducts(productId != null
                                ? new AdditionalProducts()
                                        .withProductIdentification(new ProductIdentification().withConfidential(true)
                                                .withType(ProductIdentificationType.TEXT)
                                                .withValue(mapper.getValue("Emission_Test_Product_ID", String.class)))
                                : null)
                        .withCasNumber(Eucegs.casNumber(mapper.getValue("Emission_CAS", String.class), true))
                        .withIupacName(Eucegs.string100(mapper.getValue("Emission_IUPAC", String.class), true))
                        .withMethodsFile(
                            !methodsFiles.isEmpty() ? new MethodsFile().withAttachment(methodsFiles) : null)
                        .withName(Eucegs.emissionName(mapper.getValue("Emission_Name", EmissionNameEnum.class), true))
                        .withNameOther(Eucegs.string100(mapper.getValue("Emission_Other_Name", String.class), true))
                        .withProductCombination(
                            Eucegs.string500(mapper.getValue("Emission_Product_Combination", String.class), true))
                        .withQuantity(Eucegs.decimal(mapper.getValue("Emission_Quantity", BigDecimal.class), true))
                        .withUnit(Eucegs.string40(mapper.getValue("Emission_Units", String.class), true)));
        }
        if (!list.isEmpty()) {
            product.withEmissions(new Emissions().withEmission(list));
        }
    }

    private void createDesign(final EcigProduct product, final ObjectMapper objectMapper) throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Design");
        if (!objectMapper.isSelected("Design")) {
            return;
        }
        if (mappers.isEmpty()) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mappers, null);
        product.withDesign(null);
        if (mapper != null) {
            product.withDesign(new Design()
                    .withAirflowAdjustable(
                        Eucegs.toBoolean(mapper.getValue("E-Cigarette_Airflow_Adjustable", Boolean.class)))
                    .withBatteryCapacity(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Battery_Type_Capacity", BigDecimal.class)))
                    .withBatteryType(Eucegs.string300(mapper.getValue("E-Cigarette_Battery_Type", String.class)))
                    .withChildTamperProof(
                        Eucegs.toBooleanNullable(mapper.getValue("E-Cigarette _Child_Tamper_Proof", Boolean.class)))
                    .withCoilComposition(
                        Eucegs.string300(mapper.getValue("E-Cigarette_Coil_Composition", String.class)))
                    .withCoilResistance(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Coil_Resistance", BigDecimal.class)))
                    .withConsistentDosing(
                        Eucegs.toBooleanNullable(mapper.getValue("E-Cigarette_Consistent_Dosing", Boolean.class)))
                    .withConsistentDosingMethodsFile(appendAttachment(mapper, "E-Cigarette_Consistent_Dosing_Methods"))
                    .withDescription(Eucegs.string1000(mapper.getValue("E-Cigarette_Description", String.class)))
                    .withHighPurity(Eucegs.toBooleanNullable(mapper.getValue("E-Cigarette_High_Purity", Boolean.class)))
                    .withIdentificationEcigDevice(Eucegs
                            .string300(mapper.getValue("E-Cigarette_Identification_E-Cigarette_Device", String.class)))
                    .withLeafletFile(appendAttachment(mapper, "E-Cigarette_Leaflet_File"))
                    .withLiquidVolumeCapacity(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Liquid_Volume/Capacity", BigDecimal.class)))
                    .withMicroprocessor(Eucegs.toBoolean(mapper.getValue("E-Cigarette_Microprocessor", Boolean.class)))
                    .withNicotineConcentration(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Nicotine_Concentration", BigDecimal.class)))
                    .withNicotineDoseUptakeFile(appendAttachment(mapper, "E-Cigarette_Nicotine_Dose/Uptake_File"))
                    .withNonRisk(Eucegs.toBooleanNullable(mapper.getValue("E-Cigarette_Non_Risk", Boolean.class)))
                    .withOpeningRefillFile(appendAttachment(mapper, "E-Cigarette_Opening/Refill_File"))
                    .withProductionConformity(
                        Eucegs.toBooleanNullable(mapper.getValue("E-Cigarette_Production_Conformity", Boolean.class)))
                    .withProductionFile(appendAttachment(mapper, "E-Cigarette_Production_File"))
                    .withQualitySafety(
                        Eucegs.toBooleanNullable(mapper.getValue("E-Cigarette_Quality_Safety", Boolean.class)))
                    .withVoltage(Eucegs.decimal(mapper.getValue("E-Cigarette_Voltage", BigDecimal.class)))
                    .withVoltageLowerRange(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Voltage_Lower_Range", BigDecimal.class)))
                    .withVoltageUpperRange(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Voltage_Upper_Range", BigDecimal.class)))
                    .withVoltageWattageAdjustable(Eucegs.voltageWattageAdjustable(
                        mapper.getValue("E-Cigarette_Volt/Watt_Adjustable", VoltageWattageAdjustableEnum.class)))
                    .withWattage(Eucegs.decimal(mapper.getValue("E-Cigarette_Wattage", BigDecimal.class)))
                    .withWattageLowerRange(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Wattage_Lower_Range", BigDecimal.class)))
                    .withWattageUpperRange(
                        Eucegs.decimal(mapper.getValue("E-Cigarette_Wattage_Upper_Range", BigDecimal.class)))
                    .withWickChangeable(
                        Eucegs.toBoolean(mapper.getValue("E-Cigarette_Wick_Changeable", Boolean.class))));
        }
    }

}
