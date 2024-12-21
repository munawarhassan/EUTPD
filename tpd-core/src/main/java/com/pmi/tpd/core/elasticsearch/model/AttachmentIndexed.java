package com.pmi.tpd.core.elasticsearch.model;

import java.util.List;
import java.util.Optional;

import org.eu.ceg.AttachmentAction;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.IStatusAttachment;

import lombok.Getter;

/**
 * @author Christophe Friederich
 * @since 2.2
 */
@Document(indexName = "attachments_v2")
@Setting(settingPath = "elasticsearch-settings.json", shards = 1, replicas = 0)
@Getter
public class AttachmentIndexed extends AuditEntityIndexed implements IIdentityEntity<String> {

    @Id
    private String id;

    @Field(type = FieldType.Text, store = true, analyzer = "lowercase_hyphen", searchAnalyzer = "lowercase_hyphen",
            fielddata = true)
    private String filename;

    @Field(type = FieldType.Boolean, store = true)
    private boolean confidential;

    @Field(type = FieldType.Keyword, store = true)
    private String contentType;

    @Field(type = FieldType.Keyword, store = true)
    private AttachmentSendStatus status;

    @Field(type = FieldType.Keyword, store = true)
    private AttachmentAction action;

    /**
     * Convert a list of {@link ProductEntity} to list of {@link ProductIndexed}.
     *
     * @param entities
     *            a list of entities.
     * @return Returns a list of {@link ProductIndexed}.
     */
    public static List<AttachmentIndexed> map(final List<AttachmentEntity> entities) {
        return Lists.transform(entities, AttachmentIndexed::from);
    }

    /**
     * Convert a page of {@link ProductEntity} to page of {@link ProductIndexed}.
     *
     * @param page
     *            a page.
     * @return Returns a page of {@link ProductIndexed}.
     */
    public static Page<AttachmentIndexed> map(final Page<AttachmentEntity> page) {
        return page.map(AttachmentIndexed::from);
    }

    public static AttachmentIndexed from(final AttachmentEntity entity) {
        final AttachmentIndexed attachment = new AttachmentIndexed();
        attachment.id = entity.getId();
        attachment.filename = entity.getFilename();
        final Optional<IStatusAttachment> status = entity.getDefaultStatus();
        if (status.isPresent()) {
            final IStatusAttachment s = status.get();
            attachment.status = s.getSendStatus();
            attachment.action = s.getAction();
        } else {
            attachment.status = AttachmentSendStatus.NO_SEND;
            attachment.action = AttachmentAction.CREATE;
        }
        attachment.confidential = entity.isConfidential();
        attachment.contentType = entity.getContentType();
        attachment.audit(entity);
        return attachment;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this) //
                .add("id", id)
                .add("filename", filename)
                .add("status", status)
                .add("action", action)
                .toString();
    }

}
