package com.pmi.tpd.euceg.core.importer;

import static com.pmi.tpd.euceg.api.Eucegs.casNumber;
import static com.pmi.tpd.euceg.api.Eucegs.decimal;
import static com.pmi.tpd.euceg.api.Eucegs.toBoolean;
import static com.pmi.tpd.euceg.api.Eucegs.toBooleanNullable;
import static com.pmi.tpd.euceg.api.Eucegs.toInteger;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AttachmentRef;
import org.eu.ceg.CasNumber;
import org.eu.ceg.CigaretteSpecific;
import org.eu.ceg.Company;
import org.eu.ceg.Emission.MethodsFile;
import org.eu.ceg.Ingredient.AdditionalCasNumbers;
import org.eu.ceg.Ingredient.Functions;
import org.eu.ceg.IngredientCategoryEnum;
import org.eu.ceg.IngredientFunction;
import org.eu.ceg.IngredientFunctionEnum;
import org.eu.ceg.LeafCureMethodEnum;
import org.eu.ceg.LeafTypeEnum;
import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.NovelSpecific;
import org.eu.ceg.NovelSpecific.StudyFiles;
import org.eu.ceg.PackageTypeEnum;
import org.eu.ceg.PartTypeEnum;
import org.eu.ceg.PresentationNumberType;
import org.eu.ceg.PresentationNumberTypeEnum;
import org.eu.ceg.Product.MarketResearchFiles;
import org.eu.ceg.Product.OtherProducts;
import org.eu.ceg.Product.SameCompositionProducts;
import org.eu.ceg.ProductIdentificationType;
import org.eu.ceg.ReachRegistrationEnum;
import org.eu.ceg.RyoPipeSpecific;
import org.eu.ceg.SmokelessSpecific;
import org.eu.ceg.TncoEmission;
import org.eu.ceg.TncoEmission.Laboratories;
import org.eu.ceg.TobaccoAnnualSalesData;
import org.eu.ceg.TobaccoEmission;
import org.eu.ceg.TobaccoIngredient;
import org.eu.ceg.TobaccoIngredient.PartDescriptionFiles;
import org.eu.ceg.TobaccoIngredient.Suppliers;
import org.eu.ceg.TobaccoOtherIngredient;
import org.eu.ceg.TobaccoOtherIngredient.PriorityAdditiveFiles;
import org.eu.ceg.TobaccoPresentation;
import org.eu.ceg.TobaccoPresentation.AnnualSalesDataList;
import org.eu.ceg.TobaccoPresentation.OtherMarketData;
import org.eu.ceg.TobaccoPresentation.UnitPacketPictureFiles;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProduct.OtherEmissions;
import org.eu.ceg.TobaccoProduct.OtherIngredients;
import org.eu.ceg.TobaccoProduct.Presentations;
import org.eu.ceg.TobaccoProduct.TechnicalFiles;
import org.eu.ceg.TobaccoProduct.TobaccoIngredients;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.eu.ceg.ToxicityStatusEnum;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.core.excel.ExcelMapper.ObjectMapper;
import com.pmi.tpd.euceg.core.excel.ListDescriptor;
import com.pmi.tpd.euceg.core.internal.EucegExcelSchema;

/**
 * The Base class to import tobacco product.
 *
 * @author @author Christophe Friederich
 * @since 1.0
 */
public abstract class BaseExcelmporterTobaccoProducts extends AbstractProductImporter<TobaccoProduct> {

    /**
     * @param i18nService
     *                    a localisation service.
     */
    public BaseExcelmporterTobaccoProducts(final I18nService i18nService) {
        super(i18nService);
    }

    @Override
    protected @Nonnull ListDescriptor getListDescriptor() {
        return EucegExcelSchema.TobaccoProduct.DESCRIPTORS;
    }

    /**
     * Create a tobacco product instance.
     *
     * @param product
     *                     a product to override.
     * @param objectMapper
     *                     the root object mapper.
     * @return Returns new instance of {@link TobaccoProduct}.
     * @throws EucegImportException
     *                              if the import has failed.
     */
    @Override
    protected @Nonnull TobaccoProduct createProduct(@Nullable TobaccoProduct product,
        final @Nonnull ObjectMapper objectMapper) throws EucegImportException {
        final Integer filterLength = objectMapper.getValue("Product_Filter_Length", Integer.class);
        final OtherProducts otherProducts = createOtherProducts(objectMapper);
        final SameCompositionProducts sameCompositionOthers = createSameCompositionProducts(objectMapper);

        if (product == null) {
            product = new TobaccoProduct().withProductID(Eucegs.productNumber(Eucegs.UNDEFINED_PRODUCT_ID));
        } else {
            // clone the product
            product = (TobaccoProduct) product.clone();
        }
        product.withDiameter(decimal(objectMapper.getValue("Product_Diameter", BigDecimal.class), true))
                .withProductType(
                    Eucegs.tobaccoProductType(objectMapper.getValue("Product Type", TobaccoProductTypeEnum.class)))
                .withFilter(toBooleanNullable(filterLength != null))
                .withFilterLength(toInteger(filterLength, true))
                .withLength(decimal(objectMapper.getValue("Product_Length", BigDecimal.class), true))
                .withTobaccoWeight(decimal(objectMapper.getValue("Product_Tobacco Weight", BigDecimal.class)))
                .withWeight(decimal(objectMapper.getValue("Product_Weight", BigDecimal.class)))
                .withOtherProductsExist(toBooleanNullable(otherProducts != null, true))
                .withOtherProducts(otherProducts)
                .withSameCompositionProductsExist(toBooleanNullable(sameCompositionOthers != null, true))
                .withSameCompositionProducts(sameCompositionOthers)
                .withManufacturers(createManufacturers(objectMapper))
                .withTechnicalFiles(createTechnicalFiles(objectMapper));
        createMarketResearchFiles(product, objectMapper);

        createCigaretteSpecific(product, objectMapper);
        createNovelSpecific(product, objectMapper);
        createRyoPipeSpecific(product, objectMapper);
        createSmokelessSpecific(product, objectMapper);
        createOtherEmissions(product, objectMapper);
        createOtherIngredients(product, objectMapper);
        createPresentations(product, objectMapper);
        createTncoEmission(product, objectMapper);
        createTobaccoIngredients(product, objectMapper);

        return product;
    }

    private void createSmokelessSpecific(final TobaccoProduct product, final ObjectMapper objectMapper) {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Smokeless");
        if (!objectMapper.isSelected("Smokeless")) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mappers, null);
        product.withSmokelessSpecific(null);
        if (mapper != null) {
            product.withSmokelessSpecific(new SmokelessSpecific()
                    .withAnalysisMethods(Eucegs.string300(mapper.getValue("Smokeless_Analysis_Methods", String.class)))
                    .withPh(decimal(mapper.getValue("Smokeless_pH", BigDecimal.class)))
                    .withTotalMoisture(decimal(mapper.getValue("Smokeless_Total_Moisture", BigDecimal.class)))
                    .withTotalNicotineContent(decimal(mapper.getValue("Smokeless_Nicotine_Content", BigDecimal.class)))
                    .withUnionisedNicotineContent(
                        decimal(mapper.getValue("Smokeless_Unionised_Nicotine_Content", BigDecimal.class))));
        }
    }

    private void createCigaretteSpecific(final TobaccoProduct product, final ObjectMapper objectMapper) {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Cigarette");
        if (!objectMapper.isSelected("Cigarette")) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mappers, null);
        product.withCigaretteSpecific(null);
        if (mapper != null) {
            product.withCigaretteSpecific(new CigaretteSpecific()
                    .withCharacterisingFlavour(
                        toBoolean(mapper.getValue("Cigarette_Charecterising_Flavour", Boolean.class)))
                    .withFilterDropPressureClosed(
                        decimal(mapper.getValue("Cigarette_Filter_Drop_Pressure_Closed", BigDecimal.class), true))
                    .withFilterDropPressureOpen(
                        decimal(mapper.getValue("Cigarette_Filter_Drop_Pressure_Open", BigDecimal.class), true))
                    .withFilterVentilation(
                        Eucegs.percentage(mapper.getValue("Cigarette_Filter_Ventilation", Integer.class), true)));
        }
    }

    private void createNovelSpecific(final TobaccoProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Novel");
        if (!objectMapper.isSelected("Novel")) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mappers, null);
        product.withNovelSpecific(null);
        if (mapper != null) {
            final List<AttachmentRef> studyFiles = appendAttachments(mapper, "Novel_Study");
            product.withNovelSpecific(new NovelSpecific()
                    .withDetailsDescriptionFile(appendAttachment(mapper, "Novel_Details_Description_File"))
                    .withRiskBenefitFile(appendAttachment(mapper, "Novel_Risk/Benefit_File"))
                    .withStudyFiles(!studyFiles.isEmpty() ? new StudyFiles().withAttachment(studyFiles) : null)
                    .withUseInstructionsFile(appendAttachment(mapper, "Novel_Use_Instructions_File")));
        }
    }

    private void createRyoPipeSpecific(final TobaccoProduct product, final ObjectMapper objectMapper) {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("RollOwn");
        if (!objectMapper.isSelected("RollOwn")) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mappers, null);
        product.withRyoPipeSpecific(null);
        if (mapper != null) {
            product.withRyoPipeSpecific(new RyoPipeSpecific()
                    .withTotalNicotineContent(
                        decimal(mapper.getValue("Roll-your-own/pipe_Total_ Nicotine_Content", BigDecimal.class), true))
                    .withUnionisedNicotineContent(
                        decimal(mapper.getValue("Roll-your-own/pipe_Unionised_Nicotine_Content", BigDecimal.class))));
        }
    }

    private void createMarketResearchFiles(final TobaccoProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("MarketResearchFile");
        if (!objectMapper.isSelected("MarketResearchFile")) {
            return;
        }
        product.withMarketResearchFiles(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<AttachmentRef> list = appendAttachments(mappers, "Product_Market_Research_File");
        if (!list.isEmpty()) {
            product.withMarketResearchFiles(new MarketResearchFiles().withAttachment(list));
        }
    }

    private void createOtherEmissions(final TobaccoProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Emission");
        if (!objectMapper.isSelected("Emission")) {
            return;
        }
        product.withOtherEmissions(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<TobaccoEmission> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final List<AttachmentRef> otherMethodFiles = appendAttachments(mapper, "Emission_Other_Method_Files");
            final boolean emissionAvailable = mapper.getValue("Emission_Other_Available", Boolean.class, false);
            if (emissionAvailable) {
                list.add(new TobaccoEmission()
                        .withCasNumber(Eucegs.casNumber(mapper.getValue("Emission_Other_CAS", String.class)))
                        .withIupacName(Eucegs.string100(mapper.getValue("Emission_Other_IUPAC", String.class)))
                        .withName(Eucegs.string100(mapper.getValue("Emission_Other_Name", String.class)))
                        .withQuantity(Eucegs.decimal(mapper.getValue("Emission_Other_Quantity", BigDecimal.class)))
                        .withUnit(Eucegs.string40(mapper.getValue("Emission_Other_Units", String.class)))
                        .withMethodsFile(
                            !otherMethodFiles.isEmpty() ? new MethodsFile().withAttachment(otherMethodFiles) : null));
            }
        }
        product.withOtherEmissionsAvailable(toBoolean(!list.isEmpty()));
        if (!list.isEmpty()) {
            product.withOtherEmissions(new OtherEmissions().withEmission(list));
        }
    }

    private void createOtherIngredients(final TobaccoProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("OtherIngredient", true);
        if (!objectMapper.isSelected("OtherIngredient")) {
            return;
        }

        product.withOtherIngredients(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<TobaccoOtherIngredient> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final boolean confidential = mapper.getValue("Confidential", Boolean.class);

            final String casNumber = mapper.getValue("Ingredient_CAS", String.class);
            final IngredientFunctionEnum function = mapper.getValue("Ingredient_Function",
                IngredientFunctionEnum.class);

            list.add(new TobaccoOtherIngredient()
                    .withAdditionalCasNumbers(createAdditionalCasNumbers(mapper, confidential))
                    .withAdditiveNumber(Eucegs
                            .additiveNumber(mapper.getValue("Ingredient_Additive_Number", String.class), confidential))
                    .withCasNumber(Eucegs.casNumber(casNumber, confidential))
                    .withCasNumberExists(toBooleanNullable(casNumber != null, confidential))
                    .withCategory(
                        Eucegs.ingredientCategorie(mapper.getValue("Ingredient_Category", IngredientCategoryEnum.class),
                            confidential))
                    .withCategoryOther(
                        Eucegs.string100(mapper.getValue("Ingredient_Category_Other", String.class), confidential))
                    .withClpAcuteToxDermal(
                        Eucegs.acuteToxDermalCode(mapper.getValue("Ingredient_CLP_Acute_Tox_Dermal", String.class),
                            confidential))
                    .withClpAcuteToxInhalation(Eucegs.acuteToxInhalationCode(
                        mapper.getValue("Ingredient_CLP_Acute_Tox_Inhalation", String.class),
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
                    .withClpWhetherClassification(
                        toBooleanNullable(mapper.getValue("Ingredient_CLP_Whether_Classification", Boolean.class),
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
                    .withMeasuredMaxLevel(Eucegs
                            .decimal(mapper.getValue("Ingredient_Measured_Max_Level", BigDecimal.class), confidential))
                    .withMeasuredMeanQuantity(
                        Eucegs.decimal(mapper.getValue("Ingredient_Measured_Mean_Quantity", BigDecimal.class),
                            confidential))
                    .withMeasuredMinLevel(Eucegs
                            .decimal(mapper.getValue("Ingredient_Measured_Min_Level", BigDecimal.class), confidential))
                    .withMeasuredNumber(
                        Eucegs.decimal(mapper.getValue("Ingredient_Measured_Number", BigDecimal.class), confidential))
                    .withMeasuredSd(
                        Eucegs.decimal(mapper.getValue("Ingredient_Measured_SD", BigDecimal.class), confidential))
                    .withName(Eucegs.string300(mapper.getValue("Ingredient_Name", String.class), confidential))
                    .withOtherNumber(
                        Eucegs.string40(mapper.getValue("Ingredient_Other_Number", String.class), confidential))
                    .withPriorityAdditive(
                        toBooleanNullable(mapper.getValue("Ingredient_Priority_Additive", Boolean.class), confidential))
                    .withPriorityAdditiveFiles(createPriorityAdditiveFiles(mapper))
                    .withQuantityFluctuate(
                        toBooleanNullable(mapper.getValue("Ingredient_Quantity_Fluctuate", Boolean.class),
                            confidential))
                    .withReachRegistration(Eucegs.reachRegistration(
                        mapper.getValue("Ingredient_REACH_Registration", ReachRegistrationEnum.class),
                        confidential))
                    .withReachRegistrationNumber(
                        Eucegs.string40(mapper.getValue("Ingredient_REACH_Registration_Number", String.class),
                            confidential))
                    .withRecipeQuantity(
                        Eucegs.decimal(mapper.getValue("Ingredient_Recipe_Quantity", BigDecimal.class), confidential))
                    .withRecipeRangeMaxLevel(
                        Eucegs.decimal(mapper.getValue("Ingredient_Recipe_Range_Max_Level", BigDecimal.class),
                            confidential))
                    .withRecipeRangeMinLevel(
                        Eucegs.decimal(mapper.getValue("Ingredient_Recipe_Range_Min_Level", BigDecimal.class),
                            confidential))
                    .withToxicityStatus(
                        Eucegs.toxicityStatus(mapper.getValue("Ingredient_Unburnt_Status", ToxicityStatusEnum.class),
                            confidential))
                    .withToxicologicalDetails(createToxicologicalDetails(mapper)));
        }
        product.withOtherIngredientsExist(toBoolean(!list.isEmpty()));
        if (!list.isEmpty()) {
            product.withOtherIngredients(new OtherIngredients().withIngredient(list));
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
            list.add(casNumber(casNumber.trim()).withConfidential(confidential));
        }
        return new AdditionalCasNumbers().withCasNumber(list);
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

    private void createPresentations(final TobaccoProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Presentation");
        if (!objectMapper.isSelected("Presentation")) {
            return;
        }
        product.withPresentations(null);
        if (mappers.isEmpty()) {
            return;
        }
        final List<TobaccoPresentation> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final String subtypeName = mapper.getValue("Product_Brand_Subtype_Name", String.class);
            final String launchDate = mapper.getValue("Product_Launch_Date", String.class);
            final String withdrawalDate = mapper.getValue("Product_Withdrawal_Date", String.class);
            final List<AttachmentRef> otherMarketDataFiles = appendAttachments(mapper, "Product_Other_Market_Data");
            final List<AttachmentRef> packetPictureFiles = appendAttachments(mapper,
                "Product_Unit_Packet_Picture_File");

            list.add(new TobaccoPresentation().withAnnualSalesDataList(createAnnualSalesDataList(mapper))
                    .withBrandName(Eucegs.string100(mapper.getValue("Product_Brand_Name", String.class)))
                    .withBrandSubtypeName(Eucegs.string100(subtypeName))
                    .withBrandSubtypeNameExists(Eucegs.toBoolean(!Strings.isNullOrEmpty(subtypeName)))
                    .withHasOtherMarketData(toBooleanNullable(otherMarketDataFiles != null, true))
                    .withLaunchDate(Eucegs.toDate(launchDate))
                    .withNationalComment(Eucegs.string1000(mapper.getValue("Product_National_Comment", String.class)))
                    .withNationalMarket(
                        Eucegs.nationalMarket(mapper.getValue("Product_National_Market", NationalMarketValue.class)))
                    .withOtherMarketData(!otherMarketDataFiles.isEmpty()
                            ? new OtherMarketData().withAttachment(otherMarketDataFiles) : null)
                    .withPackageNetWeight(
                        Eucegs.decimal(mapper.getValue("Product_Package_Net_Weight", BigDecimal.class)))
                    .withPackageType(Eucegs.packageType(mapper.getValue("Product_Package_Type", PackageTypeEnum.class)))
                    .withPackageUnits(Eucegs.toInteger(mapper.getValue("Product_Package_Units", Integer.class)))
                    .withProductNumber(Eucegs.string40(mapper.getValue("Product_Submitter_Number", String.class)))
                    .withProductNumberType(new PresentationNumberType().withValue(PresentationNumberTypeEnum.SUBMITTER)
                            .withConfidential(false))
                    .withUnitPacketPictureFiles(!packetPictureFiles.isEmpty()
                            ? new UnitPacketPictureFiles().withAttachment(packetPictureFiles) : null)
                    .withWithdrawalDate(Eucegs.toDate(withdrawalDate))
                    .withWithdrawalIndication(Eucegs.toBoolean(!Strings.isNullOrEmpty(withdrawalDate))));
        }
        if (!list.isEmpty()) {
            product.withPresentations(new Presentations().withPresentation(list));
        }
    }

    private AnnualSalesDataList createAnnualSalesDataList(final ObjectMapper objectMapper) {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("SaleData");
        if (mappers.isEmpty()) {
            return null;
        }
        final List<TobaccoAnnualSalesData> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            list.add(new TobaccoAnnualSalesData()
                    .withMaximumSalesPrice(
                        Eucegs.decimal(mapper.getValue("Product_Maximum_Sales_Price", BigDecimal.class)))
                    .withSalesVolume(Eucegs.decimal(mapper.getValue("Product_Sales_Volume", BigDecimal.class), true))
                    .withYear(Eucegs.year(mapper.getValue("Product_Sales_Volume_Year", Integer.class), true)));
        }

        return new AnnualSalesDataList().withAnnualSalesData(list);
    }

    private TechnicalFiles createTechnicalFiles(final ObjectMapper objectMapper) throws EucegImportException {
        final List<AttachmentRef> atts = appendAttachments(objectMapper, "Product_Technical_File");
        if (atts.isEmpty()) {
            return null;
        }
        return new TechnicalFiles().withAttachment(atts);
    }

    private PriorityAdditiveFiles createPriorityAdditiveFiles(final @Nonnull ObjectMapper objectMapper)
            throws EucegImportException {
        final List<AttachmentRef> att = appendAttachments(objectMapper, "Ingredient_Priority_Additive_Files");
        if (att.size() == 0) {
            return null;
        }
        return new PriorityAdditiveFiles().withAttachment(att);
    }

    private void createTncoEmission(final TobaccoProduct product, final ObjectMapper objectMapper) {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Emission");
        if (!objectMapper.isSelected("Emission")) {
            return;
        }

        product.withTncoEmission(null);
        if (mappers.isEmpty()) {
            return;
        }
        final ObjectMapper mapper = Iterables.getFirst(mappers, null);
        if (mapper != null) {

            final String laboratory = mapper.getValue("Emission_TNCO_Lab", String.class);
            final BigDecimal co = mapper.getValue("Emission_CO", BigDecimal.class);
            final BigDecimal nicotine = mapper.getValue("Emission_Nicotine", BigDecimal.class);
            final BigDecimal tar = mapper.getValue("Emission_Tar", BigDecimal.class);
            if (Strings.isNullOrEmpty(laboratory) && co == null && nicotine == null && tar == null) {
                return;
            }
            product.withTncoEmission(new TncoEmission().withCo(decimal(co))
                    .withNicotine(decimal(nicotine))
                    .withTar(decimal(tar))
                    .withLaboratories(Strings.isNullOrEmpty(laboratory) ? null
                            : new Laboratories().withLaboratory(Eucegs.string500(laboratory))));
        }
    }

    private void createTobaccoIngredients(final TobaccoProduct product, final ObjectMapper objectMapper)
            throws EucegImportException {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Ingredient");
        if (!objectMapper.isSelected("Ingredient")) {
            return;
        }

        final List<TobaccoIngredient> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            final AttachmentRef partDescriptionFile = appendAttachment(mapper, "Tobacco_Part_Description_File");
            list.add(new TobaccoIngredient().withLeafCureMethod(
                Eucegs.leafCureMethod(mapper.getValue("Tobacco_Leaf_Cure_Method", LeafCureMethodEnum.class), true))
                    .withLeafCureMethodOther(
                        Eucegs.string100(mapper.getValue("Tobacco_Leaf_Cure_Method _Other", String.class), true))
                    .withLeafType(Eucegs.leafType(mapper.getValue("Tobacco_Leaf_Type", LeafTypeEnum.class), true))
                    .withLeafTypeOther(Eucegs.string100(mapper.getValue("Tobacco_Leaf_Type_Other", String.class), true))
                    .withPartDescriptionFiles(partDescriptionFile != null
                            ? new PartDescriptionFiles().withAttachment(partDescriptionFile) : null)
                    .withPartType(Eucegs.partType(mapper.getValue("Tobacco_Part_Type", PartTypeEnum.class), true))
                    .withPartTypeOther(Eucegs.string100(mapper.getValue("Tobacco_Part_Type_Other", String.class), true))
                    .withQuantity(Eucegs.decimal(mapper.getValue("Tobacco_Quantity", BigDecimal.class), true))
                    .withSuppliers(createSuppliers(mapper)));
        }
        product.withTobaccoIngredients(null);
        if (!list.isEmpty()) {
            product.withTobaccoIngredients(new TobaccoIngredients().withTobaccoIngredient(list));
        }
    }

    private Suppliers createSuppliers(final ObjectMapper objectMapper) {
        final List<ObjectMapper> mappers = objectMapper.getObjectMappers("Supplier");
        if (mappers.isEmpty()) {
            return null;
        }
        final List<Company> list = Lists.newArrayList();
        for (final ObjectMapper mapper : mappers) {
            list.add(createCompany(mapper, "Supplier_ID", true));
        }
        return new Suppliers().withSupplier(list);
    }

}
