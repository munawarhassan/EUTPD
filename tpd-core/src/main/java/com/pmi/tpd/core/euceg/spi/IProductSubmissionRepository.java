package com.pmi.tpd.core.euceg.spi;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.euceg.QSubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.database.jpa.IDslAccessor;
import com.pmi.tpd.euceg.api.entity.IPayloadEntity;

/**
 * <p>
 * IProductSubmissionRepository interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IProductSubmissionRepository extends IDslAccessor<SubmissionEntity, Long> {

    /**
     *
     */
    @Override
    @Nonnull
    QSubmissionEntity entity();

    /**
     * @param submissionId
     * @return
     */
    @Nonnull
    IPayloadEntity getSubmissionPayload(@Nonnull Long id);

    /**
     * @param pageable
     * @return
     */
    @Nonnull
    Page<Long> getDeferredSubmissions(final Pageable pageable);

    /**
     * @param messageId
     * @return
     */
    @Nonnull
    Optional<TransmitReceiptEntity> findReceiptByMessageId(@Nonnull String messageId);

    /**
     * Gets all pending messageIds independently of submitters.
     *
     * @return Return a new list of {@link String} representing a messageId.
     */
    @Nonnull
    Set<String> getPendingMessageIds();

    /**
     * @return
     */
    @Nonnull
    Page<TransmitReceiptEntity> getAwaitReceiptsToSend(@Nonnull Pageable pageRequest);

}
