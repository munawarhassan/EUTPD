package com.pmi.tpd.core.euceg.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductSubmission;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.CombinableMatcher;
import org.hamcrest.core.IsNull;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.history.Revision;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.RandomUtil;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.euceg.ProductSubmissionHelper;
import com.pmi.tpd.core.euceg.spi.IProductIdGeneratorRepository;
import com.pmi.tpd.core.euceg.spi.IProductRepository;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionRepository;
import com.pmi.tpd.core.euceg.spi.IProductSubmissionStore;
import com.pmi.tpd.core.model.euceg.PayloadEntity;
import com.pmi.tpd.core.model.euceg.ProductDifference;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.ProductRevision;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

@Configuration
@ContextConfiguration(classes = { ProductStoreIT.class })
public class ProductStoreIT extends BaseDaoTestIT {

    @Inject
    private IProductStore productStore;

    @Inject
    private IProductSubmissionStore submissionStore;

    @Inject
    private TransactionTemplate transactionTemplate;

    @Inject
    private IProductRepository productRepository;

    @Bean()
    public TransactionTemplate TransactionTemplate(final PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public IProductSubmissionRepository productSubmissionRepository(final EntityManager entityManager) {
        return new JpaProductSubmissionRepository(entityManager);
    }

    @Bean
    public IProductIdGeneratorRepository productIdGeneratorRepository(final EntityManager entityManager) {
        return new JpaProductIdGeneratorRepository(entityManager);
    }

    @Bean
    public IProductRepository productRepository(final EntityManager entityManager) {
        return new JpaProductRepository(entityManager);
    }

    @Bean
    public IProductStore productStore(final IProductRepository repository) {
        return new ProductStore(repository);
    }

    @Bean
    public IProductSubmissionStore submissionStore(final IProductSubmissionRepository repository,
        final IProductIdGeneratorRepository productIdGeneratorRepository) {
        return new ProductSubmissionStore(repository, productIdGeneratorRepository);
    }

    @Test
    public void createTobaccoProduct() {
        final String submitterId = "submitterId";
        final ProductEntity entity = productStore.create(createTobaccoProduct(submitterId).build());
        assertEquals(ProductType.TOBACCO, entity.getProductType());
        assertEquals(ProductStatus.DRAFT, entity.getStatus());
        assertEquals(ProductPirStatus.AWAITING, entity.getPirStatus());
        assertNull(entity.getLastestSubmission());
        assertEquals(submitterId, entity.getSubmitterId());
        assertThat("the submisstions property must be empty and not null",
            entity.getSubmissions(),
            CombinableMatcher.both(IsEmptyCollection.empty()).and(IsNull.notNullValue()));
    }

    @Test
    public void createEcigProduct() {
        final String submitterId = "submitterId";
        final ProductEntity entity = productStore.create(createEcigProduct("submitterId").build());
        assertEquals(ProductType.ECIGARETTE, entity.getProductType());
        assertEquals(ProductStatus.DRAFT, entity.getStatus());
        assertEquals(ProductPirStatus.AWAITING, entity.getPirStatus());
        assertNull(entity.getLastestSubmission());
        assertEquals(submitterId, entity.getSubmitterId());
        assertThat("the submisstions property must be empty and not null",
            entity.getSubmissions(),
            CombinableMatcher.both(IsEmptyCollection.empty()).and(IsNull.notNullValue()));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldInactivateChild() {
        final ProductEntity child = productStore
                .create(createTobaccoProduct("submitterId").pirStatus(ProductPirStatus.ACTIVE).build());
        ProductEntity entity = productStore.create(createTobaccoProduct("submitterId").child(child).build());
        entity = productStore.get(Assert.checkNotNull(entity.getProductNumber(), "productNumber"));
        assertEquals(ProductPirStatus.INACTIVE, entity.getChild().getPirStatus());
    }

    @Test
    public void shouldLetWithdrawnChild() {
        final ProductEntity child = productStore
                .create(createTobaccoProduct("submitterId").pirStatus(ProductPirStatus.WITHDRAWN).build());
        ProductEntity entity = productStore.create(createTobaccoProduct("submitterId").child(child).build());
        entity = productStore.get(Assert.checkNotNull(entity.getProductNumber(), "productNumber"));
        assertEquals(ProductPirStatus.WITHDRAWN, entity.getChild().getPirStatus());
    }

    @Test
    public void shouldActivateProduct() {
        final String submitterId = "12345";
        ProductEntity product = productStore.create(createTobaccoProduct(submitterId).build());
        SubmissionEntity submission = createSubmission(product, SendSubmissionType.IMMEDIAT).build();
        submission = this.submissionStore.create(submission);
        // submit submission
        this.submissionStore.save(submission.copy()
                .receipt(TransmitReceiptEntity.create(submission,
                    PayloadType.SUBMISSION,
                    submission.getProductId(),
                    RandomUtil.uuid(),
                    TransmitStatus.RECEIVED))
                .build());
        // enforce update
        product = productStore.updateOnSubmission(product);
        assertEquals(ProductPirStatus.ACTIVE, product.getPirStatus());
    }

    @Test
    public void shouldWithdranProduct() {
        final String submitterId = "12345";
        ProductEntity product = createTobaccoProduct(submitterId).build();
        ((TobaccoProduct) product.getProduct()).getPresentations()
                .getPresentation()
                .forEach(p -> p.withWithdrawalDate(Eucegs.toDate(new LocalDate())));
        product = productStore.create(product);
        SubmissionEntity submission = createSubmission(product, SendSubmissionType.IMMEDIAT).build();
        submission = this.submissionStore.create(submission);
        // submit submission
        this.submissionStore.save(submission.copy()
                .receipt(TransmitReceiptEntity.create(submission,
                    PayloadType.SUBMISSION,
                    submission.getProductId(),
                    RandomUtil.uuid(),
                    TransmitStatus.RECEIVED))
                .build());
        // enforce update
        product = productStore.updateOnSubmission(product);
        assertEquals(ProductPirStatus.WITHDRAWN, product.getPirStatus());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldCreateRevision() {
        final String submitterId = "submitterId";
        final ProductEntity entity = productStore.create(createTobaccoProduct(submitterId).build());
        final String productId = entity.getId();
        transactionTemplate.execute(status -> {
            final Optional<Revision<Integer, ProductEntity>> revision = productRepository
                    .findLastChangeRevision(productId);
            revision.ifPresentOrElse(rev -> {
                final ProductEntity e = rev.getEntity();
                assertNotNull(rev.getRequiredRevisionInstant());
                assertNotNull(rev.getRequiredRevisionNumber());
                assertEquals(productId, e.getId());
                final PayloadEntity payload = e.getPayloadProduct();
                assertNotNull(payload);
                assertNotNull(payload.getId());
                final String data = payload.getData();
                assertNotNull(data);
            }, () -> fail("Revision should exist"));
            return null;
        });
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldCreateTwoRevisions() {
        final String submitterId = "submitterId";

        ProductEntity toStore = createTobaccoProduct(submitterId, new BigDecimal("18.0")).build();
        final String productNumber = Assert.checkNotNull(toStore.getId(), "productNumber");
        toStore = productStore.create(toStore);

        final TobaccoProduct product = (TobaccoProduct) toStore.getProduct();
        product.setLength(Eucegs.decimal(new BigDecimal("20.0")));
        productStore.save(toStore.copy().lastModifiedDate(DateTime.now()).product(product).build());

        final Page<ProductRevision> revisions = productStore.findRevisions(productNumber, PageUtils.newRequest(0, 10));
        assertEquals(2, revisions.toList().size());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldFindDifferences() throws IOException {
        final String submitterId = "submitterId";

        ProductEntity toStore = createTobaccoProduct(submitterId, new BigDecimal("18.0")).build();
        final String productNumber = Assert.checkNotNull(toStore.getId(), "productNumber");
        toStore = productStore.create(toStore);

        final TobaccoProduct product = (TobaccoProduct) toStore.getProduct();
        product.setLength(Eucegs.decimal(new BigDecimal("20.0")));
        productStore.save(toStore.copy().lastModifiedDate(DateTime.now()).product(product).build());

        final List<ProductRevision> revisions = productStore.findRevisions(productNumber, PageUtils.newRequest(0, 10))
                .toList();

        final ProductDifference diff = productStore
                .compareRevisions(productNumber, revisions.get(0).getId(), revisions.get(1).getId());

        assertTrue(diff.getPatch().indexOf("Length") >= 0);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void shouldMatchDifferences() throws IOException {
        final String NEW_TEXT = "20.0";
        final String OLD_TEXT = "18.0";

        final String submitterId = "submitterId";

        ProductEntity toStore = createTobaccoProduct(submitterId, new BigDecimal(OLD_TEXT))
                .productNumber("7dcf3333-79be-435a-ba1d-89efe08be5db")
                .build();
        final String productNumber = Assert.checkNotNull(toStore.getId(), "productNumber");
        toStore = productStore.create(toStore);

        final TobaccoProduct product = (TobaccoProduct) toStore.getProduct();
        product.setLength(Eucegs.decimal(new BigDecimal(NEW_TEXT)));
        productStore.save(toStore.copy().lastModifiedDate(DateTime.now()).product(product).build());

        final List<ProductRevision> revisions = productStore
                .findRevisions(productNumber, PageUtils.newRequest(0, 10, Sort.by(Order.desc("version"))))
                .toList();

        // the first is first revision
        // the second is last resision
        final ProductDifference diff = productStore
                .compareRevisions(productNumber, revisions.get(0).getId(), revisions.get(1).getId());
        // check inc of version and start to 1.
        assertEquals(2, revisions.get(0).getVersion());
        assertEquals(1, revisions.get(1).getVersion());
        approve(diff.getPatch());
    }

    private ProductEntity.Builder createTobaccoProduct(final String submitterId, final BigDecimal length) {
        return ProductEntity.builder()
                .productNumber(Eucegs.uuid())
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstTobaccoProduct(length));
    }

    private ProductEntity.Builder createTobaccoProduct(final String submitterId) {
        return ProductEntity.builder()
                .productNumber(Eucegs.uuid())
                .submitterId(submitterId)
                .status(ProductStatus.VALID)
                .product(ProductSubmissionHelper.okFirstTobaccoProduct());
    }

    private ProductEntity.Builder createEcigProduct(final String submitterId) {
        return ProductEntity.builder()
                .productNumber(Eucegs.uuid())
                .submitterId(submitterId)
                .product(ProductSubmissionHelper.okFirstEcigProduct());
    }

    private SubmissionEntity.Builder createSubmission(final ProductEntity entity, final SendSubmissionType sendType) {
        final TobaccoProductSubmission submission = ProductSubmissionHelper
                .okTobaccoProductFirstSubmission((TobaccoProduct) entity.getProduct());
        submission.getSubmitter().withSubmitterID(entity.getSubmitterId());
        return SubmissionEntity.builder()
                .productType(ProductType.TOBACCO)
                .submissionType(SubmissionTypeEnum.NEW)
                .product(entity)
                .sendType(sendType)
                .submitterId(entity.getSubmitterId())
                .submission(submission)
                .sentBy("user");
    }
}
