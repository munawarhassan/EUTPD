/* eslint-disable max-classes-per-file */

export enum ConnectionType {
    Ws,
    Jms,
}

export interface JmsOption {
    url: string;
    receiveTimeout: number;
    concurrency: string;
    username: string;
    password: string;
}

export interface WsOption {
    authorizationType: string;
    pendingInterval: number;
}

export interface DomibusSetting {
    enable: boolean;
    url: string;
    connectionType: ConnectionType;
    username: string;
    password: number;
    tlsInsecure: boolean;
    action: string;
    service: string;
    serviceType: string;
    originalSender: string;
    finalRecipient: string;
    partyIdType: string;
    fromPartyId: string;
    toPartyId: string;
    keyPairAlias: string;
    trustedCertificateAlias: string;
    jmsOptions: JmsOption;
    wsOptions: WsOption;
}
