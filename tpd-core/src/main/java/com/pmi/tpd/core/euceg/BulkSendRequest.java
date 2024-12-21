package com.pmi.tpd.core.euceg;

import org.eu.ceg.SubmissionTypeEnum;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * This class represents the request allowing create a batch of submission.
 *
 * @see com.pmi.tpd.core.euceg.ISubmissionService#bulkSendSubmissions(java.util.List)
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonDeserialize()
@JsonSerialize
public class BulkSendRequest {

    /** */
    private String productNumber;

    /** */
    private SubmissionTypeEnum submissionType;

}
