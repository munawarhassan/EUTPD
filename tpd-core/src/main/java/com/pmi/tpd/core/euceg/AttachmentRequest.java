package com.pmi.tpd.core.euceg;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eu.ceg.AttachmentAction;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.IStatusAttachment;

import lombok.Getter;

/**
 * <p>
 * support hateoas architecture.
 * </p>
 *
 * @author Christophe Friederich
 * @since 2.0
 */
@Getter
@JsonSerialize
public class AttachmentRequest extends RepresentationModel<RepresentationModel<?>> {

    /** */
    private String attachmentId;

    /** */
    private String filename;

    /** */
    private String contentType;

    /** */
    private boolean confidential;

    /** */
    private AttachmentAction action;

    /** */
    private AttachmentSendStatus sendStatus;

    /** */
    private String createdBy;

    /** */
    private DateTime createdDate;

    /** */
    private String lastModifiedBy;

    /** */
    private DateTime lastModifiedDate;

    /** */
    private boolean deletable;

    /**
     * Transforms a list of {@link AttachmentEntity} to {@link AttachmentRequest}.
     *
     * @param entities
     *                 list of {@link AttachmentEntity} to transform.
     * @return Returns a list of {@link AttachmentRequest} corresponding transformation of {@link AttachmentEntity}.
     * @see #from(AttachmentEntity)
     * @see #from(AttachmentEntity, ResourceAssembler)
     */
    @Nonnull
    public static List<AttachmentRequest> map(@Nonnull final List<? extends IAttachmentEntity> entities) {
        return map(entities, null);
    }

    /**
     * Transforms a list of {@link AttachmentEntity} to {@link AttachmentRequest} using a HATEOS assembler.
     *
     * @param entities
     *                  list of {@link AttachmentEntity} to transform
     * @param assembler
     *                  a HATEOS assembler
     * @return Returns a list of {@link AttachmentRequest} corresponding transformation of {@link AttachmentEntity}.
     * @see #from(AttachmentEntity)
     * @see #from(AttachmentEntity, ResourceAssembler)
     */
    @Nonnull
    public static List<AttachmentRequest> map(@Nonnull final List<? extends IAttachmentEntity> entities,
        @Nullable final RepresentationModelAssembler<IAttachmentEntity, AttachmentRequest> assembler) {
        return entities.stream().map(input -> AttachmentRequest.from(input, assembler)).collect(Collectors.toList());
    }

    /**
     * Transforms a page of {@link AttachmentEntity} to {@link AttachmentRequest}.
     *
     * @param page
     *             page to transform.
     * @return Returns a page of {@link AttachmentRequest} corresponding transformation of {@link AttachmentEntity}.
     * @see #toResources(Page, ResourceAssembler)
     * @see #from(AttachmentEntity)
     * @see #from(AttachmentEntity, ResourceAssembler)
     */
    @Nonnull
    public static Page<AttachmentRequest> map(@Nonnull final Page<IAttachmentEntity> page) {
        return toResources(page, null);
    }

    /**
     * Transforms a page of {@link AttachmentEntity} to {@link AttachmentRequest} using a HATEOS assembler.
     *
     * @param page
     *                  page to transform.
     * @param assembler
     *                  a HATEOS assembler
     * @return Returns a page of {@link AttachmentRequest} corresponding transformation of {@link AttachmentEntity}.
     */
    @Nonnull
    public static Page<AttachmentRequest> toResources(@Nonnull final Page<IAttachmentEntity> page,
        @Nullable final RepresentationModelAssembler<IAttachmentEntity, AttachmentRequest> assembler) {
        return page.map(entity -> AttachmentRequest.from(entity, assembler));
    }

    public static Page<AttachmentRequest> toResourcesForIndexed(final Page<AttachmentIndexed> page,
        final RepresentationModelAssembler<AttachmentIndexed, AttachmentRequest> assembler) {
        return page.map(entity -> AttachmentRequest.from(entity, assembler));
    }

    /**
     * Transforms a {@link AttachmentEntity entity} to {@link AttachmentRequest}.
     *
     * @param entity
     *               a entity to transform.
     * @return Returns new instance of {@link AttachmentRequest} representing transformation of
     *         {@link AttachmentEntity}.
     */
    @Nonnull
    public static AttachmentRequest from(@Nonnull final IAttachmentEntity entity) {
        return from(entity, null);
    }

    /**
     * Transforms a {@link AttachmentEntity entity} to {@link AttachmentRequest} using a HATEOS assembler.
     *
     * @param entity
     *                  a entity to transform.
     * @param assembler
     *                  a HATEOS assembler
     * @return Returns new instance of {@link AttachmentRequest} representing transformation of
     *         {@link AttachmentEntity}.
     */
    @Nonnull
    public static AttachmentRequest from(@Nonnull final IAttachmentEntity entity,
        @Nullable final RepresentationModelAssembler<IAttachmentEntity, AttachmentRequest> assembler) {
        Assert.checkNotNull(entity, "entity");
        AttachmentRequest request;
        if (assembler != null) {
            request = assembler.toModel(entity);
        } else {
            request = new AttachmentRequest();
        }
        final Optional<IStatusAttachment> status = entity.getDefaultStatus();
        IStatusAttachment aStatus = null;
        if (status.isPresent()) {
            aStatus = status.get();
            request.sendStatus = aStatus.getSendStatus();
            request.action = aStatus.getAction();
        } else {
            request.sendStatus = AttachmentSendStatus.NO_SEND;
            request.action = AttachmentAction.CREATE;
        }
        request.deletable = !status.isPresent();
        request.attachmentId = entity.getAttachmentId();
        request.filename = entity.getFilename();
        request.confidential = entity.isConfidential();
        request.contentType = entity.getContentType();

        request.createdBy = entity.getCreatedBy();
        request.createdDate = entity.getCreatedDate();
        request.lastModifiedBy = entity.getLastModifiedBy();
        request.lastModifiedDate = entity.getLastModifiedDate();
        return request;
    }

    public static AttachmentRequest from(@Nonnull final AttachmentIndexed entity,
        final RepresentationModelAssembler<AttachmentIndexed, AttachmentRequest> assembler) {
        Assert.checkNotNull(entity, "entity");
        AttachmentRequest request;
        if (assembler != null) {
            request = assembler.toModel(entity);
        } else {
            request = new AttachmentRequest();
        }

        request.sendStatus = entity.getStatus();
        request.action = entity.getAction();

        request.deletable = entity.getAction() == null;
        request.attachmentId = entity.getId();
        request.filename = entity.getFilename();
        request.confidential = entity.isConfidential();
        request.contentType = entity.getContentType();

        request.createdBy = entity.getCreatedBy();
        request.createdDate = entity.getCreatedDate();
        request.lastModifiedBy = entity.getLastModifiedBy();
        request.lastModifiedDate = entity.getLastModifiedDate();
        return request;
    }

}
