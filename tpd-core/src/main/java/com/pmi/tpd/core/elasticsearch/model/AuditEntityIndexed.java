package com.pmi.tpd.core.elasticsearch.model;

import org.joda.time.DateTime;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.pmi.tpd.api.model.IAuditEntity;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public abstract class AuditEntityIndexed {

    /** */
    @Field(type = FieldType.Date, store = true)
    private DateTime lastModifiedDate;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String createdBy;

    /** */
    @Field(type = FieldType.Date, store = true)
    private DateTime createdDate;

    /** */
    @Field(type = FieldType.Keyword, store = true)
    private String lastModifiedBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void audit(final IAuditEntity auditingEntity) {
        this.createdBy = auditingEntity.getCreatedBy();
        this.createdDate = auditingEntity.getCreatedDate();
        this.lastModifiedBy = auditingEntity.getLastModifiedBy();
        this.lastModifiedDate = auditingEntity.getLastModifiedDate();
    }
}
