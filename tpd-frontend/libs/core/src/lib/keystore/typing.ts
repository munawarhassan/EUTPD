export interface AliasResponse {
    alias: string;
}

export interface KeystoreRequest {
    alias: string;
    type: 'KeyPair' | 'TrustedCertificate';
    algorithm: string;
    keySize: number;
    expiredDate: Date;
    lastModified: Date;
    valid: boolean;
    expired: boolean;
}
