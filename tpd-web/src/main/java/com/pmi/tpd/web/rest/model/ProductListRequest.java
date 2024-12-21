package com.pmi.tpd.web.rest.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.Product;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProduct;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.model.AttachedAttachmentIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@JsonSerialize
public class ProductListRequest extends RepresentationModel<RepresentationModel<?>> {

    /** */
    private ProductType productType;

    /** */
    private String productNumber;

    /** */
    private String child;

    /** */
    private String submitterId;

    /** */
    private int type;

    /** */
    private List<PresentationRequest> presentations;

    /** */
    private SubmissionTypeEnum submissionType;

    /** */
    private ProductStatus status;

    private ProductPirStatus pirStatus;

    /** */
    private boolean readOnly;

    /** */
    private boolean sendable;

    /** */
    private DateTime lastModifiedDate;

    /** */
    private SubmissionStatus latestSubmissionStatus;

    /** */
    private boolean latestError;

    /** */
    private String sourceFilename;

    /** */
    private Set<String> attachments;

    /**
     * @param entities
     * @param assembler
     * @return
     */
    public static List<ProductListRequest> map(final List<? extends IProductEntity> entities,
        final RepresentationModelAssembler<IProductEntity, ProductListRequest> assembler) {
        return Lists.transform(entities, input -> ProductListRequest.from(input, assembler));
    }

    /**
     * @param page
     * @param assembler
     * @return
     */
    public static <T extends IProductEntity> Page<ProductListRequest> toResources(final Page<T> page,
        final RepresentationModelAssembler<T, ProductListRequest> assembler) {
        return page.map(entity -> ProductListRequest.from(entity, assembler));
    }

    /**
     * @param page
     * @param assembler
     * @return
     */
    public static Page<ProductListRequest> toResourcesForIndexed(final Page<ProductIndexed> page,
        final RepresentationModelAssembler<ProductIndexed, ProductListRequest> assembler) {
        return page.map(entity -> ProductListRequest.from(entity, assembler));
    }

    /**
     * @param entity
     * @param assembler
     * @return
     */
    public static <T extends IProductEntity> ProductListRequest from(@Nonnull final T entity,
        final RepresentationModelAssembler<T, ProductListRequest> assembler) {
        Assert.checkNotNull(entity, "entity");
        final Product product = entity.getProduct();
        ProductListRequest request = null;
        if (assembler != null) {
            request = assembler.toModel(entity);
        } else {
            request = new ProductListRequest();
        }
        request.productNumber = entity.getProductNumber();
        request.productType = entity.getProductType();
        request.child = entity.getChild() != null ? entity.getChild().getId() : null;
        if (product instanceof EcigProduct) {
            final EcigProduct ecigProduct = (EcigProduct) product;
            request.type = ecigProduct.getProductType().getValue().value();
            request.presentations = ecigProduct.getPresentations()
                    .getPresentation()
                    .stream()
                    .map(PresentationRequest::from)
                    .collect(Collectors.toList());
        } else if (product instanceof TobaccoProduct) {
            final TobaccoProduct tobaccoProduct = (TobaccoProduct) product;
            request.type = tobaccoProduct.getProductType().getValue().value();
            request.presentations = tobaccoProduct.getPresentations()
                    .getPresentation()
                    .stream()
                    .map(PresentationRequest::from)
                    .collect(Collectors.toList());
        }
        request.submissionType = entity.getPreferredSubmissionType();
        request.pirStatus = entity.getPirStatus();
        request.status = entity.getStatus();
        request.lastModifiedDate = entity.getLastModifiedDate();
        request.readOnly = entity.isReadOnly();
        request.sendable = entity.isSendable();
        request.submitterId = entity.getSubmitterId();
        request.sourceFilename = entity.getSourceFilename();
        final ISubmissionEntity latestSubmission = entity.getLastestSubmission();
        if (latestSubmission != null) {
            request.latestError = latestSubmission.isError();
            request.latestSubmissionStatus = latestSubmission.getSubmissionStatus();
        }
        request.attachments = entity.getAttachments();
        return request;
    }

    public static ProductListRequest from(@Nonnull final ProductIndexed entity,
        final RepresentationModelAssembler<ProductIndexed, ProductListRequest> assembler) {
        Assert.checkNotNull(entity, "entity");
        ProductListRequest request = null;
        if (assembler != null) {
            request = assembler.toModel(entity);
        } else {
            request = new ProductListRequest();
        }
        request.productNumber = entity.getProductNumber();
        request.type = entity.getType();
        request.productType = entity.getProductType();

        if (entity.getPresentations() != null) {
            request.presentations = entity.getPresentations()
                    .stream()
                    .map(PresentationRequest::from)
                    .collect(Collectors.toList());
        }
        request.child = entity.getChild();

        request.submissionType = com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum.fromValue(entity.getSubmissionType())
                .map(com.pmi.tpd.euceg.core.refs.SubmissionTypeEnum::getEnum)
                .orElse(SubmissionTypeEnum.NEW);
        request.status = entity.getStatus();
        request.pirStatus = entity.getPirStatus();
        request.lastModifiedDate = entity.getLastModifiedDate();
        request.readOnly = entity.isReadOnly();
        request.sendable = entity.isSendable();
        request.sourceFilename = entity.getSourceFilename();
        request.submitterId = entity.getSubmitterId();
        request.latestError = entity.isLatestError();
        request.latestSubmissionStatus = entity.getLatestSubmissionStatus();
        if (entity.getAttachments() != null) {
            request.attachments = entity.getAttachments()
                    .stream()
                    .map(AttachedAttachmentIndexed::getUuid)
                    .collect(Collectors.toSet());
        }
        return request;
    }

}
