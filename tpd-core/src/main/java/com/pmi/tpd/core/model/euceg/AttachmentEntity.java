package com.pmi.tpd.core.model.euceg;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eu.ceg.Attachment;
import org.eu.ceg.AttachmentAction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.core.elasticsearch.listener.AttachmentIndexingListener;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.database.jpa.JpaEntityListeners;
import com.pmi.tpd.euceg.api.entity.IAttachmentEntity;
import com.pmi.tpd.euceg.api.entity.IStatusAttachment;
import com.pmi.tpd.euceg.api.entity.ISubmitterEntity;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "Attachment")
@Table(name = AttachmentEntity.TABLE_NAME)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonDeserialize(builder = AttachmentEntity.Builder.class)
@JpaEntityListeners(AttachmentIndexingListener.class)
@Audited
@AuditOverride(forClass = BaseAuditingEntity.class)
public class AttachmentEntity extends BaseAuditingEntity<String> implements IInitializable, IAttachmentEntity {

    /** table name. */
    public static final String TABLE_NAME = "t_attachment";

    /** */
    @Id
    @Size(max = 80)
    @Column(name = "attachment_id", length = 80, updatable = false)
    private String attachmentId;

    @Version
    @Column(name = "version")
    private int version = 1;

    /** */
    @NotNull
    @Size(max = 250)
    @Column(name = "filename", length = 250, nullable = false, updatable = true)
    private String filename;

    /** */
    @NotNull
    @Size(max = 50)
    @Column(name = "content_type", length = 50, nullable = false, updatable = false)
    private String contentType;

    /** */
    @NotNull
    @Column(name = "confidential", nullable = false)
    private boolean confidential = true;

    /** */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE },
            fetch = FetchType.EAGER, mappedBy = "id.attachmentId", orphanRemoval = true)
    @NotAudited
    private final Set<StatusAttachment> status;

    /**
     *
     */
    public AttachmentEntity() {
        status = Sets.newHashSet();
    }

    @Override
    public void initialize() {

    }

    @Override
    public String getId() {
        return getAttachmentId();
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

    /**
     * @return Returns the identifier.
     * @see #getId()
     */
    @Override
    public String getAttachmentId() {
        return attachmentId;
    }

    /**
     * @return Returns the file name.
     */
    @Override
    public String getFilename() {
        return filename;
    }

    /**
     * Gets the {@link AttachmentAction} for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns a optional value representing the {@link AttachmentAction} for the specific {@code submitter}.
     */
    @Override
    public AttachmentAction getAction(@Nonnull final ISubmitterEntity submitter) {
        return this.status.stream()
                .filter(StatusAttachment.bySubmitter(submitter))
                .findFirst()
                .map(StatusAttachment::getAction)
                .orElse(AttachmentAction.CREATE);
    }

    /**
     * @return Returns the content type.
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * @return Returns {@code true} whether is flagged as confidential, {@code false} otherwise.
     */
    @Override
    public boolean isConfidential() {
        return confidential;
    }

    /**
     * @return Returns the status.
     */
    @Override
    public Set<? extends IStatusAttachment> getStatus() {
        return status;
    }

    @Override
    @Nullable
    public Optional<IStatusAttachment> getDefaultStatus() {
        final Set<? extends IStatusAttachment> status = getStatus();
        return Optional.ofNullable(Iterables.getFirst(status, null));
    }

    void addToStatus(final IStatusAttachment status) {
        if (this.status.contains(status)) {
            this.status.remove(status);
        }
        this.status.add((StatusAttachment) status);
    }

    void removeFromStatus(final IStatusAttachment status) {
        if (status == null) {
            return;
        }
        this.status.remove(status);
    }

    /**
     * Gets the {@link StatusAttachment} for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns a optional value representing the {@link StatusAttachment} for the specific {@code submitter}.
     */
    @Override
    public Optional<IStatusAttachment> getStatus(@Nonnull final ISubmitterEntity submitter) {
        return this.status.stream()
                .filter(StatusAttachment.bySubmitter(submitter))
                .map(s -> IStatusAttachment.class.cast(s))
                .findFirst();
    }

    /**
     * Gets the indicating whether is sent for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns {@code true} whether is sent for the specific {@code submitter}, {@code false} otherwise.
     */
    @Override
    public boolean isSent(@Nonnull final ISubmitterEntity submitter) {
        return getStatus(submitter).map(IStatusAttachment::isSent).orElse(false);
    }

    /**
     * Gets the indicating whether is sending for the specific {@code submitter}.
     *
     * @param submitter
     *                  the submitter to user.
     * @return Returns {@code true} whether is sending for the specific {@code submitter}, {@code false} otherwise.
     */
    @Override
    public boolean isSending(final @Nonnull ISubmitterEntity submitter) {
        return getStatus(submitter).map(IStatusAttachment::isSending).orElse(false);
    }

    /**
     * Create a new instance of {@link org.eu.ceg.Attachment}.
     *
     * @param submitter
     *                  a submitter to use.
     * @param content
     *                  a content.
     * @return Create a new instance of {@link org.eu.ceg.Attachment} representing a EUCEG attachment.
     */
    @Nonnull
    public Attachment toAttachment(@Nonnull final ISubmitterEntity submitter, @Nonnull final File content) {
        return new Attachment().withAttachmentID(attachmentId)
                .withConfidential(confidential)
                .withContentType(contentType)
                .withFilename(filename)
                .withContent(new DataHandler(new FileDataSource(content)))
                .withAction(getAction(submitter));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("attachmentId", attachmentId)
                .add("filename", filename)
                .add("contentType", contentType)
                .add("confidential", confidential)
                .toString();
    }

    /**
     * Create new {@code Builder} instance and initialise with data of the current instance.
     *
     * @return Returns new instance {@link AttachmentEntity.Builder}.
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
     * @return a {@link AttachmentEntity.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {

        /** */
        private final AttachmentEntity entity;

        /**
         *
         */
        private Builder() {
            entity = new AttachmentEntity();
        }

        /**
         *
         */
        private Builder(@Nonnull final AttachmentEntity attachment) {
            this.entity = attachment;
        }

        /**
         * @param attachmentId
         *                     a identifier
         * @return Returns fluent {@link Builder}.
         */
        public Builder attachmentId(@Nonnull final String attachmentId) {
            entity.attachmentId = attachmentId;
            return self();
        }

        /**
         * @param filename
         *                 a file name.
         * @return Returns fluent {@link Builder}.
         */
        public Builder filename(@Nonnull final String filename) {
            entity.filename = filename;
            return self();
        }

        /**
         * @param contentType
         *                    a content type.
         * @return Returns fluent {@link Builder}.
         */
        public Builder contentType(@Nonnull final String contentType) {
            entity.contentType = contentType;
            return self();
        }

        /**
         * @param confidential
         *                     {@code true} if confidential.
         * @return Returns fluent {@link Builder}.
         */
        public Builder confidential(final boolean confidential) {
            entity.confidential = confidential;
            return self();
        }

        /**
         * @param values
         *               list of status
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder status(@Nullable final Iterable<IStatusAttachment> values) {
            if (values != null) {
                for (final IStatusAttachment value : values) {
                    entity.addToStatus(value);
                }
            }
            return self();
        }

        /**
         * @param value
         *               the first status to add
         * @param values
         *               a varargs array containing 0 or more status to add after the first
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder status(@Nullable final IStatusAttachment value, @Nullable final IStatusAttachment... values) {
            entity.addToStatus(value);
            if (values != null) {
                for (final IStatusAttachment status : values) {
                    entity.addToStatus(status);
                }
            }
            return self();
        }

        /**
         * @return Returns a list of status.
         */
        public Set<StatusAttachment> status() {
            return entity.status;
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder createdBy(final String value) {
            entity.setCreatedBy(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder lastModifiedBy(final String value) {
            entity.setLastModifiedBy(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder createdDate(final DateTime value) {
            entity.setCreatedDate(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder lastModifiedDate(final DateTime value) {
            entity.setLastModifiedDate(value);
            return self();
        }

        @Nonnull
        public AttachmentEntity build() {
            return entity;
        }

        @Nonnull
        protected Builder self() {
            return this;
        }

    }

}
