package com.pmi.tpd.euceg.api.entity;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Corresponding of submission state.
 *
 * @author Christophe Friederich
 * @since 1.2
 */
public enum SubmissionStatus {

    /**
     * Indicates the submission has not yet been sent
     */
    NOT_SEND,
    /** indicates the submission awaits */
    PENDING,
    /** indicates the submission is sending. */
    SUBMITTING,
    /** indicates the submission is success and all dependents elements MUST be success. */
    SUBMITTED,
    /** indicates the submission failed. */
    ERROR,
    /**
     * indicate the submission has cancelled before sent.
     */
    CANCELLED;

    /**
     * Gets the indicating whether the status can be cancelled.
     *
     * @return Returns {@code true} whether the status can be cancelled, otherwise {@code false}.
     */
    public boolean cancelable() {
        return PENDING.equals(this) || NOT_SEND.equals(this) || SUBMITTING.equals(this);
    }

    /**
     * Gets the calculate status depending of submission and receipt states.
     *
     * @param submission
     *                   a submission
     * @return Return the calculate submission status.
     */
    public static SubmissionStatus from(@Nonnull final ISubmissionEntity submission) {
        checkNotNull(submission, "submission");
        if (submission.isError()) {
            return ERROR;
        }
        // a cancelled submission can not be changed
        if (SubmissionStatus.CANCELLED.equals(submission.getSubmissionStatus())) {
            return submission.getSubmissionStatus();
        }
        // get receipt associated to submission (normally only one)
        final Optional<? extends ITransmitReceiptEntity> receipt = submission.getReceiptByType(PayloadType.SUBMISSION)
                .stream()
                .findFirst();
        if (receipt.isEmpty()) {
            final SendSubmissionType sendType = submission.getSendType();
            if (SendSubmissionType.DEFERRED.equals(sendType) || SendSubmissionType.MANUAL.equals(sendType)) {
                return SubmissionStatus.NOT_SEND;
            }
            return SubmissionStatus.PENDING;
        }
        switch (receipt.get().getTransmitStatus()) {
            case AWAITING:
                return PENDING;
            case PENDING:
                return SUBMITTING;
            case DELETED:
            case REJECTED:
                return ERROR;
            case RECEIVED:
                return SUBMITTED;
            case CANCELLED:
                return CANCELLED;
            default:
                break;
        }
        return submission.getSubmissionStatus();
    }

    /**
     * Gets the indicating whether the submission is exportable. A submission is exportable if the status is final as
     * {@code ERROR}, {@code SUBMITTED} and {@code CANCELLED}
     *
     * @return Returns {@code true} whether the submission is exportable, otherwise {@code false}.
     */
    public boolean exportable() {
        return this.equals(ERROR) || this.equals(SUBMITTED) || this.equals(CANCELLED);
    }
}
