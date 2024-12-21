package com.pmi.tpd.euceg.api.entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.eu.ceg.Submission;
import org.eu.ceg.SubmissionTypeEnum;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.euceg.api.ProductType;

public interface ISubmissionEntity extends IInitializable, IIdentityEntity<Long>, IAuditEntity {

    <T> T accept(@Nonnull final ISubmissionVisitor<T> visitor);

    /**
     * @return
     */
    String getProductId();

    /**
     * @return
     */
    IProductEntity getProduct();

    /**
     * @return
     */
    ProductType getProductType();

    /**
     * @return
     */
    SendSubmissionType getSendType();

    /**
     * @return
     */
    boolean isError();

    /**
     * @return
     */
    SubmissionStatus getSubmissionStatus();

    /**
     * @return
     */
    SubmissionTypeEnum getSubmissionType();

    /**
     * @return
     */
    String getSubmitterId();

    /**
     * @return
     */
    String getInternalProductNumber();

    /**
     * @return
     */
    String getXmlSubmission();

    /**
     * @return
     */
    @CheckForNull
    Submission getSubmission();

    /**
     * @return
     */
    Map<String, Boolean> getAttachments();

    /**
     * @return Returns a {@link Set} of {@link String} representing attached attachments of submission.
     */
    Set<String> getAttachedAttachments();

    /**
     * @return
     */
    @Nonnull
    List<? extends ITransmitReceiptEntity> getReceipts();

    /**
     * @param type
     * @return
     */
    List<? extends ITransmitReceiptEntity> getReceiptByType(@Nonnull PayloadType type);

    /**
     * @return
     */
    float getProgress();

    /**
     * @return Returns {@code true} if the submission is latest for the associated product.
     */
    boolean isLatest();

    /**
     * @return Returns {@code true} if the submission is latest submitted for the associated product.
     * @see SubmissionStatus
     */
    boolean isLatestSubmitted();

    /**
     * @return Returns the PIR status associated to this product.
     */
    ProductPirStatus getPirStatus();

    /**
     * @return
     */
    String getSentBy();

}
