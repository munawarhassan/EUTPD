package com.pmi.tpd.core.euceg;

import org.eu.ceg.SubmissionTypeEnum;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class represents the request allowing create a submission from a product.
 *
 * @see com.pmi.tpd.core.euceg.ISubmissionService#createSubmission(SubmissionSendRequest)
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonDeserialize()
@JsonSerialize
public class SubmissionSendRequest {

    /** */
    private String productNumber;

    /** */
    private SubmissionTypeEnum submissionType;

    /** */
    private SendSubmissionType sendType;

}
