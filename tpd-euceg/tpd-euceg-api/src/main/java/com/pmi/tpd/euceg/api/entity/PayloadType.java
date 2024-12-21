package com.pmi.tpd.euceg.api.entity;

import org.eu.ceg.AppResponse;
import org.eu.ceg.AttachmentResponse;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.SubmissionResponse;
import org.eu.ceg.SubmitterDetailsResponse;

/**
 * @author Christophe Friederich
 * @version 1.0
 */
public enum PayloadType {

    /** */
    SUBMITER_DETAILS,
    /** */
    ATTACHMENT,
    /** */
    SUBMISSION,
    /** */
    ERROR_RESPONSE;

    public static PayloadType fromPayload(final AppResponse response) {
        if (response instanceof SubmitterDetailsResponse) {
            return SUBMITER_DETAILS;
        } else if (response instanceof SubmissionResponse) {
            return SUBMISSION;
        } else if (response instanceof AttachmentResponse) {
            return ATTACHMENT;
        } else if (response instanceof ErrorResponse) {
            return ERROR_RESPONSE;
        }
        return null;
    }
}
