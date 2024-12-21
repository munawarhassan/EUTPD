package com.pmi.tpd.core.model.euceg;

import org.eu.ceg.AppResponse;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.ResponseStatus;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.junit.jupiter.api.Test;

import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.pmi.tpd.testing.junit5.TestCase;

public class SubmissionEntityTest extends TestCase {

    @Test
    public void submissionInError() {
        SubmissionEntity submission = SubmissionEntity.builder()
                .sendType(SendSubmissionType.IMMEDIAT)
                .productId("1")
                .build();
        // EMPTY
        assertFalse(submission.isError());
        assertEquals(SubmissionStatus.PENDING, SubmissionStatus.from(submission));

        // PENDING
        submission = submission.copy()
                .receipt(TransmitReceiptEntity.create(submission,
                    PayloadType.SUBMITER_DETAILS,
                    submission.getProductId(),
                    RandomUtil.uuid(),
                    TransmitStatus.PENDING))
                .build();
        assertFalse(submission.isError());
        assertEquals(SubmissionStatus.PENDING, SubmissionStatus.from(submission));

        // AWAITING
        submission = SubmissionEntity.builder().productId("1").build();
        submission = submission.copy()
                .receipt(TransmitReceiptEntity.create(submission,
                    PayloadType.SUBMISSION,
                    submission.getProductId(),
                    RandomUtil.uuid(),
                    TransmitStatus.AWAITING))
                .build();
        assertFalse(submission.isError());
        assertEquals(SubmissionStatus.PENDING, SubmissionStatus.from(submission));

        // REJECTED
        submission = SubmissionEntity.builder().productId("1").build().copy().build();
        submission = submission.copy()
                .receipt(TransmitReceiptEntity.create(submission,
                    PayloadType.SUBMISSION,
                    submission.getProductId(),
                    RandomUtil.uuid(),
                    TransmitStatus.REJECTED))
                .build();
        assertTrue(submission.isError());
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));

        // DELETED
        submission = SubmissionEntity.builder().productId("1").build().copy().build();
        submission = submission.copy()
                .receipt(TransmitReceiptEntity.create(submission,
                    PayloadType.SUBMISSION,
                    submission.getProductId(),
                    RandomUtil.uuid(),
                    TransmitStatus.DELETED))
                .build();
        assertTrue(submission.isError());
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));

        AppResponse response = new ErrorResponse();
        submission = SubmissionEntity.builder().productId("1").build();
        submission = submission.copy()
                .receipt(TransmitReceiptEntity
                        .create(submission,
                            PayloadType.SUBMISSION,
                            submission.getProductId(),
                            RandomUtil.uuid(),
                            TransmitStatus.PENDING)
                        .copy()
                        .transmitStatus(TransmitStatus.from(response))
                        .response(response)
                        .build())
                .build();
        assertTrue(submission.isError());
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));

        response = new TobaccoProductSubmissionResponse().withStatus(ResponseStatus.ERROR);
        submission = SubmissionEntity.builder().productId("1").build();
        submission = submission.copy()
                .receipt(TransmitReceiptEntity
                        .create(submission,
                            PayloadType.SUBMISSION,
                            submission.getProductId(),
                            RandomUtil.uuid(),
                            TransmitStatus.PENDING)
                        .copy()
                        .transmitStatus(TransmitStatus.from(response))
                        .response(response)
                        .build())
                .build();
        assertTrue(submission.isError());
        assertEquals(SubmissionStatus.ERROR, SubmissionStatus.from(submission));
    }

}
