import { Emission, Product } from '.';
import {
    AdditionalCasNumbers,
    AnnualSalesData,
    AttachmentRef,
    Attachments,
    CompositionProducts,
    ConfidentialValue,
    Functions,
} from './product.type';

export interface EcigAnnualSalesData extends AnnualSalesData {
    SalesMode?: AttachmentRef;
}

export interface EcigAnnualSalesDataList {
    AnnualSalesData: EcigAnnualSalesData[];
}

export interface EcigPresentation {
    PackageUnits?: ConfidentialValue<number>;
    UnitPacketPictureFile: AttachmentRef;
    AnnualSalesDataList: EcigAnnualSalesDataList;
    NationalMarket: ConfidentialValue<string>;
    NationalComment: ConfidentialValue<string>;
    BrandName?: ConfidentialValue<string>;
    BrandSubtypeNameExists: ConfidentialValue<boolean>;
    BrandSubtypeName: ConfidentialValue<string>;
    LaunchDate?: ConfidentialValue<string>;
    WithdrawalIndication: ConfidentialValue<boolean>;
    WithdrawalDate?: ConfidentialValue<string>;
    ProductNumberType: ConfidentialValue<string>;
    ProductNumber?: ConfidentialValue<string>;
}

export interface EcigPresentations {
    Presentation: EcigPresentation[];
}

export interface EcigToxicologicalDetails {
    ToxicologicalDataAvailable: ConfidentialValue<string>;
    ToxEmission: ConfidentialValue<boolean>;
    ToxCmr: ConfidentialValue<boolean>;
    ToxCardioPulmonary: ConfidentialValue<boolean>;
    ToxAddictive: ConfidentialValue<boolean>;
    ToxOther: ConfidentialValue<boolean>;
    ToxEmissionFiles: Attachments;
    ToxCmrFiles: Attachments;
    ToxCardioPulmonaryFiles: Attachments;
    ToxAddictiveFiles: Attachments;
    ToxOtherFiles: Attachments;
}

export interface EcigIngredient {
    IdentificationRefillContainerCartridge: ConfidentialValue<string>;
    Name: ConfidentialValue<string>;
    CasNumberExists: ConfidentialValue<boolean>;
    CasNumber: ConfidentialValue<string>;
    AdditionalCasNumbers: AdditionalCasNumbers;
    FemaNumber: ConfidentialValue<string>;
    AdditiveNumber: ConfidentialValue<string>;
    FlNumber: ConfidentialValue<string>;
    EcNumber: ConfidentialValue<string>;
    OtherNumber?: any;
    RecipeQuantity: ConfidentialValue<string>;
    Functions: Functions;
    FunctionOther?: any;
    ToxicityStatus: ConfidentialValue<string>;
    ReachRegistration: ConfidentialValue<string>;
    ReachRegistrationNumber: ConfidentialValue<string>;
    ClpWhetherClassification: ConfidentialValue<boolean>;
    ClpAcuteToxOral: ConfidentialValue<string>;
    ClpAcuteToxDermal: ConfidentialValue<string>;
    ClpAcuteToxInhalation: ConfidentialValue<string>;
    ClpSkinCorrosiveIrritant: ConfidentialValue<string>;
    ClpEyeDamageIrritation: ConfidentialValue<string>;
    ClpRespiratorySensitisation: ConfidentialValue<string>;
    ClpSkinSensitisation: ConfidentialValue<string>;
    ClpMutagenGenotox: ConfidentialValue<string>;
    ClpCarcinogenicity: ConfidentialValue<string>;
    ClpReproductiveTox: ConfidentialValue<string>;
    ClpStot: ConfidentialValue<string>;
    ClpStotDescription: ConfidentialValue<string>;
    ClpAspirationTox: ConfidentialValue<string>;
    ToxicologicalDetails: EcigToxicologicalDetails;
}

export interface EcigIngredients {
    Ingredient: EcigIngredient[];
}

export interface EcigEmission extends Emission {
    AdditionalProducts?: CompositionProducts;
    ProductCombination: ConfidentialValue<string>;
    Name: ConfidentialValue<string>;
    NameOther: ConfidentialValue<string>;
}

export interface ECigEmissions {
    Emission: Emission[];
}

export interface EcigDesign {
    Description: ConfidentialValue<string>;
    IdentificationEcigDevice?: ConfidentialValue<string>;
    LiquidVolumeCapacity?: ConfidentialValue<string>;
    NicotineConcentration?: ConfidentialValue<string>;
    BatteryType?: ConfidentialValue<string>;
    BatteryCapacity?: ConfidentialValue<string>;
    VoltageWattageAdjustable?: ConfidentialValue<string>;
    Voltage?: ConfidentialValue<string>;
    VoltageLowerRange?: ConfidentialValue<string>;
    VoltageUpperRange?: ConfidentialValue<string>;
    Wattage?: ConfidentialValue<string>;
    WattageLowerRange?: ConfidentialValue<string>;
    WattageUpperRange?: ConfidentialValue<string>;
    AirflowAdjustable?: ConfidentialValue<boolean>;
    WickChangeable?: ConfidentialValue<boolean>;
    Microprocessor?: ConfidentialValue<boolean>;
    CoilComposition?: ConfidentialValue<string>;
    NicotineDoseUptakeFile?: AttachmentRef;
    ProductionFile: AttachmentRef;
    ProductionConformity: ConfidentialValue<boolean>;
    QualitySafety: ConfidentialValue<boolean>;
    OpeningRefillFile?: AttachmentRef;
    ChildTamperProof: ConfidentialValue<boolean>;
    HighPurity: ConfidentialValue<boolean>;
    NonRisk: ConfidentialValue<boolean>;
    ConsistentDosing: ConfidentialValue<boolean>;
    ConsistentDosingMethodsFile?: AttachmentRef;
    LeafletFile?: AttachmentRef;
    CoilResistance?: ConfidentialValue<string>;
}

export interface EcigProduct extends Product {
    ProductType?: ConfidentialValue<string>;
    Weight?: ConfidentialValue<string>;
    Volume?: ConfidentialValue<string>;
    ClpClassification?: ConfidentialValue<string>;
    StudySummaryFiles?: Attachments;
    Presentations: EcigPresentations;
    Ingredients?: EcigIngredients;
    Emissions?: ECigEmissions;
    Design: EcigDesign;
}
