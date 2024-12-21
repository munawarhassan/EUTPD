package com.pmi.tpd.web.rest.model;

import java.util.List;

import javax.annotation.Nonnull;

import org.eu.ceg.SubmitterDetails;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Getter
@JsonSerialize
public class SubmitterListRequest extends RepresentationModel<RepresentationModel<?>> {

    /** */
    private String submitterId;

    /** */
    private String name;

    /** */
    private SubmitterStatus status;

    /** */
    private boolean sme;

    /** */
    private String vat;

    /** */
    private String address;

    /** */
    private String country;

    /** */
    private String phone;

    /** */
    private String email;

    /** */
    private String createdBy;

    /** */
    private DateTime createdDate;

    /** */
    private String lastModifiedBy;

    /** */
    private DateTime lastModifiedDate;

    /**
     * @param entities
     * @return
     */
    public static List<SubmitterListRequest> map(final List<SubmitterEntity> entities) {
        return map(entities, null);
    }

    /**
     * @param entities
     * @param assembler
     * @return
     */
    public static List<SubmitterListRequest> map(final List<SubmitterEntity> entities,
        final RepresentationModelAssembler<SubmitterEntity, SubmitterListRequest> assembler) {
        return Lists.transform(entities, input -> SubmitterListRequest.from(input, assembler));
    }

    /**
     * @param page
     * @return
     */
    public static Page<SubmitterListRequest> map(final Page<SubmitterEntity> page) {
        return toResources(page, null);
    }

    /**
     * @param page
     * @param assembler
     * @return
     */
    public static Page<SubmitterListRequest> toResources(final Page<SubmitterEntity> page,
        final RepresentationModelAssembler<SubmitterEntity, SubmitterListRequest> assembler) {
        return page.map(entity -> SubmitterListRequest.from(entity, assembler));
    }

    /**
     * @param page
     * @param assembler
     * @return
     */
    public static Page<SubmitterListRequest> toResourcesForIndexed(final Page<SubmitterIndexed> page,
        final RepresentationModelAssembler<SubmitterIndexed, SubmitterListRequest> assembler) {
        return page.map(entity -> SubmitterListRequest.from(entity, assembler));
    }

    /**
     * @param entity
     * @return
     */
    public static SubmitterListRequest from(@Nonnull final SubmitterEntity entity) {
        return from(entity, null);
    }

    /**
     * @param entity
     * @param assembler
     * @return
     */
    public static SubmitterListRequest from(@Nonnull final SubmitterEntity entity,
        final RepresentationModelAssembler<SubmitterEntity, SubmitterListRequest> assembler) {
        Assert.checkNotNull(entity, "entity");
        final SubmitterDetails details = Assert.checkNotNull(entity.getSubmitterDetails(), "submitterDetails");
        SubmitterListRequest request;
        if (assembler != null) {
            request = assembler.toModel(entity);
        } else {
            request = new SubmitterListRequest();
        }
        request.submitterId = entity.getSubmitterId();
        request.name = details.getName();
        request.status = entity.getStatus();
        request.vat = details.getVatNumber();
        request.address = details.getAddress();
        request.country = details.getCountry().value();
        request.phone = details.getPhoneNumber();
        request.email = details.getEmail();

        request.createdBy = entity.getCreatedBy();
        request.createdDate = entity.getCreatedDate();
        request.lastModifiedBy = entity.getLastModifiedBy();
        request.lastModifiedDate = entity.getLastModifiedDate();

        return request;
    }

    /**
     * @param entity
     * @param assembler
     * @return
     */
    public static SubmitterListRequest from(@Nonnull final SubmitterIndexed entity,
        final RepresentationModelAssembler<SubmitterIndexed, SubmitterListRequest> assembler) {
        Assert.checkNotNull(entity, "entity");
        SubmitterListRequest request;
        if (assembler != null) {
            request = assembler.toModel(entity);
        } else {
            request = new SubmitterListRequest();
        }
        request.submitterId = entity.getSubmitterId();
        request.name = entity.getName();
        request.status = entity.getStatus();
        request.vat = entity.getVat();
        request.address = entity.getAddress();
        request.country = entity.getCountry();
        request.phone = entity.getPhone();
        request.email = entity.getEmail();

        request.createdBy = entity.getCreatedBy();
        request.createdDate = entity.getCreatedDate();
        request.lastModifiedBy = entity.getLastModifiedBy();
        request.lastModifiedDate = entity.getLastModifiedDate();

        return request;
    }

}
