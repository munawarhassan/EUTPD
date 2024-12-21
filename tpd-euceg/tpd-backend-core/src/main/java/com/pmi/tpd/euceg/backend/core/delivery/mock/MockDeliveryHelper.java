package com.pmi.tpd.euceg.backend.core.delivery.mock;

import org.eu.ceg.AbstractAppResponse;
import org.eu.ceg.AppResponse;
import org.eu.ceg.Attachment;
import org.eu.ceg.AttachmentResponse;
import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.EcigProductSubmissionResponse;
import org.eu.ceg.Error;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.ResponseStatus;
import org.eu.ceg.SubmitterDetails;
import org.eu.ceg.SubmitterDetailsResponse;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;
import org.eu.ceg.TobaccoProductSubmissionResponse;
import org.joda.time.DateTime;

import com.pmi.tpd.euceg.api.entity.TransmitStatus;

class MockDeliveryHelper {

    public static AppResponse responseError(final String messageId) {
        return new ErrorResponse().withCode("XFKH-1258")
                .withMessage("error message")
                .withUuid(messageId)
                .withDate(DateTime.now());
    }

    public static AppResponse okAttachmentResponse(final Attachment payload, final String messageId) {
        return new AttachmentResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.SUCCESS)
                .withUuid(messageId)
                .withAttachmentID(payload.getAttachmentID());
    }

    public static AppResponse rejectedAttachmentResponse(final Attachment payload, final String messageId) {
        return new AttachmentResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.ERROR)
                .withError(new Error().withCode("ERR-RULES-002-002")
                        .withMessage(
                            "The value 'CREATE' of the 'action' attribute is not correct because the attachment ID already exists."))
                .withUuid(messageId)
                .withAttachmentID(payload.getAttachmentID());
    }

    public static AppResponse okSubmitterDetailsResponse(final SubmitterDetails payload, final String messageId) {
        return new SubmitterDetailsResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.SUCCESS)
                .withUuid(messageId)
                .withSubmitterID(payload.getSubmitterID());
    }

    public static TobaccoProductSubmissionResponse okTobaccoProductSubmissionResponse(
        final TobaccoProductSubmission payload,
        final String messageId) {
        return new TobaccoProductSubmissionResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.SUCCESS)
                .withUuid(messageId)
                .withProductID(payload.getProduct() != null ? payload.getProduct().getProductID().getValue() : null)
                .withSubmissionType(
                    payload.getSubmissionType() != null ? payload.getSubmissionType().getValue() : null);
    }

    public static TobaccoProductSubmissionResponse errorTobaccoProductSubmissionResponse(
        final TobaccoProductSubmission payload,
        final String messageId) {
        return new TobaccoProductSubmissionResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.ERROR)
                .withErrors(createErrors())
                .withUuid(messageId)
                .withProductID(payload.getProduct() != null ? payload.getProduct().getProductID().getValue() : null)
                .withSubmissionType(
                    payload.getSubmissionType() != null ? payload.getSubmissionType().getValue() : null);
    }

    public static EcigProductSubmissionResponse okEcigProductSubmissionResponse(final EcigProductSubmission payload,
        final String messageId) {
        return new EcigProductSubmissionResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.SUCCESS)
                .withUuid(messageId)
                .withProductID(payload.getProduct() != null ? payload.getProduct().getProductID().getValue() : null)
                .withSubmissionType(
                    payload.getSubmissionType() != null ? payload.getSubmissionType().getValue() : null);
    }

    public static EcigProductSubmissionResponse errorEcigProductSubmissionResponse(final EcigProductSubmission payload,
        final String messageId) {
        return new EcigProductSubmissionResponse().withChecksum("")
                .withDate(DateTime.now())
                .withStatus(ResponseStatus.ERROR)
                .withErrors(createErrors())
                .withUuid(messageId)
                .withProductID(payload.getProduct() != null ? payload.getProduct().getProductID().getValue() : null)
                .withSubmissionType(
                    payload.getSubmissionType() != null ? payload.getSubmissionType().getValue() : null);
    }

    public static AbstractAppResponse.Errors createErrors() {
        return new AbstractAppResponse.Errors()
                .withError(new org.eu.ceg.Error().withCode("XFKH-1258").withMessage("error message"));

    }

    public static String getProductNumber(final TobaccoProduct product) {
        return product.getPresentations()
                .getPresentation()
                .stream()
                .map(p -> p.getProductNumber().getValue())
                .findFirst()
                .orElse(null);
    }

    public static String getProductNumber(final EcigProduct product) {
        return product.getPresentations()
                .getPresentation()
                .stream()
                .map(p -> p.getProductNumber().getValue())
                .findFirst()
                .orElse(null);
    }

    public static PayloadStatus extractStatus(final String productNumber) {
        String status = "received";
        boolean isError = false;
        if (productNumber != null) {
            final String[] ar = productNumber.split("\\.");
            if (ar.length > 1) {
                status = ar[1];
            }
            if (ar.length > 2) {
                if ("error".equals(ar[2])) {
                    isError = true;
                }
            }
        }
        return new PayloadStatus(TransmitStatus.from(status), isError);
    }

    public static final class PayloadStatus {

        /** */
        private final TransmitStatus status;

        /** */
        private final boolean isError;

        /**
         * @param status
         *                the transmit status
         * @param isError
         *                indicate if has error
         */
        private PayloadStatus(TransmitStatus status, final boolean isError) {
            super();
            if (status == null) {
                status = TransmitStatus.RECEIVED;
            }
            this.status = status;

            this.isError = isError;
        }

        public TransmitStatus getStatus() {
            return status;
        }

        public boolean isError() {
            return isError;
        }
    }
}
