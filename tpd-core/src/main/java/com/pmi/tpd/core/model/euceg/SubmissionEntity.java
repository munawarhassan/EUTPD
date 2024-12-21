package com.pmi.tpd.core.model.euceg;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.mutable.MutableInt;
import org.eu.ceg.EcigProductSubmission;
import org.eu.ceg.Submission;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProductSubmission;
import org.hibernate.Hibernate;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.listener.SubmissionIndexingListener;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.core.model.Converters.SubmissionTypeConverter;
import com.pmi.tpd.core.model.euceg.SubmissionEntity.SubmissionListener;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.database.jpa.JpaEntityListeners;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionVisitor;
import com.pmi.tpd.euceg.api.entity.ITransmitReceiptEntity;
import com.pmi.tpd.euceg.api.entity.PayloadType;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SendSubmissionType;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;
import com.pmi.tpd.euceg.api.entity.TransmitStatus;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "Submission")
@Table(name = SubmissionEntity.TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_3j79h83ehtuhasf882h2lu3fk", columnNames = { "payload_submission_id" }) },
        indexes = { @Index(name = "idx_submission_modified_date", columnList = "last_modified_date", unique = false) })
@Cacheable(false)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonDeserialize(builder = SubmissionEntity.Builder.class)
@EntityListeners(SubmissionListener.class)
@JpaEntityListeners(SubmissionIndexingListener.class)
public class SubmissionEntity extends BaseAuditingEntity<Long> implements ISubmissionEntity {

    /** generator identifier. */
    private static final String ID_GEN = "submissionIdGenerator";

    /** */
    public static final String GENERATOR_COLUMN_NAME = "submission_id";

    /** table name. */
    public static final String TABLE_NAME = "t_submission";

    /** */
    public static final String TABLE_NAME_EXPORTED_ATTACHMENT = "t_submission_exported_att";

    /** */
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME, //
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME, //
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME,
            pkColumnValue = GENERATOR_COLUMN_NAME, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
    private Long id;

    /** */
    @Size(max = 25)
    @Column(name = "product_id", length = 25, nullable = false)
    private String productId;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 25, nullable = false)
    private ProductType productType;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", length = 25, nullable = false)
    private SubmissionStatus submissionStatus;

    /** */
    @Column(name = "submission_type", nullable = false)
    @Convert(converter = SubmissionTypeConverter.class)
    private SubmissionTypeEnum submissionType;

    /** */
    @ManyToOne(optional = false, fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "product_number")
    private ProductEntity product;

    /** */
    @NotNull
    @Size(max = 20)
    @Column(name = "submitter_id", length = 20, nullable = false)
    private String submitterId;

    @Size(max = 255)
    @Column(name = "internal_product_number", length = 255, nullable = true)
    private String internalProductNumber;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "send_type", length = 25, nullable = false)
    private SendSubmissionType sendType;

    /** */
    @Transient
    private transient Submission submission;

    /** */
    @ElementCollection(fetch = FetchType.LAZY)
    @MapKeyColumn(name = "attachmentId")
    @Column(name = "att_exported")
    @CollectionTable(name = TABLE_NAME_EXPORTED_ATTACHMENT, joinColumns = @JoinColumn(name = "id"))
    private final Map<String, Boolean> attachements;

    /** */
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE },
            fetch = FetchType.LAZY, mappedBy = "submission", orphanRemoval = true)
    private final List<TransmitReceiptEntity> receipts;

    /** */
    @OneToOne(optional = false, fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "payload_submission_id")
    private PayloadEntity payloadSubmission;

    /** */
    @Column(name = "sent_by", length = 50, nullable = false)
    private String sentBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "pir_status", length = 25, nullable = true)
    private ProductPirStatus pirStatus;

    /**
     *
     */
    public SubmissionEntity() {
        this.attachements = Maps.newHashMap();
        this.receipts = Lists.newArrayList();
    }

    @Override
    public void initialize() {
        HibernateUtils.initialize(this.payloadSubmission);
        for (final ITransmitReceiptEntity receipt : getReceipts()) {
            // Don't use HibernateUtils.initialize here; it will cause a stack overflow
            Hibernate.initialize(receipt);
        }
        Hibernate.initialize(this.attachements);
    }

    /**
     * <p>
     * accept.
     * </p>
     *
     * @param visitor
     *            a {@link ISubmissionVisitor} object.
     * @param <T>
     *            a T object.
     * @return a T object.
     */
    @Override
    public <T> T accept(@Nonnull final ISubmissionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @return
     */
    @Override
    public String getProductId() {
        return productId;
    }

    /**
     * @return
     */
    @Override
    @JsonIgnore
    public ProductEntity getProduct() {
        return product;
    }

    void setProduct(final ProductEntity product) {
        this.product = product;
        if (!product.getSubmissions().contains(this)) {
            product.getSubmissions().add(this);
        }
    }

    /**
     * @return
     */
    @Override
    public ProductType getProductType() {
        return productType;
    }

    /**
     * @return
     */
    @Override
    public SendSubmissionType getSendType() {
        return sendType;
    }

    /**
     * @return
     */
    @Override
    public boolean isError() {
        return receipts.stream().anyMatch(TransmitReceiptEntity::isError);
    }

    /**
     * @return
     */
    @Override
    public SubmissionStatus getSubmissionStatus() {
        return submissionStatus;
    }

    /**
     * @return
     */
    @Override
    public SubmissionTypeEnum getSubmissionType() {
        return submissionType;
    }

    /**
     * @return
     */
    @Override
    public String getSubmitterId() {
        return submitterId;
    }

    /**
     * @return
     */
    @Override
    public String getInternalProductNumber() {
        return internalProductNumber;
    }

    /**
     * @return
     */
    @Override
    @JsonIgnore
    public String getXmlSubmission() {
        if (payloadSubmission == null) {
            return null;
        }
        return payloadSubmission.getData();
    }

    /**
     * @return
     */
    @Override
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "productType")
    @JsonSubTypes({ @Type(name = "TOBACCO", value = TobaccoProductSubmission.class),
            @Type(name = "ECIGARETTE", value = EcigProductSubmission.class) })
    @Transient
    @CheckForNull
    public Submission getSubmission() {
        if (getXmlSubmission() == null) {
            return null;
        }
        if (this.submission == null) {
            this.submission = Eucegs.unmarshal(getXmlSubmission());
        }
        return this.submission;
    }

    protected void setPayloadSubmission(final String xmlSubmission) {
        if (this.payloadSubmission == null) {
            this.payloadSubmission = PayloadEntity.builder().data(xmlSubmission).build();
        } else {
            this.payloadSubmission = this.payloadSubmission.copy().data(xmlSubmission).build();
        }
    }

    /**
     * @param submission
     */
    protected void setSubmission(final Submission submission) {
        this.submission = submission;
        if (submission == null) {
            setPayloadSubmission(null);
        } else {
            setPayloadSubmission(Eucegs.marshal(submission));
        }
    }

    /**
     * @return
     */
    @Override
    public Map<String, Boolean> getAttachments() {
        return attachements;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    public Set<String> getAttachedAttachments() {
        return Maps
                .filterValues(getAttachments(), Predicates.and(Predicates.notNull(), Predicates.equalTo(Boolean.TRUE)))
                .keySet();
    }

    /**
     * @return
     */
    @Override
    @JsonIgnore
    @Nonnull
    public List<TransmitReceiptEntity> getReceipts() {
        return receipts;
    }

    void addToReceipt(final TransmitReceiptEntity receipt) {
        if (receipts.contains(receipt)) {
            this.receipts.remove(receipt);
        }
        this.receipts.add(receipt);
        if (receipt.getSubmission() != this) {
            receipt.setSubmission(this);
        }
    }

    void removeFromReceipts(final TransmitReceiptEntity receipt) {
        if (receipt == null) {
            return;
        }
        receipts.remove(receipt);
    }

    /**
     * @param type
     * @return
     */
    @Override
    public List<TransmitReceiptEntity> getReceiptByType(@Nonnull final PayloadType type) {
        Assert.checkNotNull(type, "type");
        return getReceipts().stream().filter(input -> type.equals(input.getType())).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getProgress() {
        final int size = getReceipts().size();
        final MutableInt pending = new MutableInt();
        getReceipts().forEach((Consumer<ITransmitReceiptEntity>) input -> {
            if (TransmitStatus.PENDING.equals(input.getTransmitStatus())
                    || TransmitStatus.AWAITING.equals(input.getTransmitStatus())) {
                pending.increment();
            }

        });
        if (size == 0) {
            return 0.0f;
        }
        return (size - pending.floatValue()) / size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLatest() {
        return this.equals(this.product.getLastestSubmission());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLatestSubmitted() {
        return this.equals(this.product.getLastestSubmittedSubmission());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductPirStatus getPirStatus() {
        return this.pirStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSentBy() {
        return this.sentBy;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("productId", productId)
                .add("productType", productType)
                .add("product", product)
                .add("submitterId", submitterId)
                .add("receipts", receipts)
                .toString();
    }

    /**
     * Create new {@code Builder} instance and initialise with data of the current instance.
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
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties({ "transferStatus", "error", "sendable", "submissionStatus", "progress" })
    public static class Builder {

        /** */
        private final SubmissionEntity entity;

        /**
         *
         */
        private Builder() {
            this.entity = new SubmissionEntity();
        }

        /**
         * @param entity
         */
        public Builder(@Nonnull final SubmissionEntity entity) {
            this.entity = entity;
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder id(final Long value) {
            entity.id = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder productId(final String value) {
            entity.productId = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder productType(final ProductType value) {
            entity.productType = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder sendType(final SendSubmissionType value) {
            entity.sendType = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}
         */
        public Builder submissionStatus(final SubmissionStatus value) {
            entity.submissionStatus = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder submissionType(final SubmissionTypeEnum value) {
            entity.submissionType = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder product(final ProductEntity value) {
            entity.setProduct(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder submitterId(final String value) {
            entity.submitterId = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder internalProductNumber(final String value) {
            entity.internalProductNumber = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "productType")
        @JsonSubTypes({ @Type(name = "TOBACCO", value = TobaccoProductSubmission.class),
                @Type(name = "ECIGARETTE", value = EcigProductSubmission.class) })
        public Builder submission(final Submission value) {
            entity.setSubmission(value);
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder xmlSubmission(final String value) {
            entity.setPayloadSubmission(value);
            entity.submission = null;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder receipt(@Nullable final TransmitReceiptEntity value) {
            return receipts(value);
        }

        /**
         * @param values
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder receipts(@Nullable final Iterable<TransmitReceiptEntity> values) {
            if (values != null) {
                for (final TransmitReceiptEntity receiptEntity : values) {
                    entity.addToReceipt(receiptEntity);
                }
            }
            return self();
        }

        /**
         * @param value
         * @param values
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder receipts(@Nullable final TransmitReceiptEntity value,
            @Nullable final TransmitReceiptEntity... values) {
            entity.addToReceipt(value);
            if (values != null) {
                for (final TransmitReceiptEntity receiptEntity : values) {
                    entity.addToReceipt(receiptEntity);
                }
            }
            return self();
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

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder sentBy(final String value) {
            entity.sentBy = value;
            return self();
        }

        /**
         * @param value
         * @return Returns fluent {@link Builder}.
         */
        public Builder pirStatus(final ProductPirStatus value) {
            entity.pirStatus = value;
            return self();
        }

        /**
         * @return
         */
        @Nonnull
        public SubmissionEntity build() {
            return entity;
        }

        @Nonnull
        protected Builder self() {
            return this;
        }

    }

    public static class SubmissionListener {

        @PostLoad
        public void onPostLoad(final SubmissionEntity entity) {
            entity.submission = null;
        }

    }

}
