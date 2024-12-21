package com.pmi.tpd.core.model;

import java.io.Serializable;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;

import com.pmi.tpd.api.model.AbstractEntityBuilder;

/**
 * <p>
 * Abstract AbstractEntityBuilder class.
 * </p>
 *
 * @author Christophe Friederich
 * @param <K>
 * @param <T>
 * @param <B>
 * @since 1.0
 */
public abstract class AbstractAuditedEntityBuilder<K extends Serializable, T extends BaseAuditingEntity<K>, //
        B extends AbstractAuditedEntityBuilder<K, T, B>> extends AbstractEntityBuilder<K, T, B> {

    /** */
    protected String createdBy;

    /** */
    protected DateTime createdDate;

    /** */
    protected String lastModifiedBy;

    /** */
    protected DateTime lastModifiedDate;

    /**
     *
     */
    public AbstractAuditedEntityBuilder() {
        this.createdBy = null;
        this.createdDate = null;
        this.lastModifiedBy = null;
        this.lastModifiedDate = null;
    }

    /**
     * @return
     */
    public String createdBy() {
        return createdBy;
    }

    /**
     * @return
     */
    public DateTime createdDate() {
        return createdDate;
    }

    /**
     * @return
     */
    public String lastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @return
     */
    public DateTime lastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * @param value
     * @return
     */
    public B createdBy(final String value) {
        createdBy = value;
        return self();
    }

    /**
     * @param value
     * @return
     */
    public B lastModifiedBy(final String value) {
        lastModifiedBy = value;
        return self();
    }

    /**
     * @param value
     * @return
     */
    public B createdDate(final DateTime value) {
        createdDate = value;
        return self();
    }

    /**
     * @param value
     * @return
     */
    public B lastModifiedDate(final DateTime value) {
        lastModifiedDate = value;
        return self();
    }

    /**
     * @param obj
     */
    public AbstractAuditedEntityBuilder(@Nonnull final T obj) {
        super(obj);

        this.createdBy = obj.getCreatedBy();
        this.createdDate = obj.getCreatedDate();
        this.lastModifiedBy = obj.getLastModifiedBy();
        this.lastModifiedDate = obj.getLastModifiedDate();
    }
}
