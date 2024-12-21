package com.pmi.tpd.core.euceg;

import java.io.OutputStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Singleton;

import org.eu.ceg.SubmissionTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.paging.Filter;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.repositories.IProductIndexedRepository;
import com.pmi.tpd.core.euceg.spi.IAttachmentStore;
import com.pmi.tpd.core.euceg.spi.IProductStore;
import com.pmi.tpd.euceg.api.EucegException;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.core.BulkRequest;
import com.pmi.tpd.euceg.core.EucegProduct;
import com.pmi.tpd.euceg.core.exporter.IDataProvider;
import com.pmi.tpd.euceg.core.exporter.product.JXPathExcelExporterEcigProduct;
import com.pmi.tpd.euceg.core.exporter.product.JXPathExcelExporterTobaccoProduct;

@Singleton
@Service
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class DefaultBulkProductService implements IBulkProductService {

    public DefaultBulkProductService(final IProductStore productStore,
            final IProductIndexedRepository productIndexedRepository, final IAttachmentStore attachmentStore,
            final ISubmissionService submissionService, final IEucegConstraintRuleManager constraintRules,
            final I18nService i18nService, final ObjectMapper objectMapper) {
        this.productStore = productStore;
        this.productIndexedRepository = productIndexedRepository;
        this.attachmentStore = attachmentStore;
        this.i18nService = i18nService;
        this.objectMapper = objectMapper;
        this.submissionService = submissionService;
        this.constraintRules = constraintRules;
    }

    private final IProductStore productStore;

    /** */
    private final IProductIndexedRepository productIndexedRepository;

    /** */
    private final IAttachmentStore attachmentStore;

    /** */
    private final I18nService i18nService;

    /** */
    private final ObjectMapper objectMapper;

    private final ISubmissionService submissionService;

    /** */
    private final IEucegConstraintRuleManager constraintRules;

    /**
     * {@inheritDoc}
     */
    @Override
    @PreAuthorize("hasGlobalPermission('USER')")
    public void exportToExcel(@Nonnull final OutputStream stream,
        final @Nonnull ProductType productType,
        @Nonnull final BulkRequest request) {
        final var dataProvider = new BulkProductDataProivder(productType, request, attachmentStore, productStore,
                productIndexedRepository);
        try {
            if (ProductType.ECIGARETTE.equals(productType)) {
                new JXPathExcelExporterEcigProduct(dataProvider, request, objectMapper).export(stream);
            } else if (ProductType.TOBACCO.equals(productType)) {
                new JXPathExcelExporterTobaccoProduct(dataProvider, request, objectMapper).export(stream);
            } else {
                throw new RuntimeException("unknown product type");
            }
        } catch (final Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @PreAuthorize("hasGlobalPermission('USER')")
    @Override
    public void bulkSend(@Nonnull final ProductType productType, @Nonnull final BulkRequest request) {
        Assert.checkNotNull(request, "request");
        // pre check
        findAllSendProductForBulk(productType, request).forEach(product -> {
            SubmissionTypeEnum resolvedSubmissionType = resolveSubmissionType(product, request);
            if (SubmissionTypeEnum.MODIFICATION_NEW.equals(resolvedSubmissionType)) {
                throw new EucegException(i18nService.createKeyedMessage(
                    "app.service.euceg.submission.send.bulk.modificationnewnotaccepted",
                    product.getProductNumber()));
            }
            constraintRules.checkNewProductSubmissionIsPossible(product, resolvedSubmissionType);
        });

        // create deferred submission;
        findAllSendProductForBulk(productType, request).map(product -> SubmissionSendRequest.builder()
                .productNumber(product.getProductNumber())
                .sendType(BulkRequest.BulkAction.createSubmission.equals(request.getAction())
                        ? SendSubmissionType.MANUAL : SendSubmissionType.DEFERRED)
                .submissionType(resolveSubmissionType(product, request))
                .build()).forEach(req -> submissionService.createSubmission(req));
    }

    private Stream<IProductEntity> findAllSendProductForBulk(@Nonnull final ProductType productType,
        final BulkRequest request) {
        final Filters filters = request.getPagingFilters();
        filters.addOrReplace(Filter.eq("productType", productType));
        filters.addOrReplace(Filter.eq("status", ProductStatus.VALID));
        final Pageable pageable = PageUtils
                .newRequest(0, 1000, Sort.by(Direction.DESC, "lastModifiedDate"), filters, null);
        return Streams.stream(PageUtils.asIterable(
            page -> this.productIndexedRepository.findAll(page).map(p -> this.productStore.get(p.getProductNumber())),
            pageable));
    }

    @Nullable
    private SubmissionTypeEnum resolveSubmissionType(final IProductEntity product, final BulkRequest request) {
        // override submission type
        SubmissionTypeEnum submissionType = product.getPreferredSubmissionType();
        if (request.getData() != null && request.getData().get("overrideSubmissionType") != null) {
            submissionType = SubmissionTypeEnum
                    .fromValue(Integer.valueOf(request.getData().get("overrideSubmissionType")));
        }
        return submissionType;
    }

    @Nonnull
    protected static EucegProduct createEucegProduct(final IProductEntity entity) {
        return EucegProduct.builder()
                .product(entity.getProduct())
                .submitterId(entity.getSubmitterId())
                .internalProductNumber(entity.getInternalProductNumber())
                .productNumber(entity.getProductNumber())
                .preferredSubmissionType(entity.getPreferredSubmissionType())
                .previousProductNumber(entity.getPreviousProductNumber())
                .generalComment(entity.getPreferredGeneralComment())
                .pirStatus(entity.getPirStatus())
                .status(entity.getStatus())
                .lastModifiedDate(entity.getLastModifiedDate())
                .lastModifiedBy(entity.getLastModifiedBy())
                .createdDate(entity.getCreatedDate())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private static class BulkProductDataProivder implements IDataProvider<EucegProduct> {

        private final ProductType productType;

        private final IAttachmentStore attachmentStore;

        private final IProductStore productStore;

        private final IProductIndexedRepository productIndexedRepository;

        private final BulkRequest request;

        public BulkProductDataProivder(final ProductType productType, final BulkRequest request,
                final IAttachmentStore attachmentStore, final IProductStore productStore,
                final IProductIndexedRepository productIndexedRepository) {
            super();
            this.productType = productType;
            this.request = request;
            this.attachmentStore = attachmentStore;
            this.productStore = productStore;
            this.productIndexedRepository = productIndexedRepository;
        }

        @Override
        @Nonnull
        public String getAttachementFilename(@Nonnull final String uuid) {
            return attachmentStore.get(uuid).getFilename();
        }

        @Override
        @Nonnull
        public Page<EucegProduct> findAll(@Nonnull final Pageable pageable) {
            return productIndexedRepository.findAll(pageable)
                    .map(p -> createEucegProduct(this.productStore.get(p.getProductNumber())));
        }

        @Override
        public Pageable getInitialPageableRequest() {
            return PageUtils
                    .newRequest(0, 20, Sort.by(Direction.DESC, "lastModifiedDate"), request.getPagingFilters(), null);

        }

    }
}
