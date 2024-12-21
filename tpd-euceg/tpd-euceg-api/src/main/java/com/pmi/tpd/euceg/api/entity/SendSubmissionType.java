package com.pmi.tpd.euceg.api.entity;

/**
 * This enumeration represents the possible type of send for a submission.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public enum SendSubmissionType {
    /**
     * indicates the submission is sent manually.
     */
    MANUAL,
    /**
     * indicates the submission have to send at once after creation.
     */
    IMMEDIAT,
    /**
     * indicates the submission will be send automatically later by scheduling.
     */
    DEFERRED;

}
