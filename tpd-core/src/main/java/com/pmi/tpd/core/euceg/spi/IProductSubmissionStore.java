package com.pmi.tpd.core.euceg.spi;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.IPayloadEntity;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.backend.core.spi.IPendingMessageProvider;

/**
 * Allow to manipulate SubmissionEntity.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IProductSubmissionStore extends IPendingMessageProvider {

    /**
     * @param pageRequest
     * @return
     */
    @Nonnull
    Page<SubmissionEntity> findAll(@Nonnull Pageable pageRequest);

    /**
     * @param pageRequest
     * @return
     */
    @Nonnull
    Stream<SubmissionEntity> stream(@Nonnull Pageable pageRequest);

    /**
     * @param productNumber
     * @param pageRequest
     * @return
     * @since 2.4
     */
    @Nonnull
    Page<SubmissionEntity> findAllForProduct(@Nonnull String productNumber, @Nonnull final Pageable pageRequest);

    /**
     * @return
     */
    long count();

    /**
     * @param numberOfSubmission
     * @return
     */
    @Nonnull
    Iterable<Long> getDeferredSubmissions(int numberOfSubmission);

    /**
     * @param submissionId
     * @return
     */
    @Nonnull
    java.lang.Boolean exists(@Nonnull Long id);

    /**
     * @param submissionId
     * @return
     */
    @Nonnull
    SubmissionEntity get(@Nonnull Long id);

    /**
     * @param id
     * @return
     */
    @Nonnull
    SubmissionEntity getLazy(@Nonnull final Long id);

    /**
     * @param submissionId
     * @return
     */
    @Nonnull
    IPayloadEntity getSubmissionPayload(@Nonnull Long id);

    /**
     * <p>
     * <strong>Note</strong>: Only submission NOT_SEND can be updated, preferred method
     * {@link #updateNotSendSubmission(SubmissionEntity)}
     * </p>
     *
     * @param submission
     */
    @Nonnull
    SubmissionEntity save(@Nonnull SubmissionEntity submission);

    /**
     * <p>
     * <strong>Note</strong>: Only submission NOT_SEND can be updated, preferred method
     * {@link #updateNotSendSubmission(SubmissionEntity)}
     * </p>
     *
     * @param submissionEntity
     * @return
     */
    @Nonnull
    SubmissionEntity saveAndFlush(@Nonnull SubmissionEntity entity);

    /**
     * @param submission
     */
    void remove(@Nonnull SubmissionEntity submission);

    /**
     * @param productId
     */
    void remove(@Nonnull Long id);

    /**
     * @param submission
     * @return
     */
    @Nonnull
    SubmissionEntity create(@Nonnull SubmissionEntity submission);

    /**
     * @return
     */
    @Nonnull
    Page<TransmitReceiptEntity> getAwaitReceiptsToSend(@Nonnull Pageable pageRequest);

    /**
     * @param messageId
     * @return
     */
    @Nonnull
    Optional<TransmitReceiptEntity> findReceiptByMessageId(@Nonnull final String messageId);

    /**
     * Update only {@link SubmissionStatus#NOT_SEND NOT_SEND} submission.
     *
     * @param entity
     *               the entity to update.
     * @return Returns the updated submission.
     * @since 2.5
     */
    @Nonnull
    Optional<SubmissionEntity> updateLastestSubmissionIfNotSend(@Nonnull final ProductEntity entity);

    /**
     * @param fromDate
     * @return
     */
    @Nonnull
    Iterable<SubmissionEntity> getPendingOrSubmittingSubmissionsBefore(@Nonnull DateTime fromDate);

}
