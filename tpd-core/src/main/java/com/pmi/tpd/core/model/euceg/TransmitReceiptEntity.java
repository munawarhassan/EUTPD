package com.pmi.tpd.core.model.euceg;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.eu.ceg.AbstractAppResponse;
import org.eu.ceg.AppResponse;
import org.eu.ceg.AttachmentResponse;
import org.eu.ceg.ErrorResponse;
import org.eu.ceg.ResponseStatus;
import org.eu.ceg.SubmissionResponse;
import org.eu.ceg.SubmitterDetailsResponse;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.core.model.euceg.TransmitReceiptEntity.ReceiptListener;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.entity.IReceiptVisitor;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "TransmitReceipt")
@Table(name = TransmitReceiptEntity.TABLE_NAME)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties({ "error", "responseType" })
@EntityListeners({ ReceiptListener.class })
public class TransmitReceiptEntity extends BaseAuditingEntity<Long> implements IInitializable, ITransmitReceiptEntity {

    /** table name. */
    public static final String TABLE_NAME = "t_transmit_receipt";

    /** */
    private static final String ID_GEN = "receiptIdGenerator";

    /** */
    public static final String GENERATOR_COLUMN_NAME = "receipt_id";

    /** Generated user id. */
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME, //
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME, //
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME,
            pkColumnValue = GENERATOR_COLUMN_NAME, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
    private Long id;

    /** */
    @ManyToOne(optional = false, fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "submission_id", nullable = false)
    private SubmissionEntity submission;

    /** */
    @Size(max = 250)
    @Column(name = "message_id", length = 250, nullable = false)
    private String messageId;

    /** */
    @Size(max = 250)
    @Column(name = "response_message_id", length = 250, nullable = true)
    private String responseMessageId;

    /** */
    @Size(max = 250)
    @Column(name = "payload_name", length = 250, nullable = false)
    private String name;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "payload_type", length = 25, nullable = true)
    private PayloadType type;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "transmit_status", length = 25, nullable = false)
    private TransmitStatus transmitStatus;

    /** */
    @Column(name = "response_error", nullable = true)
    private boolean error;

    /** */
    @Lob
    @Column(name = "xml_response", length = 32768, nullable = true)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.TextType")
    private String xmlResponse;

    /** */
    @Transient
    private transient AppResponse response;

    /**
     *
     */
    public TransmitReceiptEntity() {
    }

    @Override
    public void initialize() {
        HibernateUtils.initialize(this.submission);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ITransmitReceiptEntity, U> U accept(final IReceiptVisitor<T, U> visitor) {
        return visitor.visit((T) this);
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return
     */
    @Override
    public PayloadType getType() {
        return type;
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    @Override
    @Nullable
    public PayloadType getResponseType() {
        return PayloadType.fromPayload(getResponse());
    }

    /**
     * @return
     */
    @Override
    public boolean isError() {
        return error;
    }

    /**
     * @return
     */
    @Override
    @JsonIgnore
    public @Nonnull SubmissionEntity getSubmission() {
        return submission;
    }

    void setSubmission(final SubmissionEntity submission) {
        this.submission = submission;
        if (!submission.getReceipts().contains(this)) {
            submission.getReceipts().add(this);
        }
    }

    /**
     * @return
     */
    @Override
    public String getMessageId() {
        return messageId;
    }

    /**
     * @return
     */
    @Override
    public String getResponseMessageId() {
        return responseMessageId;
    }

    /**
     * @return
     */
    @Override
    public TransmitStatus getTransmitStatus() {
        return transmitStatus;
    }

    /**
     * @return
     */
    @Override
    @JsonIgnore
    public String getXmlResponse() {
        return xmlResponse;
    }

    /**
     * @return
     */
    @Override
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({ @Type(name = "SUBMITER_DETAILS", value = SubmitterDetailsResponse.class),
            @Type(name = "ATTACHMENT", value = AttachmentResponse.class),
            @Type(name = "SUBMISSION", value = SubmissionResponse.class),
            @Type(name = "ERROR_RESPONSE", value = ErrorResponse.class) })
    @Transient
    public AppResponse getResponse() {
        if (Strings.isNullOrEmpty(xmlResponse)) {
            return null;
        }
        if (response != null) {
            return response;
        }
        response = Eucegs.unmarshal(xmlResponse);
        return response;
    }

    /**
     * @param response
     */
    protected void setResponse(final AppResponse response) {
        this.response = response;
        this.xmlResponse = null;
        if (response != null) {
            xmlResponse = Eucegs.marshal(response);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id)
                .add("type", type)
                .add("name", name)
                .add("messageId", messageId)
                .add("responseMessageId", responseMessageId)
                .add("transmitStatus", transmitStatus)
                .toString();
    }

    /**
     * Create new {@code Builder} instance and initialise with data of the current instance.
     *
     * @return Returns new instance {@link TransmitReceiptEntity.Builder}.
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
     * @return a {@link TransmitReceiptEntity.Builder} object.
     */
    protected static Builder builder() {
        return new Builder();
    }

    /**
     * @param submission
     * @param payloadType
     * @param name
     * @param messageId
     * @param status
     * @return
     */
    public static TransmitReceiptEntity create(@Nonnull final SubmissionEntity submission,
        @Nonnull final PayloadType payloadType,
        final String name,
        @Nonnull final String messageId,
        @Nonnull final TransmitStatus status) {
        Assert.checkNotNull(submission, "submission");
        Assert.checkNotNull(payloadType, "payloadType");
        Assert.checkNotNull(messageId, "messageId");
        Assert.checkNotNull(status, "status");
        Assert.checkHasText(name, "name");
        return TransmitReceiptEntity.builder()
                .submission(submission)
                .messageId(messageId)
                .type(payloadType)
                .name(name)
                .transmitStatus(status)
                .build();
    }

    /**
     * @return
     */
    boolean _isError() {
        final AppResponse response = getResponse();
        final TransmitStatus status = getTransmitStatus();
        if (status != null) {
            switch (status) {
                case DELETED:
                case REJECTED:
                    return true;
                default:
                    break;
            }
        }
        if (response == null) {
            return false;
        }
        if (response instanceof ErrorResponse) {
            return true;
        }
        if (response instanceof AbstractAppResponse) {
            final AbstractAppResponse r = (AbstractAppResponse) response;
            return ResponseStatus.ERROR.equals(r.getStatus());
        }
        return false;
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class Builder {

        /** */
        @Nonnull
        private final TransmitReceiptEntity entity;

        /**
         *
         */
        private Builder() {
            this.entity = new TransmitReceiptEntity();
        }

        /**
         * @param receipt
         */
        private Builder(@Nonnull final TransmitReceiptEntity receipt) {
            this.entity = receipt;
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder type(final PayloadType value) {
            entity.type = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder submission(final SubmissionEntity value) {
            entity.setSubmission(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder messageId(final String value) {
            entity.messageId = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder responseMessageId(final String value) {
            entity.responseMessageId = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder name(final String value) {
            entity.name = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder transmitStatus(final TransmitStatus value) {
            entity.transmitStatus = value;
            return self();
        }

        /**
         * @param response
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder response(final AppResponse response) {
            entity.setResponse(response);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder createdBy(final String value) {
            entity.setCreatedBy(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder lastModifiedBy(final String value) {
            entity.setLastModifiedBy(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder createdDate(final DateTime value) {
            entity.setCreatedDate(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder lastModifiedDate(final DateTime value) {
            entity.setLastModifiedDate(value);
            return self();
        }

        /**
         * @return
         */
        @Nonnull
        public TransmitReceiptEntity build() {
            entity.error = entity._isError();
            return entity;
        }

        /**
         * @return
         */
        @Nonnull
        protected Builder self() {
            return this;
        }

    }

    public static class ReceiptListener {

        @PostLoad
        public void onPostLoad(final TransmitReceiptEntity receipt) {
            receipt.response = null;
        }

    }

}
