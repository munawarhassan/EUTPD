package com.pmi.tpd.core.euceg.impl;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBException;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Product;
import org.eu.ceg.Submission;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.spi.IProductRepository;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.model.euceg.ProductDifference;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.ProductRevision;
import com.pmi.tpd.core.model.euceg.QProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.BaseProductVisitor;
import com.pmi.tpd.euceg.api.entity.BaseSubmissionVisitor;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.core.ValidationHelper;
import com.pmi.tpd.euceg.core.support.EucegXmlDiff;
import com.pmi.tpd.euceg.core.util.validation.ValidationResult;

@Singleton
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class ProductStore implements IProductStore {

    /** */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductStore.class);

    /** */
    private final IProductRepository repository;

    @Inject
    public ProductStore(final IProductRepository repository) {
        this.repository = Assert.checkNotNull(repository, "repository");
    }

    @SuppressWarnings("unused")
    private QProductEntity entity() {
        return this.repository.entity();
    }

    /**
     * @param entity
     * @return
     */
    @Override
    public ProductEntity detach(final ProductEntity entity) {
        return this.repository.detach(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull Page<ProductEntity> findAll(final Pageable pageRequest) {
        return this.repository.findAll(pageRequest);
    }

    @Override
    public long count() {
        return this.repository.count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProductEntity> findAllNewProduct(@Nonnull final ProductType productType) {
        return HibernateUtils
                .initializeList(this.repository.findAllNewProduct(Assert.checkNotNull(productType, "productType")));
    }

    @Nonnull
    @Override
    public Page<ProductEntity> findAllValidProduct(@Nonnull final ProductType productType,
        @Nonnull final Pageable pageRequest) {
        return repository.findAllValidProduct(productType, pageRequest);
    }

    @Override
    @Nonnull
    public Page<ProductEntity> findAllUseAttachment(final @Nonnull Pageable pageable, final @Nonnull String uuid) {
        return repository.findAllUseAttachment(pageable, uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(@Nonnull final String productNumber) {
        return this.repository.existsById(Assert.checkHasText(productNumber, "productNumber"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull ProductEntity get(@Nonnull final String productNumber) {
        return HibernateUtils.initialize(this.repository.getById(Assert.checkHasText(productNumber, "productNumber")));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductEntity find(@Nonnull final String productNumber) {
        return HibernateUtils.initialize(this.repository.findById(Assert.checkHasText(productNumber, "productNumber")))
                .orElse(null);
    }

    @Override
    public Page<ProductRevision> findRevisions(@Nonnull final String productNumber, final Pageable pageRequest) {
        final Page<ProductRevision> revisions = this.repository.findRevisions(productNumber, pageRequest)
                .map(ProductRevision::fromRevision);
        return revisions;
    }

    @Override
    public ProductDifference compareRevisions(@Nonnull final String productNumber,
        @Nullable Integer revisedRevision,
        @Nonnull final Integer originalRevision) throws IOException {

        if (revisedRevision == null) {
            final ProductRevision current = this.getCurrentRevision(productNumber);
            revisedRevision = current.getId();
        }

        final ProductEntity originalProduct = this.repository.findRevision(productNumber, originalRevision)
                .orElseThrow()
                .getEntity();
        final ProductEntity revisedProduct = this.repository.findRevision(productNumber, revisedRevision)
                .orElseThrow()
                .getEntity();

        final EucegXmlDiff diff = new EucegXmlDiff(productNumber,
                EucegXmlDiff.getXml(originalProduct.getProduct(), Product.class, true),
                EucegXmlDiff.getXml(revisedProduct.getProduct(), Product.class, true));

        return ProductDifference.builder()
                .productNumber(productNumber)
                .originalRevision(originalRevision)
                .revisedRevision(revisedRevision)
                .changeType(diff.result().getChange())
                .patch(diff.result().getPatch())
                .build();
    }

    @Override
    public @Nonnull ProductRevision getCurrentRevision(@Nonnull final String productNumber) {
        return this.repository.findLastChangeRevision(productNumber).map(ProductRevision::fromRevision).orElseThrow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildWithAnotherProduct(final String previousProductNumber, final String productNumber) {
        return this.repository.hasChildWithAnotherProduct(previousProductNumber, productNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductEntity save(@Nonnull final ProductEntity entity) {
        Assert.checkNotNull(entity, "entity");
        final ProductEntity result = repository.save(normalize(entity, false));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductEntity saveAndFlush(@Nonnull final ProductEntity entity) {
        Assert.checkNotNull(entity, "entity");
        final ProductEntity result = repository.saveAndFlush(normalize(entity, false));
        return result;
    }

    @Override
    @Transactional
    public ProductEntity updateOnSubmission(@Nonnull final ProductEntity entity) {
        Assert.checkNotNull(entity, "entity");
        final ProductEntity result = repository.save(normalize(entity, true));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductEntity create(@Nonnull final ProductEntity entity) {
        Assert.checkNotNull(entity, "entity");
        final ProductEntity result = repository.save(normalize(entity, false));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void remove(@Nonnull final ProductEntity product) {
        Assert.checkNotNull(product, "submission");
        this.repository.delete(product);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void remove(@Nonnull final String productNumber) {
        this.repository.deleteById(Assert.checkHasText(productNumber, "productNumber"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(@Nonnull final ProductEntity entity, @Nonnull final ValidationResult result) {
        Assert.checkNotNull(entity, "product");
        Assert.checkNotNull(result, "result");
        return validate(ALL_TRANSFORMATION_SUBMISSION.apply(entity).getProduct(), result);
    }

    @Override
    public Product normalize(final Product product) {
        // create fake ProductEntity
        final ProductEntity entity = new ProductEntity().copy().product(product).build();
        ALL_TRANSFORMATION_SUBMISSION.apply(entity);
        return entity.getProduct();
    }

    @Override
    public ValidationResult validate(@Nonnull final Product product) {
        Assert.checkNotNull(product, "product");
        final ValidationResult result = new ValidationResult();
        try {
            ValidationHelper.validateSubmission(Eucegs.wrap(product, Product.class), result);
            return result;
        } catch (final JAXBException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    protected ProductEntity normalize(final ProductEntity entity, final boolean updateStatusOnSubmission) {
        return ALL_TRANSFORMATION_SUBMISSION.andThen(enforceProductSynchronization())
                .andThen(extractAttachments())
                .andThen(updateProductStatus())
                .andThen(updateProductPirStatus(updateStatusOnSubmission))
                .apply(entity);
    }

    private Function<IProductEntity, ProductEntity> enforceProductSynchronization() {
        return new BaseProductVisitor<>() {

            private ProductEntity.Builder builder;

            public ProductEntity visit(@Nonnull final IProductEntity entity) {
                builder = ((ProductEntity) entity).copy();
                visit((ProductEntity) entity);
                return builder.build();
            }

            private ProductEntity visit(@Nonnull final ProductEntity entity) {
                super.visit(entity);
                if (entity.getProduct() != null) {
                    builder.productType(ProductType.productType(entity.getProduct()));
                }
                return entity;
            }

            public Product visit(@Nonnull Product product) {
                return super.visit(product);
            }

            @Override
            public Product visit(@Nonnull EcigProduct product) {
                builder.type(product.getProductType().getValue());
                return super.visit(product);
            }

            @Override
            public Product visit(@Nonnull TobaccoProduct product) {
                builder.type(product.getProductType().getValue());
                return super.visit(product);
            }
        };
    }

    private Function<IProductEntity, ProductEntity> updateProductStatus() {
        return new BaseProductVisitor<>() {

            public ProductEntity visit(@Nonnull final IProductEntity entity) {
                return visit((ProductEntity) entity);
            }

            private ProductEntity visit(@Nonnull final ProductEntity entity) {

                ProductStatus oldStatus = entity.getStatus();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("updateProductStatus: {}", oldStatus);
                }
                if (ProductStatus.SENT.equals(oldStatus)) {
                    return entity;
                }

                final ValidationResult result = new ValidationResult();
                if (oldStatus == null) {
                    oldStatus = ProductStatus.DRAFT;
                }
                if (oldStatus == ProductStatus.VALID) {
                    oldStatus = ProductStatus.DRAFT;
                }
                final ProductStatus status = validate(entity.getProduct(), result) ? ProductStatus.VALID : oldStatus;
                return entity.copy().status(status).build();
            }

        };
    }

    private Function<IProductEntity, ProductEntity> extractAttachments() {
        return new BaseProductVisitor<>() {

            public ProductEntity visit(@Nonnull final IProductEntity entity) {
                return visit((ProductEntity) entity);
            }

            private ProductEntity visit(@Nonnull final ProductEntity entity) {
                Set<String> uuids = Eucegs.extractAttachementID(entity.getXmlProduct());
                return entity.copy().clearAttachments().attachments(uuids).build();
            }

        };

    }

    private Function<IProductEntity, ProductEntity> updateProductPirStatus(final boolean updateOnSubmission) {
        return new BaseProductVisitor<>() {

            public ProductEntity visit(@Nonnull final IProductEntity entity) {
                return visit((ProductEntity) entity);
            }

            private ProductEntity visit(@Nonnull final ProductEntity entity) {
                ProductPirStatus pirStatus = entity.getPirStatus();
                // by default
                if (pirStatus == null) {
                    pirStatus = ProductPirStatus.AWAITING;
                }
                final ProductEntity.Builder builder = entity.copy();
                final ProductEntity child = entity.getChild();
                if (child != null) {
                    final ProductPirStatus childPirStatus = child.getPirStatus();
                    // a old product becomes inactive unless has already withdrawn
                    if (!ProductPirStatus.WITHDRAWN.equals(childPirStatus)) {
                        builder.child(child.copy().pirStatus(ProductPirStatus.INACTIVE).build());
                    }
                }
                // a product becomes active after submission, same thing for withdrawal.
                if (updateOnSubmission) {
                    final SubmissionEntity latestSubmission = entity.getLastestSubmission();
                    if (latestSubmission != null) {
                        // only if has been submitted
                        if (SubmissionStatus.SUBMITTED.equals(latestSubmission.getSubmissionStatus())) {
                            // a product stays inactive unless is withdrawn
                            if (!ProductPirStatus.INACTIVE.equals(pirStatus)) {
                                pirStatus = ProductPirStatus.ACTIVE;
                            }
                            if (isProductWithdrawn(entity)) {
                                pirStatus = ProductPirStatus.WITHDRAWN;
                            }
                        }
                        builder.submission(latestSubmission.copy().pirStatus(pirStatus).build());
                    }
                }
                return builder.pirStatus(pirStatus).build();
            }

        };
    }

    private boolean isProductWithdrawn(final ProductEntity entity) {
        final SubmissionEntity latestSubmission = entity.getLastestSubmission();
        if (latestSubmission == null) {
            return false;
        }
        return latestSubmission.accept(new BaseSubmissionVisitor<Boolean>() {

            private boolean withdrawn = false;

            @Override
            public Boolean visit(@Nonnull final ISubmissionEntity entity) {
                if (entity.getSubmission() != null) {
                    visit(entity.getSubmission());
                }
                return withdrawn;
            }

            @Override
            public Submission visit(@Nonnull final EcigProductSubmission submission) {
                if (submission.getProduct() != null && submission.getProduct().getPresentations() != null
                        && submission.getProduct().getPresentations().getPresentation() != null) {
                    withdrawn = submission.getProduct()
                            .getPresentations()
                            .getPresentation()
                            .stream()
                            .allMatch(p -> p.getWithdrawalDate() != null && p.getWithdrawalIndication().isValue());
                }
                return submission;
            };

            @Override
            public Submission visit(@Nonnull final TobaccoProductSubmission submission) {
                if (submission.getProduct() != null && submission.getProduct().getPresentations() != null
                        && submission.getProduct().getPresentations().getPresentation() != null) {
                    withdrawn = submission.getProduct()
                            .getPresentations()
                            .getPresentation()
                            .stream()
                            .allMatch(p -> p.getWithdrawalDate() != null);
                }
                return submission;
            };
        });
    }

    private boolean validate(@Nonnull final Product product, @Nonnull final ValidationResult result) {
        Assert.checkNotNull(product, "product");
        Assert.checkNotNull(result, "result");
        try {
            return ValidationHelper.validateSubmission(Eucegs.wrap(product, Product.class), result);
        } catch (final JAXBException e) {
            LOGGER.warn(e.getMessage(), e);
            return false;
        }

    }

}
