package com.pmi.tpd.euceg.api.entity;

import javax.annotation.Nonnull;

import org.eu.ceg.AppResponse;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;

public interface ITransmitReceiptEntity extends IInitializable, IIdentityEntity<Long>, IAuditEntity {

    <T extends ITransmitReceiptEntity, R> R accept(IReceiptVisitor<T, R> visitor);

    /**
     * @return
     */
    PayloadType getType();

    /**
     * @return
     */
    String getName();

    /**
     * @return
     */
    PayloadType getResponseType();

    /**
     * @return
     */
    boolean isError();

    /**
     * @return
     */
    @Nonnull
    ISubmissionEntity getSubmission();

    /**
     * @return
     */
    String getMessageId();

    /**
     * @return
     */
    String getResponseMessageId();

    /**
     * @return
     */
    TransmitStatus getTransmitStatus();

    /**
     * @return
     */
    String getXmlResponse();

    /**
     * @return
     */
    AppResponse getResponse();

}