export interface ConfidentialValue<T = unknown> {
    confidential: boolean;
    value: T;
}

export interface Company {
    Name: string;
    Address: string;
    Country: string;
    PhoneNumber: string;
    Email: string;
    confidential: boolean;
    submitterID?: string;
}

export type ProductIdentificationType = 'TEXT' | 'PRODUCT_ID';

export interface ProductIdentification extends ConfidentialValue<string> {
    value: string;
    confidential: boolean;
    type: ProductIdentificationType;
}

export interface CompositionProducts {
    ProductIdentification: ProductIdentification[];
}

export interface AttachmentRef {
    attachmentID: string;
}

export interface Attachments {
    Attachment: AttachmentRef[];
}

export interface ProductionSiteAddress {
    Address: string;
    Country: string;
    PhoneNumber?: string;
    Email?: string;
    confidential: boolean;
}

export interface ProductionSiteAddresses {
    ProductionSiteAddress: ProductionSiteAddress[];
}

export interface Manufacturer extends Company {
    ProductionSiteAddresses: ProductionSiteAddresses;
}

export interface Manufacturers {
    Manufacturer: Manufacturer[];
}

export interface Functions {
    Function: ConfidentialValue<string>[];
}

export interface AdditionalCasNumbers {
    CasNumber: ConfidentialValue<string>[];
}

export interface ToxicologicalDetails {
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

export interface AnnualSalesData {
    Year?: ConfidentialValue<string>;
    SalesVolume?: ConfidentialValue<string>;
}

export interface Presentation {
    NationalMarket: ConfidentialValue<string>;
    NationalComment?: ConfidentialValue<string>;
    BrandName?: ConfidentialValue<string>;
    BrandSubtypeNameExists: ConfidentialValue<boolean>;
    BrandSubtypeName?: ConfidentialValue<string>;
    LaunchDate?: ConfidentialValue<string>;
    WithdrawalIndication: ConfidentialValue<boolean>;
    WithdrawalDate?: ConfidentialValue<string>;
    ProductNumberType: ConfidentialValue<string>;
    ProductNumber?: ConfidentialValue<string>;
}

export interface Emission {
    CasNumber: ConfidentialValue<string>;
    IupacName: ConfidentialValue<string>;
    Quantity: ConfidentialValue<number>;
    Unit: ConfidentialValue<string>;
    MethodsFile: Attachments;
}

export interface Product {
    ProductID: ConfidentialValue<string>;
    PreviousProductID?: ConfidentialValue<string>;
    OtherProductsExist: ConfidentialValue<boolean>;
    OtherProducts?: CompositionProducts;
    SameCompositionProductsExist: ConfidentialValue<boolean>;
    SameCompositionProducts?: CompositionProducts;
    Manufacturers?: Manufacturers;
    MarketResearchFiles?: Attachments;
}
