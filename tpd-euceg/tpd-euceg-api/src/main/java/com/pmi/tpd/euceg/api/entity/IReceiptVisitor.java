package com.pmi.tpd.euceg.api.entity;

/**
 * @author christophe friederich
 * @since 3.0
 * @param <T>
 */
public interface IReceiptVisitor<T extends ITransmitReceiptEntity, R> {

    default R visit(final T receipt) {
        switch (receipt.getType()) {
            case SUBMITER_DETAILS:
                return visitSubmitterDetail(receipt);
            case ATTACHMENT:
                return visitAttachment(receipt);
            case SUBMISSION:
                return visitSubmission(receipt);
            default:
                throw new IllegalArgumentException();
        }
    }

    R visitSubmitterDetail(final T receiptEntity);

    R visitAttachment(final T receiptEntity);

    R visitSubmission(final T receiptEntity);
}
