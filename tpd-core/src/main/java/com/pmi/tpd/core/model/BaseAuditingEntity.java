package com.pmi.tpd.core.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.pmi.tpd.api.model.IAuditEntity;
import com.pmi.tpd.core.model.Converters.JodaDateTimeConverter;
import com.pmi.tpd.database.support.Identifiable;

/**
 * Base class for entities which will hold definitions for created, last modified by and created, last modified by date.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditingEntity<K extends Serializable> extends Identifiable<K> implements IAuditEntity {

    /** */
    @CreatedBy
    @NotNull
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    private String createdBy;

    /** */
    @CreatedDate
    @NotNull
    @Column(name = "created_date", nullable = false)
    @Convert(converter = JodaDateTimeConverter.class)
    private DateTime createdDate = new DateTime();

    /** */
    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    /** */
    @LastModifiedDate
    @Column(name = "last_modified_date")
    @Convert(converter = JodaDateTimeConverter.class)
    private DateTime lastModifiedDate = new DateTime();

    public BaseAuditingEntity() {
    }

    public BaseAuditingEntity(final AbstractAuditedEntityBuilder<K, ? extends BaseAuditingEntity<K>, ?> builder) {
        this.setCreatedBy(builder.createdBy());
        this.setCreatedDate(builder.createdDate());
        this.setLastModifiedBy(builder.lastModifiedBy());
        this.setLastModifiedDate(builder.lastModifiedDate());
    }

    /**
     * <p>
     * Getter for the field <code>createdBy</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * <p>
     * Setter for the field <code>createdBy</code>.
     * </p>
     *
     * @param createdBy
     *            a {@link java.lang.String} object.
     */
    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * <p>
     * Getter for the field <code>createdDate</code>.
     * </p>
     *
     * @return a {@link DateTime} object.
     */
    @Override
    public DateTime getCreatedDate() {
        return createdDate;
    }

    /**
     * <p>
     * Setter for the field <code>createdDate</code>.
     * </p>
     *
     * @param createdDate
     *            a {@link DateTime} object.
     */
    public void setCreatedDate(final DateTime createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * <p>
     * Getter for the field <code>lastModifiedBy</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * <p>
     * Setter for the field <code>lastModifiedBy</code>.
     * </p>
     *
     * @param lastModifiedBy
     *            a {@link java.lang.String} object.
     */
    public void setLastModifiedBy(final String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * <p>
     * Getter for the field <code>lastModifiedDate</code>.
     * </p>
     *
     * @return a {@link DateTime} object.
     */
    @Override
    public DateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * <p>
     * Setter for the field <code>lastModifiedDate</code>.
     * </p>
     *
     * @param lastModifiedDate
     *            a {@link DateTime} object.
     */
    public void setLastModifiedDate(final DateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
