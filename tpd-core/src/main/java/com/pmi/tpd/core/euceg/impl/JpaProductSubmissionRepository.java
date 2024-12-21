package com.pmi.tpd.core.euceg.impl;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Sets;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionRepository;
import com.pmi.tpd.core.model.euceg.PayloadEntity;
import com.pmi.tpd.core.model.euceg.QPayloadEntity;
import com.pmi.tpd.core.model.euceg.QSubmissionEntity;
import com.pmi.tpd.core.model.euceg.QTransmitReceiptEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;
import com.pmi.tpd.euceg.api.entity.IPayloadEntity;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Repository
public class JpaProductSubmissionRepository extends DefaultJpaRepository<SubmissionEntity, Long>
        implements IProductSubmissionRepository {

    /**
     * @param entityManager
     *                      the JPA entity manager.
     */
    public JpaProductSubmissionRepository(final EntityManager entityManager) {
        super(SubmissionEntity.class, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public QSubmissionEntity entity() {
        return QSubmissionEntity.submissionEntity;
    }

    @Override
    @Nonnull
    public IPayloadEntity getSubmissionPayload(@Nonnull final Long id) {
        final JPQLQuery<PayloadEntity> query = from(QPayloadEntity.payloadEntity).innerJoin(entity())
                .on(QPayloadEntity.payloadEntity.id.eq(entity().payloadSubmission.id))
                .where(entity().id.eq(id));
        final IPayloadEntity payload = query.fetchOne();
        if (payload == null) {
            throw new EntityNotFoundException("payload not found");
        }
        return payload;
    }

    @Override
    @Nonnull
    public Page<Long> getDeferredSubmissions(final Pageable pageable) {
        return toPage(
            from().select(entity().id)
                    .where(entity().sendType.eq(SendSubmissionType.DEFERRED).and(entity().receipts.isEmpty()))
                    .orderBy(entity().createdDate.asc()),
            pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Optional<TransmitReceiptEntity> findReceiptByMessageId(@Nonnull final String messageId) {
        final JPQLQuery<TransmitReceiptEntity> query = from(QTransmitReceiptEntity.transmitReceiptEntity)
                .select(QTransmitReceiptEntity.transmitReceiptEntity)
                .where(QTransmitReceiptEntity.transmitReceiptEntity.messageId.eq(messageId));
        return Optional.ofNullable(query.fetchOne());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Set<String> getPendingMessageIds() {
        return Sets.newHashSet(from(QTransmitReceiptEntity.transmitReceiptEntity)
                .select(QTransmitReceiptEntity.transmitReceiptEntity.messageId)
                .where(QTransmitReceiptEntity.transmitReceiptEntity.transmitStatus.eq(TransmitStatus.PENDING))
                .fetch());
    }

    @Override
    @Nonnull
    public Page<TransmitReceiptEntity> getAwaitReceiptsToSend(@Nonnull final Pageable pageRequest) {
        return toPage(from(QTransmitReceiptEntity.transmitReceiptEntity).where(
            QTransmitReceiptEntity.transmitReceiptEntity.transmitStatus.eq(TransmitStatus.AWAITING)), pageRequest);
    }

}
