package com.pmi.tpd.core.model.euceg;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eu.ceg.Submitter;
import org.eu.ceg.SubmitterDetails;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.core.elasticsearch.listener.SubmitterIndexingListener;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.core.model.euceg.SubmitterEntity.SubmitterListener;
import com.pmi.tpd.database.jpa.JpaEntityListeners;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;
import com.pmi.tpd.euceg.api.entity.SubmitterStatus;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "Submitter")
@Table(name = SubmitterEntity.TABLE_NAME)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonDeserialize(builder = SubmitterEntity.Builder.class)
@EntityListeners(SubmitterListener.class)
@JpaEntityListeners(SubmitterIndexingListener.class)
@Audited
@AuditOverride(forClass = BaseAuditingEntity.class)
public class SubmitterEntity extends BaseAuditingEntity<String> implements IInitializable, ISubmitterEntity {

    /** table name. */
    public static final String TABLE_NAME = "t_submitter";

    /** */
    @Id
    @Size(max = 10)
    @Column(name = "submitter_id", length = 10)
    private String submitterId;

    /** */
    @NotNull
    @Size(max = 250)
    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Version
    @Column(name = "version")
    private int version = 1;

    /** */
    @NotNull
    @Lob
    @Column(name = "xml_submitter", length = 1024000, nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    private String xmlSubmitter;

    /** */
    @NotNull
    @Lob
    @Column(name = "xml_submitter_detail", length = 1024000, nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    private String xmlSubmitterDetail;

    @Enumerated(EnumType.STRING)
    @Column(name = "submitter_status", length = 50, nullable = false)
    private SubmitterStatus status;

    /** */
    @Transient
    private transient SubmitterDetails details;

    /** */
    @Transient
    private transient Submitter submitter;

    /**
     * Create new instance.
     */
    public SubmitterEntity() {

    }

    @Override
    public void initialize() {

    }

    @Override
    public String getId() {
        return submitterId;
    }

    /**
     * @return Returns the current version of attachment.
     * @since 2.4
     */
    @Override
    public int getVersion() {
        return version;
    }

    /**
     * NOT REMOVE
     * <p>
     * note: workaround on eclipse auto format
     * </p>
     *
     * @since 2.4
     */
    protected void setVersion(final int version) {
        this.version = version;
    }

    @Override
    public String getSubmitterId() {
        return submitterId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public SubmitterStatus getStatus() {
        return status;
    }

    @Override
    @JsonIgnore
    public String getXmlSubmitter() {
        return xmlSubmitter;
    }

    @Override
    @JsonIgnore
    public String getXmlSubmitterDetail() {
        return xmlSubmitterDetail;
    }

    @Override
    @JsonProperty(value = "details")
    @Transient
    public SubmitterDetails getSubmitterDetails() {
        if (xmlSubmitterDetail == null) {
            return null;
        }
        if (details == null) {
            details = Eucegs.unmarshal(new String(xmlSubmitterDetail));
        }
        return details;
    }

    protected void setSubmitterDetails(final SubmitterDetails details) {
        this.details = details;
        if (details == null) {
            xmlSubmitterDetail = null;
        } else {
            final String str = Eucegs.marshal(details);
            if (str != null) {
                xmlSubmitterDetail = str;
            } else {
                xmlSubmitterDetail = null;
            }
        }
    }

    @Override
    @Transient
    public Submitter getSubmitter() {
        if (xmlSubmitter == null) {
            return null;
        }
        if (submitter == null) {
            submitter = Eucegs.unmarshal(xmlSubmitter, Submitter.class);
        }
        return submitter;
    }

    protected void setSubmitter(final Submitter submitter) {
        this.submitter = submitter;
        final String str = Eucegs.marshal(Eucegs.wrap(submitter));
        if (str != null) {
            xmlSubmitter = str;
        } else {
            xmlSubmitter = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("submitterId", submitterId)
                .add("name", name)
                .add("status", status)
                .toString();
    }

    /**
     * Create new {@code Builder} instance and initialize with data of the current instance.
     *
     * @return Returns new instance {@link com.pmi.tpd.core.model.user.UserEntity.Builder}.
     */
    @Nonnull
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.model.user.UserEntity.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {

        /** */
        private final SubmitterEntity entity;

        /**
         *
         */
        private Builder() {
            this.entity = new SubmitterEntity();
        }

        /**
         * @param submitter
         */
        public Builder(@Nonnull final SubmitterEntity submitter) {
            this.entity = submitter;
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        @JsonSetter("submitterId")
        public Builder submitterId(final String value) {
            this.entity.submitterId = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder name(final String value) {
            this.entity.name = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder status(final SubmitterStatus value) {
            this.entity.status = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder submitter(final Submitter value) {
            this.entity.setSubmitter(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder details(final SubmitterDetails value) {
            this.entity.setSubmitterDetails(value);
            return self();
        }

        @Nonnull
        public Builder lastModifiedDate(final DateTime date) {
            this.entity.setLastModifiedDate(date);
            return self();
        }

        @Nonnull
        public Builder lastModifiedBy(final String by) {
            this.entity.setLastModifiedBy(by);
            return self();
        }

        /**
         * @return
         */
        @Nonnull
        public SubmitterEntity build() {
            return this.entity;
        }

        /**
         * @return
         */
        @Nonnull
        protected Builder self() {
            return this;
        }

    }

    /**
     * @author Christophe Friederich
     */
    public static class SubmitterListener {

        @PostLoad
        public void onPostLoad(final SubmitterEntity entity) {
            entity.submitter = null;
            entity.details = null;
        }

    }

}
