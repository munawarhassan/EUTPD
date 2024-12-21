package com.pmi.tpd.core.model.euceg;

import org.joda.time.DateTime;
import org.springframework.data.history.Revision;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentRevision {

    private int id;

    private int version;

    private String attachmentId;

    private boolean confidential;

    private String contentType;

    private String filename;

    private long size;

    private String createdBy;

    private DateTime createdDate;

    private String lastModifiedBy;

    private DateTime lastModifiedDate;

    public static AttachmentRevision fromRevision(final Revision<Integer, AttachmentEntity> from) {

        return AttachmentRevision.builder()
                .id(from.getRequiredRevisionNumber())
                .version(from.getEntity().getVersion())
                .attachmentId(from.getEntity().getAttachmentId())
                .confidential(from.getEntity().isConfidential())
                .contentType(from.getEntity().getContentType())
                .filename(from.getEntity().getFilename())
                .createdBy(from.getEntity().getCreatedBy())
                .createdDate(from.getEntity().getCreatedDate())
                .lastModifiedBy(from.getEntity().getLastModifiedBy())
                .lastModifiedDate(from.getEntity().getLastModifiedDate())
                .build();
    }
}
