package com.pmi.tpd.core.euceg;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static com.pmi.tpd.euceg.api.Eucegs.acuteToxDermalCode;
import static com.pmi.tpd.euceg.api.Eucegs.acuteToxInhalationCode;
import static com.pmi.tpd.euceg.api.Eucegs.acuteToxOralCode;
import static com.pmi.tpd.euceg.api.Eucegs.aspirationToxCode;
import static com.pmi.tpd.euceg.api.Eucegs.carcinogenicityCode;
import static com.pmi.tpd.euceg.api.Eucegs.casNumber;
import static com.pmi.tpd.euceg.api.Eucegs.decimal;
import static com.pmi.tpd.euceg.api.Eucegs.ecigProductType;
import static com.pmi.tpd.euceg.api.Eucegs.emissionName;
import static com.pmi.tpd.euceg.api.Eucegs.eyeDamageIrritationCode;
import static com.pmi.tpd.euceg.api.Eucegs.ingredientCategorie;
import static com.pmi.tpd.euceg.api.Eucegs.ingredientFunction;
import static com.pmi.tpd.euceg.api.Eucegs.mutagenGenotoxCode;
import static com.pmi.tpd.euceg.api.Eucegs.nationalMarket;
import static com.pmi.tpd.euceg.api.Eucegs.percentage;
import static com.pmi.tpd.euceg.api.Eucegs.productNumber;
import static com.pmi.tpd.euceg.api.Eucegs.reproductiveToxCode;
import static com.pmi.tpd.euceg.api.Eucegs.respiratorySensitisationCode;
import static com.pmi.tpd.euceg.api.Eucegs.skinCorrosiveIrritantCode;
import static com.pmi.tpd.euceg.api.Eucegs.skinSensitisationCode;
import static com.pmi.tpd.euceg.api.Eucegs.stotCode;
import static com.pmi.tpd.euceg.api.Eucegs.string100;
import static com.pmi.tpd.euceg.api.Eucegs.string1000;
import static com.pmi.tpd.euceg.api.Eucegs.string300;
import static com.pmi.tpd.euceg.api.Eucegs.string40;
import static com.pmi.tpd.euceg.api.Eucegs.string500;
import static com.pmi.tpd.euceg.api.Eucegs.toBoolean;
import static com.pmi.tpd.euceg.api.Eucegs.toBooleanNullable;
import static com.pmi.tpd.euceg.api.Eucegs.toDate;
import static com.pmi.tpd.euceg.api.Eucegs.toInteger;
import static com.pmi.tpd.euceg.api.Eucegs.year;

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import org.eu.ceg.AdditiveNumber;
import org.eu.ceg.CigaretteSpecific;
import org.eu.ceg.Company;
import org.eu.ceg.CountryValue;
import org.eu.ceg.Design;
import org.eu.ceg.EcNumber;
import org.eu.ceg.EcigAnnualSalesData;
import org.eu.ceg.EcigEmission;
import org.eu.ceg.EcigIngredient;
import org.eu.ceg.EcigPresentation;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.EmissionNameEnum;
import org.eu.ceg.FemaNumber;
import org.eu.ceg.FlNumber;
import org.eu.ceg.Ingredient;
import org.eu.ceg.Ingredient.AdditionalCasNumbers;
import org.eu.ceg.IngredientCategoryEnum;
import org.eu.ceg.IngredientFunctionEnum;
import org.eu.ceg.Manufacturer;
import org.eu.ceg.Manufacturer.ProductionSiteAddresses;
import org.eu.ceg.NationalMarketValue;
import org.eu.ceg.PresentationNumberType;
import org.eu.ceg.Product.Manufacturers;
import org.eu.ceg.Product.OtherProducts;
import org.eu.ceg.Product.SameCompositionProducts;
import org.eu.ceg.ProductIdentification;
import org.eu.ceg.ProductIdentificationType;
import org.eu.ceg.ProductionSiteAddress;
import org.eu.ceg.RyoPipeSpecific;
import org.eu.ceg.SmokelessSpecific;
import org.eu.ceg.Submission;
import org.eu.ceg.TncoEmission;
import org.eu.ceg.TncoEmission.Laboratories;
import org.eu.ceg.TobaccoAnnualSalesData;
import org.eu.ceg.TobaccoEmission;
import org.eu.ceg.TobaccoIngredient;
import org.eu.ceg.TobaccoOtherIngredient;
import org.eu.ceg.TobaccoPresentation;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProduct.OtherEmissions;
import org.eu.ceg.TobaccoProduct.OtherIngredients;
import org.eu.ceg.TobaccoProduct.TechnicalFiles;
import org.eu.ceg.TobaccoProduct.TobaccoIngredients;
import org.eu.ceg.TobaccoProductSubmission;
import org.eu.ceg.ToxicityStatus;
import org.eu.ceg.ToxicityStatusEnum;
import org.eu.ceg.ToxicologicalDetails;
import org.joda.time.LocalDate;

import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.core.refs.PackageTypeEnum;
import com.pmi.tpd.euceg.core.refs.ProductNumberTypeEnum;
import com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum;
import com.pmi.tpd.euceg.core.refs.TobaccoLeafCureMethod;
import com.pmi.tpd.euceg.core.refs.TobaccoLeafType;
import com.pmi.tpd.euceg.core.refs.TobaccoPartType;
import com.pmi.tpd.euceg.core.refs.TobaccoProductTypeEnum;
import com.pmi.tpd.euceg.core.refs.ToxicologicalDataAvailableEnum;
import com.pmi.tpd.euceg.core.refs.VoltageWattageAdjustableEnum;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ProductSubmissionHelper {

    private ProductSubmissionHelper() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * @param submission
     * @return
     */
    public static SubmissionEntity entity(final @Nonnull Submission submission) {
        checkNotNull(submission, "submission");
        String productId = null;
        if (submission instanceof TobaccoProductSubmission) {
            productId = ((TobaccoProductSubmission) submission).getProduct().getProductID().getValue();
        } else if (submission instanceof EcigProductSubmission) {
            productId = ((EcigProductSubmission) submission).getProduct().getProductID().getValue();
        } else if (submission instanceof EcigProductSubmission) {
            throw new RuntimeException(
                    "the type of submission '" + submission.getClass().getName() + "' is not supported");
        }
        return SubmissionEntity.builder()
                .submission(submission)
                .productId(productId)
                .product(ProductEntity.builder().productNumber("FA016217").build())
                .productType(ProductType.productType(submission))
                .submitterId(submission.getSubmitter().getSubmitterID())
                .build();
    }

    public static TobaccoProductSubmission okTobaccoProductFirstSubmission() {
        return okTobaccoProductFirstSubmission(null);
    }

    /**
     * @return
     */
    public static TobaccoProductSubmission okTobaccoProductFirstSubmission(final TobaccoProduct product) {
        final TobaccoProductSubmission tobaccoProductSubmission = new TobaccoProductSubmission()
                .withGeneralComment(string1000("string 1000 First Tobacco Product version").withConfidential(true))
                .withSubmissionType(SubmissionTypeEnum.NEW_PRODUCT.toSubmissionType().withConfidential(false))
                .withSubmitter(SubmitterHelper.okSubmitter())
                .withProduct(product == null ? okFirstTobaccoProduct() : product);
        final ISubmissionEntity tobaccoEntity = SubmissionEntity.builder().submission(tobaccoProductSubmission).build();
        return (TobaccoProductSubmission) tobaccoEntity.getSubmission();
    }

    public static TobaccoProductSubmission okSimpleTobaccoProductFirstSubmission() {
        final TobaccoProductSubmission tobaccoProductSubmission = new TobaccoProductSubmission()
                .withGeneralComment(string1000("string 1000 First Tobacco Product version").withConfidential(true))
                .withSubmissionType(SubmissionTypeEnum.NEW_PRODUCT.toSubmissionType().withConfidential(false))
                .withSubmitter(SubmitterHelper.okSimpleSubmitter())
                .withProduct(okSimpleFirstTobaccoProduct());
        final ISubmissionEntity tobaccoEntity = SubmissionEntity.builder().submission(tobaccoProductSubmission).build();
        return (TobaccoProductSubmission) tobaccoEntity.getSubmission();
    }

    public static TobaccoProductSubmission okTobaccoProductCorrectSubmission() {
        final TobaccoProduct product = okCorrectFirstTobaccoProduct();
        return new TobaccoProductSubmission()
                .withGeneralComment(
                    string1000("string 1000 Correction Tobacco Product " + product.getPreviousProductID().getValue())
                            .withConfidential(true))
                .withSubmissionType(SubmissionTypeEnum.CORRECTION.toSubmissionType().withConfidential(true))
                .withSubmitter(SubmitterHelper.okSubmitter().withConfidential(true))
                .withProduct(product);
    }

    public static TobaccoProduct okCorrectFirstTobaccoProduct() {
        final TobaccoProduct previousProduct = okFirstTobaccoProduct();
        return okFirstTobaccoProduct().withProductID(productNumber("12345-19-00015"))
                .withPreviousProductID(productNumber(previousProduct.getProductID().getValue()).withConfidential(true))
                .withLength(decimal(new BigDecimal("9.0")))
                .withTobaccoWeight(decimal(new BigDecimal("20.4")))
                .withWeight(decimal(new BigDecimal("21.1")))
                .withDiameter(decimal(new BigDecimal("1.1")));
    }

    public static TobaccoProduct okSimpleFirstTobaccoProduct() {
        return new TobaccoProduct().withProductID(productNumber("12345-20-00230").withConfidential(true))
                .withProductType(TobaccoProductTypeEnum.CIGARETTE.toTobaccoProductType().withConfidential(true))
                .withLength(decimal(new BigDecimal("11.5")).withConfidential(true))
                .withTobaccoWeight(decimal(new BigDecimal("25.4")).withConfidential(true))
                .withWeight(decimal(new BigDecimal("2.1")).withConfidential(true))
                .withDiameter(decimal(new BigDecimal("1.4")).withConfidential(true))
                .withFilter(toBooleanNullable(true).withConfidential(true))
                .withFilterLength(toInteger(30).withConfidential(true))
                .withTechnicalFiles(technicalFiles())
                .withPresentations(new TobaccoProduct.Presentations().withPresentation(
                    tobaccoPresentation(PackageTypeEnum.FLIP_TOP_BOX_ROUNDED_CORNER, NationalMarketValue.FR),
                    tobaccoPresentation(PackageTypeEnum.CARTON_BOX, NationalMarketValue.DE)))
                .withCigaretteSpecific(
                    new CigaretteSpecific().withCharacterisingFlavour(toBoolean(true).withConfidential(true))
                            .withFilterDropPressureClosed(decimal(new BigDecimal("1.2")).withConfidential(true))
                            .withFilterDropPressureOpen(decimal(new BigDecimal("3.4")).withConfidential(true))
                            .withFilterVentilation(percentage(80).withConfidential(true)))
                .withManufacturers(new Manufacturers().withManufacturer(okSimpleManufacturer().withConfidential(true)))
                .withOtherIngredients(new OtherIngredients().withIngredient(tobaccoOtherIngredient("other ingredient"),
                    tobaccoOtherIngredient("First ingredient")))
                .withOtherProducts(new OtherProducts().withProductIdentification(
                    new ProductIdentification().withConfidential(true)
                            .withType(ProductIdentificationType.PRODUCT_ID)
                            .withValue("12345-10-0001"),
                    new ProductIdentification().withConfidential(true)
                            .withType(ProductIdentificationType.TEXT)
                            .withValue("Product Z")))
                .withOtherProductsExist(toBooleanNullable(true))
                .withSameCompositionProductsExist(toBooleanNullable(false))
                .withTncoEmission(new TncoEmission().withCo(decimal(new BigDecimal("12.5")).withConfidential(true))
                        .withLaboratories(new Laboratories().withLaboratory(
                            string500("string 300 laboratory 1").withConfidential(true),
                            string500("string 300 laboratory 2").withConfidential(true)))
                        .withNicotine(decimal(new BigDecimal("11.1")).withConfidential(true))
                        .withTar(decimal(new BigDecimal("20.4")).withConfidential(true)))
                .withTobaccoIngredients(new TobaccoIngredients().withTobaccoIngredient(
                    tobaccoSimpleIngredient(TobaccoLeafCureMethod.AIR,
                        TobaccoLeafType.KENTUCKY,
                        TobaccoPartType.MANUFACTURED_CUT_STEMS),
                    tobaccoSimpleIngredient(TobaccoLeafCureMethod.SUN,
                        TobaccoLeafType.MARYLAND,
                        TobaccoPartType.MANUFACTURED_EXPANDED_TOBACCO)));

    }

    public static TobaccoProduct okFirstTobaccoProduct() {
        return okFirstTobaccoProduct(null);
    }

    public static TobaccoProduct okFirstTobaccoProduct(final BigDecimal length) {
        return new TobaccoProduct().withProductID(productNumber(Eucegs.UNDEFINED_PRODUCT_ID))
                .withProductType(TobaccoProductTypeEnum.CIGARETTE.toTobaccoProductType().withConfidential(true))
                .withLength(decimal(length != null ? length : new BigDecimal("11.5")).withConfidential(true))
                .withTobaccoWeight(decimal(new BigDecimal("25.4")).withConfidential(true))
                .withWeight(decimal(new BigDecimal("2.1")).withConfidential(true))
                .withDiameter(decimal(new BigDecimal("1.4")).withConfidential(true))
                .withFilter(toBooleanNullable(true).withConfidential(true))
                .withFilterLength(toInteger(30).withConfidential(true))
                .withPresentations(new TobaccoProduct.Presentations().withPresentation(
                    tobaccoPresentation(PackageTypeEnum.FLIP_TOP_BOX_ROUNDED_CORNER, NationalMarketValue.FR),
                    tobaccoPresentation(PackageTypeEnum.CARTON_BOX, NationalMarketValue.DE)))
                .withCigaretteSpecific(
                    new CigaretteSpecific().withCharacterisingFlavour(toBoolean(true).withConfidential(true))
                            .withFilterDropPressureClosed(decimal(new BigDecimal("1.2")).withConfidential(true))
                            .withFilterDropPressureOpen(decimal(new BigDecimal("3.4")).withConfidential(true))
                            .withFilterVentilation(percentage(80).withConfidential(true)))
                .withManufacturers(new Manufacturers().withManufacturer(okManufacturer().withConfidential(true)))
                // .withMarketResearchFiles(new MarketResearchFiles().withAttachment(attachmentRef(uuid())))
                // .withTechnicalFiles(technicalFiles())
                // .withNovelSpecific(new NovelSpecific())
                // .withDetailsDescriptionFile(attachmentRef(uuid()))
                // .withRiskBenefitFile(attachmentRef(uuid()))
                // .withStudyFiles(new StudyFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                // .withUseInstructionsFile(attachmentRef(uuid())))
                .withOtherEmissions(new OtherEmissions().withEmission(tobaccoEmission()))
                .withOtherIngredients(new OtherIngredients().withIngredient(tobaccoOtherIngredient("other ingredient"),
                    tobaccoOtherIngredient("First ingredient")))
                .withOtherProducts(new OtherProducts().withProductIdentification(
                    new ProductIdentification().withConfidential(true)
                            .withType(ProductIdentificationType.PRODUCT_ID)
                            .withValue("12345-10-0001"),
                    new ProductIdentification().withConfidential(true)
                            .withType(ProductIdentificationType.TEXT)
                            .withValue("Product Z")))
                .withOtherProductsExist(toBooleanNullable(true))
                .withRyoPipeSpecific(new RyoPipeSpecific()
                        .withTotalNicotineContent(decimal(new BigDecimal("11.0")).withConfidential(true))
                        .withUnionisedNicotineContent(decimal(new BigDecimal("2.0")).withConfidential(true)))
                .withSameCompositionProducts(new SameCompositionProducts())
                .withSmokelessSpecific(smokelessSpecific())

                .withTncoEmission(new TncoEmission().withCo(decimal(new BigDecimal("12.5")).withConfidential(true))
                        .withLaboratories(new Laboratories().withLaboratory(
                            string500("string 300 laboratory 1").withConfidential(true),
                            string500("string 300 laboratory 2").withConfidential(true)))
                        .withNicotine(decimal(new BigDecimal("11.1")).withConfidential(true))
                        .withTar(decimal(new BigDecimal("20.4")).withConfidential(true)))
                .withTobaccoIngredients(new TobaccoIngredients().withTobaccoIngredient(
                    tobaccoIngredient(TobaccoLeafCureMethod.AIR,
                        TobaccoLeafType.KENTUCKY,
                        TobaccoPartType.MANUFACTURED_CUT_STEMS),
                    tobaccoIngredient(TobaccoLeafCureMethod.SUN,
                        TobaccoLeafType.MARYLAND,
                        TobaccoPartType.MANUFACTURED_EXPANDED_TOBACCO)));

    }

    public static Manufacturer okSimpleManufacturer() {
        return new Manufacturer().withSubmitterID("00009")
                .withProductionSiteAddresses(new ProductionSiteAddresses()
                        .withProductionSiteAddress(new ProductionSiteAddress().withAddress("45 rue des heros")
                                .withCountry(CountryValue.FR)
                                .withEmail("productioncenter@company.com")
                                .withPhoneNumber("+339252255522")));
    }

    public static Manufacturer okManufacturer() {
        return new Manufacturer().withAddress("1 rue de hall")
                .withCountry(CountryValue.FR)
                .withEmail("manufacturer@company.com")
                .withName("Manufacturer")
                .withPhoneNumber("+33485242245")
                .withSubmitterID("00004")
                .withProductionSiteAddresses(new ProductionSiteAddresses()
                        .withProductionSiteAddress(new ProductionSiteAddress().withAddress("45 rue des heros")
                                .withCountry(CountryValue.FR)
                                .withEmail("productioncenter@company.com")
                                .withPhoneNumber("+339252255522")));
    }

    public static Company okCompany() {
        return new Company().withAddress("address")
                .withConfidential(true)
                .withCountry(CountryValue.FR)
                .withEmail("company@company.com")
                .withName("company")
                .withPhoneNumber("+33458686886")
                .withSubmitterID("00009");
    }

    public static TobaccoPresentation tobaccoPresentation(final PackageTypeEnum packageType,
        final NationalMarketValue nationalMarket) {
        return new TobaccoPresentation().withLaunchDate(toDate(LocalDate.now()))
                .withNationalComment(
                    string1000("string 1000 National comment " + nationalMarket).withConfidential(true))
                .withPackageType(packageType.toPackageType().withConfidential(true))
                .withPackageUnits(toInteger(20).withConfidential(true))
                .withBrandName(string100("string 100 Brand name " + nationalMarket).withConfidential(true))
                .withNationalMarket(nationalMarket(nationalMarket).withConfidential(true))
                .withProductNumber(string40("string 40 roduct number").withConfidential(true))
                .withWithdrawalIndication(toBoolean(false).withConfidential(true))
                .withProductNumberType(new PresentationNumberType().withConfidential(true)
                        .withValue(ProductNumberTypeEnum.SUBMITTER.toPresentationNumberType()))
                .withAnnualSalesDataList(new TobaccoPresentation.AnnualSalesDataList().withAnnualSalesData(
                    new TobaccoAnnualSalesData()
                            .withMaximumSalesPrice(decimal(new BigDecimal("9.5")).withConfidential(true))
                            .withSalesVolume(decimal(new BigDecimal("15000.0")).withConfidential(true))
                            .withYear(year(2012).withConfidential(true)),
                    new TobaccoAnnualSalesData()
                            .withMaximumSalesPrice(decimal(new BigDecimal("9.8")).withConfidential(true))
                            .withSalesVolume(decimal(new BigDecimal("30000.0")).withConfidential(true))
                            .withYear(year(2013).withConfidential(true))))
                .withHasOtherMarketData(toBooleanNullable(true).withConfidential(true));
        // .withOtherMarketData(new TobaccoPresentation.OtherMarketData().withAttachment(attachmentRef(uuid()),
        // attachmentRef(uuid())))
        // .withUnitPacketPictureFiles(
        // new UnitPacketPictureFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())));
    }

    public static TobaccoEmission tobaccoEmission() {
        return new TobaccoEmission().withCasNumber(casNumber("1052-10-1").withConfidential(true))
                .withIupacName(string100("string 100").withConfidential(true))
                // .withMethodsFile(new MethodsFile().withAttachment(attachmentRef(uuid())))
                .withName(string100("string 100 tobacco emmission name").withConfidential(true))
                .withQuantity(decimal(new BigDecimal("1.0")).withConfidential(true))
                .withUnit(string40("string 40").withConfidential(true));
    }

    public static TechnicalFiles technicalFiles() {
        return new TechnicalFiles();
        // return new TechnicalFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid()));
    }

    public static SmokelessSpecific smokelessSpecific() {
        return new SmokelessSpecific().withAnalysisMethods(string300("string 300").withConfidential(true))
                .withPh(decimal(new BigDecimal("7.0")).withConfidential(true))
                .withTotalMoisture(decimal(new BigDecimal("1.0")).withConfidential(true))
                .withTotalNicotineContent(decimal(new BigDecimal("11.0")).withConfidential(true))
                .withUnionisedNicotineContent(decimal(new BigDecimal("1.0")).withConfidential(true));
    }

    public static TobaccoIngredient tobaccoSimpleIngredient(final TobaccoLeafCureMethod leafCureMethod,
        final TobaccoLeafType leafType,
        final TobaccoPartType partType) {
        return new TobaccoIngredient().withLeafCureMethod(leafCureMethod.toLeafCureMethod().withConfidential(true))
                .withLeafType(leafType.toLeafType().withConfidential(true))
                .withPartType(partType.toPartType().withConfidential(true))
                .withQuantity(decimal(new BigDecimal("12.0")).withConfidential(true))
                .withSuppliers(new TobaccoIngredient.Suppliers().withSupplier(okCompany().withName("supplier"),
                    okCompany().withName("supplier")));
    }

    public static TobaccoIngredient tobaccoIngredient(final TobaccoLeafCureMethod leafCureMethod,
        final TobaccoLeafType leafType,
        final TobaccoPartType partType) {
        return new TobaccoIngredient().withLeafCureMethod(leafCureMethod.toLeafCureMethod().withConfidential(true))
                .withLeafCureMethodOther(string100("string 100 Other method").withConfidential(true))
                .withLeafType(leafType.toLeafType().withConfidential(true))
                .withLeafTypeOther(string100("strin 100 Leaf type other").withConfidential(true))
                // .withPartDescriptionFiles(
                // new PartDescriptionFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                .withPartType(partType.toPartType().withConfidential(true))
                .withPartTypeOther(string100("string 100 part type other").withConfidential(true))
                .withQuantity(decimal(new BigDecimal("12.0")).withConfidential(true))
                .withSuppliers(new TobaccoIngredient.Suppliers().withSupplier(okCompany().withName("supplier"),
                    okCompany().withName("supplier")));
    }

    public static TobaccoOtherIngredient tobaccoOtherIngredient(final String name) {
        return new TobaccoOtherIngredient().withName(string300(name).withConfidential(true))
                .withAdditionalCasNumbers(new AdditionalCasNumbers().withCasNumber(casNumber("1234-21-1"))
                        .withCasNumber(casNumber("12345-12-1")))
                .withAdditiveNumber(new AdditiveNumber().withValue("E1402").withConfidential(true))
                .withCasNumber(casNumber("1234-02-2").withConfidential(true))
                .withCasNumberExists(toBooleanNullable(true).withConfidential(true))
                .withCategory(ingredientCategorie(IngredientCategoryEnum.FILTER_ADHESIVE).withConfidential(true))
                .withCategoryOther(string100("string 100 category other").withConfidential(true))
                .withClpAcuteToxDermal(acuteToxDermalCode("1").withConfidential(true))
                .withClpAcuteToxInhalation(acuteToxInhalationCode("1").withConfidential(true))
                .withClpAcuteToxOral(acuteToxOralCode("1").withConfidential(true))
                .withClpAspirationTox(aspirationToxCode("1").withConfidential(true))
                .withClpCarcinogenicity(carcinogenicityCode("1A").withConfidential(true))
                .withClpEyeDamageIrritation(eyeDamageIrritationCode("1").withConfidential(true))
                .withClpMutagenGenotox(mutagenGenotoxCode("1A").withConfidential(true))
                .withClpReproductiveTox(reproductiveToxCode("1A").withConfidential(true))
                .withClpRespiratorySensitisation(respiratorySensitisationCode("1").withConfidential(true))
                .withClpSkinCorrosiveIrritant(skinCorrosiveIrritantCode("1A").withConfidential(true))
                .withClpSkinSensitisation(skinSensitisationCode("1").withConfidential(true))
                .withClpStot(stotCode("1").withConfidential(true))
                .withClpStotDescription(string500("withClpStotDescription 500 string").withConfidential(true))
                .withClpWhetherClassification(toBooleanNullable(true).withConfidential(true))
                .withEcNumber(new EcNumber().withValue("200-003-9").withConfidential(true))
                .withFemaNumber(new FemaNumber().withValue("0553").withConfidential(true))
                .withFlNumber(new FlNumber().withValue("FL16.012").withConfidential(true))
                .withFunctionOther(string100("function other string 100").withConfidential(true))
                .withFunctions(new Ingredient.Functions().withFunction(
                    ingredientFunction(IngredientFunctionEnum.ADHESIVE).withConfidential(true),
                    ingredientFunction(IngredientFunctionEnum.SMOKE_COLOUR_MODIFIER).withConfidential(true)))
                .withMeasuredMaxLevel(decimal(new BigDecimal("2.5")).withConfidential(true))
                .withMeasuredMeanQuantity(decimal(new BigDecimal("1.2")).withConfidential(true))
                .withMeasuredMinLevel(decimal(new BigDecimal("1.01")).withConfidential(true))
                .withMeasuredNumber(decimal(new BigDecimal("5.2")).withConfidential(true))
                .withMeasuredSd(decimal(new BigDecimal("4.1")).withConfidential(true))
                .withPriorityAdditive(toBooleanNullable(true).withConfidential(true))
                // .withPriorityAdditiveFiles(
                // new PriorityAdditiveFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                .withQuantityFluctuate(toBooleanNullable(true).withConfidential(true))
                .withToxicityStatus(toxicityStatus())
                .withReachRegistrationNumber(string40("Reach Registration 40 string").withConfidential(true))
                .withRecipeQuantity(decimal(new BigDecimal("15.2")).withConfidential(true))
                .withRecipeRangeMaxLevel(decimal(new BigDecimal("40.0")).withConfidential(true))
                .withRecipeRangeMinLevel(decimal(new BigDecimal("10.0")).withConfidential(true))
                .withToxicologicalDetails(
                    new ToxicologicalDetails().withToxAddictive(toBoolean(true).withConfidential(true))
                            // .withToxAddictiveFiles(
                            // new ToxAddictiveFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxCardioPulmonary(toBoolean(true).withConfidential(true))
                            // .withToxCardioPulmonaryFiles(new ToxCardioPulmonaryFiles()
                            // .withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxCmr(toBoolean(true).withConfidential(true))
                            // .withToxCmrFiles(
                            // new ToxCmrFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxEmission(toBoolean(true).withConfidential(true))
                            // .withToxEmissionFiles(
                            // new ToxEmissionFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxicologicalDataAvailable(ToxicologicalDataAvailableEnum.TOXICOLOGICAL_DATA_AVAILABLE
                                    .toToxicologicalDataAvailable()
                                    .withConfidential(true))
                            .withToxOther(toBoolean(true).withConfidential(true)))
        // .withToxOtherFiles(
        // new ToxOtherFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid()))))
        // .withUnburntStatus(UnburntStatusEnum.NOT_TOXIC.toUnburntStatus().withConfidential(true))
        ;
    }

    private static ToxicityStatus toxicityStatus() {
        return new ToxicityStatus().withValue(ToxicityStatusEnum.NOT_AVAILABLE).withConfidential(true);
    }

    public static EcigProductSubmission okEcigProductFirstSubmission() {
        return new EcigProductSubmission()
                .withGeneralComment(string1000("First Ecig Product version").withConfidential(true))
                .withSubmissionType(SubmissionTypeEnum.NEW_PRODUCT.toSubmissionType().withConfidential(false))
                .withSubmitter(SubmitterHelper.okSubmitter())
                .withProduct(okFirstEcigProduct());
    }

    public static EcigProduct okFirstEcigProduct() {
        return new EcigProduct().withProductID(productNumber(Eucegs.UNDEFINED_PRODUCT_ID))
                .withProductType(ecigProductType(EcigProductTypeEnum.RECHARGEABLE_DEVICE_ONLY).withConfidential(true))
                .withWeight(decimal(new BigDecimal("2.1")).withConfidential(true))
                .withVolume(decimal(new BigDecimal("11")))
                .withClpClassification(string300("String 300").withConfidential(true))
                .withPresentations(new EcigProduct.Presentations()
                        .withPresentation(presentation(NationalMarketValue.FR), presentation(NationalMarketValue.DE)))
                .withManufacturers(new Manufacturers().withManufacturer(okManufacturer().withConfidential(true)))
                .withEmissions(new EcigProduct.Emissions().withEmission(emission()))
                .withOtherProductsExist(toBooleanNullable(true).withConfidential(true))
                .withOtherProducts(new OtherProducts().withProductIdentification(
                    new ProductIdentification().withConfidential(true)
                            .withType(ProductIdentificationType.PRODUCT_ID)
                            .withValue("12345-10-0001"),
                    new ProductIdentification().withConfidential(true)
                            .withType(ProductIdentificationType.TEXT)
                            .withValue("Product Z")))
                .withSameCompositionProductsExist(toBooleanNullable(true).withConfidential(true))
                .withSameCompositionProducts(new SameCompositionProducts())
                .withIngredients(new EcigProduct.Ingredients().withIngredient(ingredient()))
                .withDesign(design());

    }

    public static EcigEmission emission() {
        return new EcigEmission().withCasNumber(casNumber("1052-10-2").withConfidential(true))
                .withIupacName(string100("string 100").withConfidential(true))
                .withName(emissionName(EmissionNameEnum.ACETYL_PROPIONYL).withConfidential(true))
                .withQuantity(decimal(new BigDecimal("1.0")).withConfidential(true))
                .withUnit(string40("unit").withConfidential(true))
                .withProductCombination(string500("withProductCombination string 500").withConfidential(true));
    }

    public static EcigPresentation presentation(final NationalMarketValue nationalMarket) {
        return new EcigPresentation().withLaunchDate(toDate(LocalDate.now()))
                .withNationalComment(
                    string1000("String 1000 National comment " + nationalMarket).withConfidential(true))
                // .withPackageType(packageType.toPackageType().withConfidential(true))
                .withPackageUnits(toInteger(20).withConfidential(true))
                .withBrandName(string100("string 100 Brand name " + nationalMarket).withConfidential(true))
                .withBrandSubtypeName(string100("string 100"))
                .withNationalMarket(nationalMarket(nationalMarket).withConfidential(true))
                .withProductNumber(string40("string 40").withConfidential(true))
                .withProductNumberType(new PresentationNumberType().withConfidential(true)
                        .withValue(ProductNumberTypeEnum.SUBMITTER.toPresentationNumberType()))
                .withWithdrawalIndication(toBoolean(false).withConfidential(true))
                .withAnnualSalesDataList(new EcigPresentation.AnnualSalesDataList().withAnnualSalesData(
                    new EcigAnnualSalesData().withSalesVolume(decimal(new BigDecimal("15000.0")).withConfidential(true))
                            .withYear(year(2012).withConfidential(true)),
                    new EcigAnnualSalesData().withSalesVolume(decimal(new BigDecimal("30000.0")).withConfidential(true))
                            .withYear(year(2013).withConfidential(true))));
    }

    public static EcigIngredient ingredient() {
        return new EcigIngredient()
                .withAdditionalCasNumbers(new AdditionalCasNumbers()
                        .withCasNumber(casNumber("1511-15-7").withConfidential(true), casNumber("3398-14-8")))
                .withAdditiveNumber(new AdditiveNumber().withValue("E1402").withConfidential(true))
                .withCasNumber(casNumber("568-45-5").withConfidential(true))
                .withCasNumberExists(toBooleanNullable(true).withConfidential(true))
                .withClpAcuteToxDermal(acuteToxDermalCode("1A").withConfidential(true))
                .withClpAcuteToxInhalation(acuteToxInhalationCode("1A").withConfidential(true))
                .withClpAcuteToxOral(acuteToxOralCode("1A").withConfidential(true))
                .withClpAspirationTox(aspirationToxCode("1A").withConfidential(true))
                .withClpCarcinogenicity(carcinogenicityCode("1A").withConfidential(true))
                .withClpEyeDamageIrritation(eyeDamageIrritationCode("1A").withConfidential(true))
                .withClpMutagenGenotox(mutagenGenotoxCode("1A").withConfidential(true))
                .withClpReproductiveTox(reproductiveToxCode("1A").withConfidential(true))
                .withClpRespiratorySensitisation(respiratorySensitisationCode("1A").withConfidential(true))
                .withClpSkinCorrosiveIrritant(skinCorrosiveIrritantCode("1A").withConfidential(true))
                .withClpSkinSensitisation(skinSensitisationCode("1A").withConfidential(true))
                .withClpStot(stotCode("1A").withConfidential(true))
                .withClpStotDescription(string500("withClpStotDescription 500 string").withConfidential(true))
                .withClpWhetherClassification(toBooleanNullable(true).withConfidential(true))
                .withEcNumber(new EcNumber().withValue("200-003-9").withConfidential(true))
                .withFemaNumber(new FemaNumber().withValue("0553").withConfidential(true))
                .withFlNumber(new FlNumber().withValue("FL16.012").withConfidential(true))
                .withFunctionOther(string100("function other string 100").withConfidential(true))
                .withFunctions(new Ingredient.Functions().withFunction(
                    ingredientFunction(IngredientFunctionEnum.ADHESIVE).withConfidential(true),
                    ingredientFunction(IngredientFunctionEnum.SMOKE_COLOUR_MODIFIER).withConfidential(true)))
                .withName(string300("string 100").withConfidential(true))
                .withReachRegistrationNumber(string40("string 40").withConfidential(true))
                /* TODO missing Ingredient_Non_Vaporised_Status */
                .withRecipeQuantity(decimal(new BigDecimal("1.5")).withConfidential(true))
                .withToxicologicalDetails(
                    new ToxicologicalDetails().withToxAddictive(toBoolean(true).withConfidential(true))
                            // .withToxAddictiveFiles(
                            // new ToxAddictiveFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxCardioPulmonary(toBoolean(true).withConfidential(true))
                            // .withToxCardioPulmonaryFiles(new ToxCardioPulmonaryFiles()
                            // .withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxCmr(toBoolean(true).withConfidential(true))
                            // .withToxCmrFiles(
                            // new ToxCmrFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxEmission(toBoolean(true).withConfidential(true))
                            // .withToxEmissionFiles(
                            // new ToxEmissionFiles().withAttachment(attachmentRef(uuid()), attachmentRef(uuid())))
                            .withToxicologicalDataAvailable(ToxicologicalDataAvailableEnum.TOXICOLOGICAL_DATA_AVAILABLE
                                    .toToxicologicalDataAvailable()
                                    .withConfidential(true))
                            .withToxOther(toBoolean(true).withConfidential(true)));
    }

    public static Design design() {
        return new Design().withAirflowAdjustable(toBoolean(true).withConfidential(true))
                .withBatteryCapacity(decimal(new BigDecimal("5.4")).withConfidential(true))
                .withBatteryType(string300("withBatteryType string 300").withConfidential(true))
                .withChildTamperProof(toBooleanNullable(true).withConfidential(true))
                .withCoilComposition(string300("withCoilComposition string 300").withConfidential(true))
                .withCoilResistance(decimal(new BigDecimal("1.2")).withConfidential(true))
                .withConsistentDosing(toBooleanNullable(true).withConfidential(true))
                .withDescription(string1000("withDescriptionstring 1000").withConfidential(true))
                .withHighPurity(toBooleanNullable(true).withConfidential(true))
                .withLiquidVolumeCapacity(decimal(new BigDecimal("5.0")).withConfidential(true))
                .withMicroprocessor(toBoolean(true).withConfidential(true))
                .withNicotineConcentration(decimal(new BigDecimal("1.0")).withConfidential(true))
                .withNonRisk(toBooleanNullable(true).withConfidential(true))
                .withProductionConformity(toBooleanNullable(true).withConfidential(true))
                .withQualitySafety(toBooleanNullable(true).withConfidential(true))
                .withVoltage(decimal(new BigDecimal("1.5")).withConfidential(true))
                .withVoltageLowerRange(decimal(new BigDecimal("0.5")).withConfidential(true))
                .withVoltageUpperRange(decimal(new BigDecimal("1.8")).withConfidential(true))
                .withVoltageWattageAdjustable(
                    VoltageWattageAdjustableEnum.YES_ONLY_VOLTAGE_ADJUSTABLE.toVoltageWattageAdjustable()
                            .withConfidential(true))
                .withWattage(decimal(new BigDecimal("18.0")).withConfidential(true))
                .withWattageLowerRange(decimal(new BigDecimal("1.0")).withConfidential(true))
                .withWattageUpperRange(decimal(new BigDecimal("70.0")).withConfidential(true))
                .withWickChangeable(toBoolean(true).withConfidential(true));
    }

}
