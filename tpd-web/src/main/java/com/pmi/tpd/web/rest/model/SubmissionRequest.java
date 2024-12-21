package com.pmi.tpd.web.rest.model;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Submission;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductSubmission;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.SubmissionEntity;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@JsonSerialize
public class SubmissionRequest extends RepresentationModel<RepresentationModel<?>> {

    /** */
    private Long submissionId;

    /** */
    private String productId;

    /** */
    private ProductType type;

    /** */
    private String productNumber;

    /** */
    private int productType;

    /** */
    private String previousProductId;

    /** */
    private List<PresentationRequest> presentations;

    /** */
    private SubmissionTypeEnum submissionType;

    /** */
    private SendSubmissionType sendType;

    /** */
    private boolean latest;

    /** */
    private boolean latestSumbitted;

    /** */
    private SubmissionStatus submissionStatus;

    /** */
    private DateTime lastModifiedDate;

    /** */
    private float progress;

    /** */
    private boolean cancelable;

    /** */
    private boolean exportable;

    /** */
    private String sentBy;

    /** */
    private ProductPirStatus pirStatus;

    public static List<SubmissionRequest> map(@Nonnull final List<SubmissionEntity> entities) {
        return map(entities, null);
    }

    public static List<SubmissionRequest> map(@Nonnull final List<SubmissionEntity> entities,
        @Nullable final RepresentationModelAssembler<SubmissionEntity, SubmissionRequest> assembler) {
        return Lists.transform(entities,
            (@Nonnull final SubmissionEntity input) -> SubmissionRequest.from(input, assembler));
    }

    public static Page<SubmissionRequest> map(@Nonnull final Page<SubmissionEntity> entities) {
        return toResources(entities, null);
    }

    public static Page<SubmissionRequest> toResources(@Nonnull final Page<SubmissionEntity> page,
        @Nullable final RepresentationModelAssembler<SubmissionEntity, SubmissionRequest> assembler) {
        return page.map((@Nonnull final SubmissionEntity entity) -> SubmissionRequest.from(entity, assembler));
    }

    public static Page<SubmissionRequest> toResourcesForIndexed(@Nonnull final Page<SubmissionIndexed> page,
        @Nullable final RepresentationModelAssembler<SubmissionIndexed, SubmissionRequest> assembler) {
        return page.map((@Nonnull final SubmissionIndexed entity) -> SubmissionRequest.from(entity, assembler));
    }

    public static SubmissionRequest from(@Nonnull final SubmissionEntity entity) {
        return from(entity, null);
    }

    public static SubmissionRequest from(@Nonnull final SubmissionEntity entity,
        @Nullable final RepresentationModelAssembler<SubmissionEntity, SubmissionRequest> assembler) {
        checkNotNull(entity, "entity");
        final Submission submission = entity.getSubmission();
        SubmissionRequest request = null;
        if (assembler == null) {
            request = new SubmissionRequest();
        } else {
            request = assembler.toModel(entity);
        }
        request.submissionId = entity.getId();
        request.productId = entity.getProductId();
        final ProductEntity productEntity = entity.getProduct();
        request.productNumber = productEntity.getProductNumber();
        request.type = entity.getProductType();
        request.progress = entity.getProgress();
        if (submission instanceof EcigProductSubmission) {
            final EcigProductSubmission sub = (EcigProductSubmission) submission;
            final EcigProduct product = sub.getProduct();
            request.productType = product.getProductType().getValue().value();
            request.previousProductId = product.getPreviousProductID() != null
                    ? product.getPreviousProductID().getValue() : null;
            request.presentations = product.getPresentations()
                    .getPresentation()
                    .stream()
                    .map(PresentationRequest::from)
                    .collect(Collectors.toList());
        } else if (submission instanceof TobaccoProductSubmission) {
            final TobaccoProductSubmission product = (TobaccoProductSubmission) submission;
            request.productType = product.getProduct().getProductType().getValue().value();
            request.previousProductId = product.getProduct().getPreviousProductID() != null
                    ? product.getProduct().getPreviousProductID().getValue() : null;
            request.presentations = product.getProduct()
                    .getPresentations()
                    .getPresentation()
                    .stream()
                    .map(PresentationRequest::from)
                    .collect(Collectors.toList());
        }

        request.submissionType = entity.getSubmissionType();
        request.submissionStatus = entity.getSubmissionStatus();
        request.sendType = entity.getSendType();
        request.latest = entity.isLatest();
        request.latestSumbitted = entity.isLatestSubmitted();
        request.cancelable = request.submissionStatus.cancelable();
        request.exportable = request.submissionStatus.exportable();
        request.lastModifiedDate = entity.getLastModifiedDate();
        request.sentBy = entity.getSentBy();
        request.pirStatus = entity.getPirStatus();
        return request;
    }

    public static SubmissionRequest from(@Nonnull final SubmissionIndexed entity,
        @Nullable final RepresentationModelAssembler<SubmissionIndexed, SubmissionRequest> assembler) {
        checkNotNull(entity, "entity");
        SubmissionRequest request = null;
        if (assembler == null) {
            request = new SubmissionRequest();
        } else {
            request = assembler.toModel(entity);
        }
        request.submissionId = entity.getId();
        request.productId = entity.getProductId();
        request.productNumber = entity.getProductNumber();
        request.type = entity.getType();
        request.progress = entity.getProgress();;
        request.productType = entity.getProductType();
        request.previousProductId = entity.getPreviousProductId();
        request.presentations = entity.getPresentations()
                .stream()
                .map(PresentationRequest::from)
                .collect(Collectors.toList());

        request.submissionType = SubmissionTypeEnum.fromValue(entity.getSubmissionType());
        request.submissionStatus = entity.getSubmissionStatus();
        request.sendType = entity.getSendType();
        request.latest = entity.isLatest();
        request.latestSumbitted = entity.isLatestSubmitted();
        request.cancelable = request.submissionStatus.cancelable();
        request.exportable = request.submissionStatus.exportable();
        request.lastModifiedDate = entity.getLastModifiedDate();
        request.sentBy = entity.getSentBy();
        request.pirStatus = entity.getPirStatus();
        return request;
    }

}
