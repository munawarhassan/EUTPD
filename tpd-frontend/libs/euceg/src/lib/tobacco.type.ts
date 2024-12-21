import { AttachmentRef, Company, Product } from '.';
import {
    AdditionalCasNumbers,
    AnnualSalesData,
    Attachments,
    ConfidentialValue,
    Emission,
    Functions,
    Presentation,
    ToxicologicalDetails,
} from './product.type';

export interface TobaccoAnnualSalesData extends AnnualSalesData {
    MaximumSalesPrice?: ConfidentialValue<number>;
}

export interface TobaccoAnnualSalesDataList {
    AnnualSalesData: TobaccoAnnualSalesData[];
}

export interface TobaccoPresentation extends Presentation {
    PackageType: ConfidentialValue<string>;
    PackageUnits?: ConfidentialValue<number>;
    PackageNetWeight?: ConfidentialValue<number>;
    HasOtherMarketData: ConfidentialValue<boolean>;
    OtherMarketData?: Attachments;
    UnitPacketPictureFiles?: Attachments;
    AnnualSalesDataList: TobaccoAnnualSalesDataList;
}

export interface TobaccoPresentations {
    Presentation: TobaccoPresentation[];
}

export interface Suppliers {
    Supplier: Company[];
}

export interface TobaccoIngredient {
    PartType: ConfidentialValue<string>;
    PartTypeOther?: any;
    LeafType: ConfidentialValue<string>;
    LeafTypeOther?: any;
    LeafCureMethod: ConfidentialValue<string>;
    LeafCureMethodOther?: any;
    Quantity?: ConfidentialValue<string>;
    PartDescriptionFiles?: Attachments;
    Suppliers?: Suppliers;
}

export interface TobaccoIngredients {
    TobaccoIngredient: TobaccoIngredient[];
}

export interface TobaccoOtherIngredients {
    Ingredient: TobaccoOtherIngredient[];
}

export interface TobaccoOtherIngredient {
    Category: ConfidentialValue<string>;
    CategoryOther?: any;
    QuantityFluctuate: ConfidentialValue<boolean>;
    RecipeRangeMinLevel?: any;
    RecipeRangeMaxLevel?: any;
    MeasuredMeanQuantity?: any;
    MeasuredSd?: any;
    MeasuredMinLevel?: any;
    MeasuredMaxLevel?: any;
    MeasuredNumber?: any;
    PriorityAdditive: ConfidentialValue<boolean>;
    PriorityAdditiveFiles: Attachments;
    Name: ConfidentialValue<string>;
    CasNumberExists: ConfidentialValue<boolean>;
    CasNumber?: ConfidentialValue<string>;
    AdditionalCasNumbers?: AdditionalCasNumbers;
    FemaNumber?: any;
    AdditiveNumber?: any;
    FlNumber?: any;
    EcNumber?: any;
    OtherNumber?: any;
    RecipeQuantity: ConfidentialValue<string>;
    Functions: Functions;
    FunctionOther?: any;
    ToxicityStatus: ConfidentialValue<string>;
    ReachRegistration: ConfidentialValue<string>;
    ReachRegistrationNumber?: any;
    ClpWhetherClassification: ConfidentialValue<boolean>;
    ClpAcuteToxOral?: any;
    ClpAcuteToxDermal?: any;
    ClpAcuteToxInhalation?: any;
    ClpSkinCorrosiveIrritant?: any;
    ClpEyeDamageIrritation?: any;
    ClpRespiratorySensitisation?: any;
    ClpSkinSensitisation?: any;
    ClpMutagenGenotox?: any;
    ClpCarcinogenicity?: any;
    ClpReproductiveTox?: any;
    ClpStot?: any;
    ClpStotDescription?: any;
    ClpAspirationTox?: any;
    ToxicologicalDetails: ToxicologicalDetails;
}

export interface TobaccoCigaretteSpecific {
    CharacterisingFlavour: ConfidentialValue<boolean>;
    FilterVentilation: ConfidentialValue<number>;
    FilterDropPressureClosed: ConfidentialValue<string>;
    FilterDropPressureOpen: ConfidentialValue<string>;
}

export interface RyoPipeSpecific {
    TotalNicotineContent: ConfidentialValue<string>;
    UnionisedNicotineContent?: ConfidentialValue<string>;
}

export interface SmokelessSpecific extends RyoPipeSpecific {
    Ph: ConfidentialValue<string>;
    TotalMoisture?: ConfidentialValue<string>;
    AnalysisMethods?: ConfidentialValue<string>;
}

export interface NovelSpecific {
    DetailsDescriptionFile?: AttachmentRef;
    UseInstructionsFile?: AttachmentRef;
    RiskBenefitFile?: AttachmentRef;
    StudyFiles?: Attachments;
}

export interface Laboratories {
    Laboratory: ConfidentialValue<string>[];
}

export interface TobaccoTncoEmission {
    Tar: ConfidentialValue<string>;
    Nicotine: ConfidentialValue<string>;
    Co: ConfidentialValue<string>;
    Laboratories: Laboratories;
}

export interface TobaccoEmission extends Emission {
    Name: ConfidentialValue<string>;
}

export interface TobaccoOtherEmissions {
    Emission: TobaccoEmission[];
}

export interface TobaccoProduct extends Product {
    ProductType: ConfidentialValue<string>;
    Length?: ConfidentialValue<string>;
    Diameter?: ConfidentialValue<string>;
    Weight: ConfidentialValue<string>;
    TobaccoWeight?: ConfidentialValue<string>;
    Filter?: ConfidentialValue<boolean>;
    FilterLength?: ConfidentialValue<number>;
    TechnicalFiles?: Attachments;
    Presentations: TobaccoPresentations;
    TobaccoIngredients?: TobaccoIngredients;
    OtherIngredientsExist: ConfidentialValue<boolean>;
    OtherIngredients?: TobaccoOtherIngredients;
    TncoEmission?: TobaccoTncoEmission;
    OtherEmissionsAvailable: ConfidentialValue<boolean>;
    OtherEmissions?: TobaccoOtherEmissions;
    CigaretteSpecific?: TobaccoCigaretteSpecific;
    SmokelessSpecific?: SmokelessSpecific;
    RyoPipeSpecific?: RyoPipeSpecific;
    NovelSpecific?: NovelSpecific;
}
