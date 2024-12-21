package com.pmi.tpd.core.model.euceg;

import java.util.Arrays;

import org.eu.ceg.ResponseStatus;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Iterables;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.testing.junit5.TestCase;

public class SubmissionStatusTest extends TestCase {

    @Test
    public void submissionIsPending() {
        final SubmissionEntity submission = createSubmission(TransmitStatus.AWAITING, TransmitStatus.PENDING);
        assertEquals(SubmissionStatus.PENDING, SubmissionStatus.from(submission));

    }

    @Test
    public void submissionIsSubmitting() {
        final SubmissionEntity submission = createSubmission(TransmitStatus.PENDING, TransmitStatus.PENDING);
        assertEquals(SubmissionStatus.SUBMITTING, SubmissionStatus.from(submission));

    }

    @Test
    public void submissionIsError() {
        SubmissionEntity submission = createSubmission(TransmitStatus.DELETED, TransmitStatus.PENDING);
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));

        submission = createSubmission(TransmitStatus.REJECTED, TransmitStatus.PENDING);
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));

        submission = createSubmission(TransmitStatus.RECEIVED, TransmitStatus.RECEIVED);
        TransmitReceiptEntity receipt = Iterables.getFirst(submission.getReceiptByType(PayloadType.SUBMISSION), null);
        submission = submission.copy()
                .receipt(receipt.copy()
                        .response(new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.ERROR))
                        .build())
                .build();
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));

        submission = createSubmission(TransmitStatus.RECEIVED, TransmitStatus.RECEIVED);
        receipt = Iterables.getFirst(submission.getReceiptByType(PayloadType.ATTACHMENT), null);

        submission = submission.copy()
                .receipt(receipt.copy()
                        .response(new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.ERROR))
                        .build())
                .build();
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));
    }

    @Test
    public void submissionIsSubmitted() {

        SubmissionEntity submission = createSubmission(TransmitStatus.RECEIVED, TransmitStatus.RECEIVED);
        final TransmitReceiptEntity receipt = Iterables.getFirst(submission.getReceiptByType(PayloadType.SUBMISSION),
            null);
        submission = submission.copy()
                .receipt(receipt.copy()
                        .response(new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.SUCCESS))
                        .build())
                .build();
        assertEquals(SubmissionStatus.SUBMITTED, SubmissionStatus.from(submission));
    }

    @Test
    public void submissionIsCancelled() {

        SubmissionEntity submission = createSubmission(TransmitStatus.RECEIVED, TransmitStatus.CANCELLED);
        TransmitReceiptEntity receipt = Iterables.getFirst(submission.getReceiptByType(PayloadType.SUBMISSION), null);

        submission = submission.copy()
                .receipt(receipt.copy()
                        .response(new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.SUCCESS))
                        .build())
                .build();
        assertEquals(SubmissionStatus.SUBMITTED, SubmissionStatus.from(submission));

        submission = createSubmission(TransmitStatus.CANCELLED, TransmitStatus.RECEIVED);
        receipt = Iterables.getFirst(submission.getReceiptByType(PayloadType.SUBMISSION), null);

        submission = submission.copy()
                .receipt(receipt.copy()
                        .response(new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.SUCCESS))
                        .build())
                .build();
        assertEquals(SubmissionStatus.CANCELLED, SubmissionStatus.from(submission));
    }

    private SubmissionEntity createSubmission(final TransmitStatus submissionStatus,
        final TransmitStatus attachmentStatus) {
        final SubmissionEntity submission = SubmissionEntity.builder().build();
        submission.getReceipts()
                .addAll(Arrays.asList(TransmitReceiptEntity
                        .create(submission, PayloadType.SUBMISSION, "submission", RandomUtil.uuid(), submissionStatus),
                    TransmitReceiptEntity.create(submission,
                        PayloadType.ATTACHMENT,
                        "attachment",
                        RandomUtil.uuid(),
                        attachmentStatus)));
        return submission;
    }

}
