package com.pmi.tpd.core.euceg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.SubmissionTypeEnum;

import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;

public interface IEucegConstraintRuleManager {

    void checkAttachmentIsSending(@Nonnull final ISubmissionEntity submission,
        @Nonnull final SubmitterEntity submitter);

    void checkPreviousIDExistsOnModificationNew(@Nullable String previousProductId,
        @Nonnull SubmissionTypeEnum submissionType);

    void checkNewProductSubmissionIsPossible(@Nonnull IProductEntity productEntity,
        @Nullable SubmissionTypeEnum submissionType);
}
