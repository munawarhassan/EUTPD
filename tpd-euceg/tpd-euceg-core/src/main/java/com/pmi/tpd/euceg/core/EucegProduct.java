package com.pmi.tpd.euceg.core;

import org.eu.ceg.Product;
import org.eu.ceg.SubmissionTypeEnum;
import org.joda.time.DateTime;

import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@ToString
public class EucegProduct {

    /** */
    private Product product;

    /** */
    private String productNumber;

    /** */
    private String internalProductNumber;

    /** */
    private String submitterId;

    /** */
    private SubmissionTypeEnum preferredSubmissionType;

    /** */
    private String generalComment;

    /** */
    private String previousProductNumber;

    /** */
    private ProductPirStatus pirStatus;

    /** */
    private ProductStatus status;

    /** */
    private DateTime lastModifiedDate;

    /** */
    private String lastModifiedBy;

    /** */
    private DateTime createdDate;

    /** */
    private String createdBy;

}
