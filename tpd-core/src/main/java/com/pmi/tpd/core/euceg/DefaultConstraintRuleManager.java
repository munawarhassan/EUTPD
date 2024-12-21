package com.pmi.tpd.core.euceg;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.SubmissionTypeEnum;

import com.google.common.base.Strings;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

public class DefaultConstraintRuleManager implements IEucegConstraintRuleManager {

    /** */
    private final I18nService i18nService;

    /** */
    private final IAttachmentStore attachmentStore;

    public DefaultConstraintRuleManager(@Nonnull final IAttachmentStore attachmentStore,
            @Nonnull final I18nService i18nService) {
        this.i18nService = Assert.checkNotNull(i18nService, "i18nService");
        this.attachmentStore = Assert.checkNotNull(attachmentStore, "attachmentStore");
    }

    public void checkAttachmentIsSending(@Nonnull final ISubmissionEntity submission,
        @Nonnull final SubmitterEntity submitter) throws AttachmentIsSendingException {
        final Set<String> uuids = submission.getAttachments().keySet();

        for (final String uuid : uuids) {
            final IAttachmentEntity attachment = this.attachmentStore.get(uuid);
            if (attachment.isSending(submitter)) {
                throw new AttachmentIsSendingException(
                        i18nService.createKeyedMessage("app.service.euceg.submission.attachment.issending",
                            attachment.getFilename()));
            }
        }
    }

    public void checkPreviousIDExistsOnModificationNew(@Nullable final String previousProductId,
        @Nonnull final SubmissionTypeEnum submissionType) {
        if (SubmissionTypeEnum.MODIFICATION_NEW.equals(submissionType)) {
            if (Strings.isNullOrEmpty(previousProductId)) {
                throw new EucegException(
                        i18nService.createKeyedMessage("app.service.euceg.submission.send.previous-tpid.required",
                            submissionType));
            }
        }
    }

    public void checkNewProductSubmissionIsPossible(@Nonnull final IProductEntity productEntity,
        @Nullable final SubmissionTypeEnum submissionType) {
        final ISubmissionEntity latest = productEntity.getLastestSubmission();
        if (latest != null) {
            // check doesn't already exist a submission on new product other than has error
            // or cancelled.
            if (SubmissionTypeEnum.NEW.equals(submissionType)
                    && !(SubmissionStatus.ERROR.equals(latest.getSubmissionStatus())
                            || SubmissionStatus.CANCELLED.equals(latest.getSubmissionStatus()))) {
                throw new EucegException(
                        i18nService.createKeyedMessage("app.service.euceg.submission.send.newsubmissionnotaccepted",
                            productEntity.getProductNumber()));
            }
        }
    }
}
