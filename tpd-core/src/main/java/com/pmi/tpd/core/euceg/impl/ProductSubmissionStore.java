package com.pmi.tpd.core.euceg.impl;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Submission;
import org.eu.ceg.TobaccoProductSubmission;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.spi.IProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionRepository;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.QSubmissionEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.BaseSubmissionVisitor;
import com.pmi.tpd.euceg.api.entity.IPayloadEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionVisitor;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

/**
 * the default implementation of {@link IProductSubmissionStore} allowing manipulate {@link SubmissionEntity}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ProductSubmissionStore implements IProductSubmissionStore {

    /** */
    private final IProductSubmissionRepository repository;

    /** */
    private final IProductIdGeneratorRepository productIdGenerator;

    /**
     * Default constructor.
     *
     * @param repository
     *                           the product submission repository.
     * @param productIdGenerator
     *                           the generator of productId.
     */
    @Inject
    public ProductSubmissionStore(final IProductSubmissionRepository repository,
            final IProductIdGeneratorRepository productIdGenerator) {
        this.repository = Assert.checkNotNull(repository, "repository");
        this.productIdGenerator = Assert.checkNotNull(productIdGenerator, "productIdGenerator");
    }

    private QSubmissionEntity entity() {
        return this.repository.entity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<SubmissionEntity> findAll(@Nonnull final Pageable pageRequest) {
        return this.repository.findAll(pageRequest);
    }

    @Override
    @Nonnull
    public Stream<SubmissionEntity> stream(@Nonnull final Pageable pageRequest) {
        return this.repository.stream(pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<SubmissionEntity> findAllForProduct(@Nonnull final String productNumber,
        @Nonnull final Pageable pageRequest) {
        return this.repository.findAll(this.entity().product.productNumber.eq(productNumber), pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return this.repository.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Iterable<Long> getDeferredSubmissions(final int numberOfSubmission) {
        return repository.getDeferredSubmissions(PageUtils.newRequest(0, numberOfSubmission));
    }

    @Override
    public IPayloadEntity getSubmissionPayload(final Long id) {
        return repository.getSubmissionPayload(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull Boolean exists(@Nonnull final Long id) {
        return this.repository.existsById(Assert.checkNotNull(id, "id"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull SubmissionEntity get(@Nonnull final Long id) {
        return HibernateUtils.initialize(this.repository.getById(Assert.checkNotNull(id, "id")));
    }

    @Override
    public @Nonnull SubmissionEntity getLazy(@Nonnull final Long id) {
        return this.repository.getById(Assert.checkNotNull(id, "id"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public @Nonnull SubmissionEntity save(@Nonnull final SubmissionEntity entity) {
        Assert.checkNotNull(entity, "entity");
        if (entity.isNew()) {
            return this.create(entity);
        }
        final SubmissionEntity result = repository.save(synchronizeProductWithSubmission(entity));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public @Nonnull SubmissionEntity saveAndFlush(@Nonnull final SubmissionEntity entity) {
        Assert.checkNotNull(entity, "entity");
        if (entity.getId() == null) {
            return this.create(entity);
        }
        final SubmissionEntity result = repository.saveAndFlush(synchronizeProductWithSubmission(entity));
        return result;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    @Nonnull
    public Optional<SubmissionEntity> updateLastestSubmissionIfNotSend(@Nonnull final ProductEntity productEntity) {
        final SubmissionEntity submissionEntity = productEntity.getLastestSubmission();
        // should be valid and latest submission not send yet.
        if (submissionEntity != null && ProductStatus.VALID.equals(productEntity.getStatus())
                && SubmissionStatus.NOT_SEND.equals(submissionEntity.getSubmissionStatus())) {
            final ISubmissionVisitor<Submission> visitor = new BaseSubmissionVisitor<>() {

                @Override
                public Submission visit(final ISubmissionEntity entity) {
                    return visit(entity.getSubmission());
                }

                @Override
                public Submission visit(final EcigProductSubmission submission) {
                    return submission.withProduct((org.eu.ceg.EcigProduct) productEntity.getProduct());
                }

                @Override
                public Submission visit(final TobaccoProductSubmission submission) {
                    return submission.withProduct((org.eu.ceg.TobaccoProduct) productEntity.getProduct());
                }
            };
            return Optional.of(this.updateNotSendSubmission(submissionEntity.copy()
                    .submission(submissionEntity.accept(visitor))
                    .internalProductNumber(productEntity.getInternalProductNumber())
                    .build()));
        }
        return Optional.empty();
    }

    /**
     * @param entity
     *               a not send submission
     * @return Returns a updated submission
     */
    private SubmissionEntity updateNotSendSubmission(@Nonnull final SubmissionEntity entity) {
        Assert.checkNotNull(entity, "entity");
        Assert.state(SubmissionStatus.NOT_SEND.equals(Assert.checkNotNull(entity, "entity").getSubmissionStatus()),
            "Only NOT_SEND submission can be updated");
        final SubmissionEntity result = repository
                .save(applyForAllAttachments(false).apply(synchronizeProductWithSubmission(entity)));

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public @Nonnull SubmissionEntity create(@Nonnull final SubmissionEntity entity) {
        Assert.checkNotNull(entity, "entity");
        Assert.checkHasText(entity.getSubmitterId(), "entity.submitterId");
        String productId = entity.getProductId();
        if (Eucegs.UNDEFINED_PRODUCT_ID.equals(productId) || Strings.isNullOrEmpty(productId)) {
            productId = this.productIdGenerator.getNextProductId(entity.getSubmitterId());
        }
        final SubmissionEntity result = repository.saveAndFlush(applyForAllAttachments(false)
                .apply(synchronizeProductWithSubmission(entity.copy().productId(productId).build())));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void remove(@Nonnull final SubmissionEntity submission) {
        Assert.checkNotNull(submission, "submission");
        this.repository.delete(submission);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void remove(@Nonnull final Long id) {
        this.repository.deleteById(Assert.checkNotNull(id, "id"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Optional<TransmitReceiptEntity> findReceiptByMessageId(@Nonnull final String messageId) {
        return this.repository.findReceiptByMessageId(messageId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<TransmitReceiptEntity> getAwaitReceiptsToSend(@Nonnull final Pageable pageRequest) {
        return this.repository.getAwaitReceiptsToSend(pageRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Set<String> getPendingMessageIds() {
        return this.repository.getPendingMessageIds();
    }

    @Override
    public boolean isOwner(@Nonnull String messageId) {
        return this.repository.findReceiptByMessageId(messageId).isPresent();
    }

    @Override
    @Nonnull
    public Iterable<SubmissionEntity> getPendingOrSubmittingSubmissionsBefore(@Nonnull final DateTime fromDate) {
        return this.repository
                .findAll(entity().submissionStatus.in(SubmissionStatus.PENDING, SubmissionStatus.SUBMITTING)
                        .and(entity().lastModifiedDate.before(fromDate)));
    }

    /**
     * @return Returns a {@link Function} allowing synchronize the productId of product associated to.
     */
    @VisibleForTesting
    public static SubmissionEntity synchronizeProductWithSubmission(final SubmissionEntity entity) {

        final ISubmissionVisitor<Submission> visitor = new BaseSubmissionVisitor<>() {

            public Submission visit(final ISubmissionEntity entity) {
                return visit(entity.getSubmission());
            }

            @Override
            public Submission visit(final EcigProductSubmission submission) {
                submission.getProduct().withProductID(Eucegs.productNumber(entity.getProductId()));
                return submission;
            }

            @Override
            public Submission visit(final TobaccoProductSubmission submission) {
                submission.getProduct().withProductID(Eucegs.productNumber(entity.getProductId()));
                return submission;
            }
        };

        return entity.copy()
                .submission(entity.accept(visitor))
                .internalProductNumber(entity.getInternalProductNumber())
                .submissionStatus(SubmissionStatus.from(entity))
                .productType(ProductType.productType(entity.getSubmission()))
                .build();

    }

    @VisibleForTesting
    public static Function<SubmissionEntity, SubmissionEntity> applyForAllAttachments(final boolean sent) {
        return entity -> {
            final Set<String> uuids = Eucegs.extractAttachementID(entity.getXmlSubmission());
            entity.getAttachedAttachments().clear();
            entity.getAttachments().putAll(Maps.asMap(uuids, input -> sent));
            return entity;
        };
    }

}
