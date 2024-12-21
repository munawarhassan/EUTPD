export interface MessageLog {
    messageId: string;
    mshRole: string;
    conversationId: string;
    messageType: string;
    messageStatus: string;
    notificationStatus: string;
    fromPartyId: string;
    toPartyId: string;
    originalSender: string;
    finalRecipient: string;
    refToMessageId: string;
    received: Date;
    deleted: Date;
    sendAttempts: number;
    sendAttemptsMax: number;
}
