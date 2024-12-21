package com.pmi.tpd.core.model.euceg;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.EcigProductTypeEnum;
import org.eu.ceg.Product;
import org.eu.ceg.SubmissionTypeEnum;
import org.eu.ceg.TobaccoProduct;
import org.eu.ceg.TobaccoProductTypeEnum;
import org.glassfish.jersey.internal.guava.Sets;
import org.hibernate.Hibernate;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.api.util.FluentIterable;
import com.pmi.tpd.core.elasticsearch.listener.ProductIndexingListener;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.core.model.euceg.ProductEntity.ProductListener;
import com.pmi.tpd.database.hibernate.HibernateUtils;
import com.pmi.tpd.database.jpa.JpaEntityListeners;
import com.pmi.tpd.euceg.api.Eucegs;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.IProductEntity;
import com.pmi.tpd.euceg.api.entity.IProductVisitor;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.ISubmissionVisitor;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.ProductStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Audited()
@AuditOverride(forClass = BaseAuditingEntity.class)
@Entity(name = "Product")
@Table(name = ProductEntity.TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_o6cx299gd3803m9hhe7db1pef", columnNames = { "payload_product_id" }) },
        indexes = { @Index(name = "idx_product_modified_date", columnList = "last_modified_date", unique = false) })
@Cacheable(false)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonDeserialize(builder = ProductEntity.Builder.class)
@EntityListeners(ProductListener.class)
@JpaEntityListeners(ProductIndexingListener.class)
public class ProductEntity extends BaseAuditingEntity<String> implements IInitializable, IProductEntity {

    /** table name. */
    public static final String TABLE_NAME = "t_product";

    public static final String TABLE_NAME_ATTACHMENT = "t_product_attachment";

    /** */
    @Id
    @Size(max = 255)
    @Column(name = "product_number", length = 255, nullable = false)
    private String productNumber;

    @Size(max = 255)
    @Column(name = "internal_product_number", length = 255, nullable = true)
    private String internalProductNumber;

    /**
     * @since 2.4
     */
    @Version
    @Column(name = "version")
    private int version = 1;

    /**
     * @since 1.7
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(name = "previous_product_number", nullable = true, updatable = false)
    private ProductEntity child;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", length = 50, nullable = false)
    private ProductType productType;

    @Column(name = "type", nullable = false)
    private int type;

    /** */
    @NotNull
    @Size(max = 20)
    @Column(name = "preferred_submitter_id", length = 20, nullable = false)
    @NotAudited
    private String submitterId;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_submission_type", length = 50, nullable = true)
    @NotAudited
    private SubmissionTypeEnum preferredSubmissionType;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "product_pir_status", length = 50, nullable = false)
    private ProductPirStatus pirStatus;

    /** */
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", length = 50, nullable = false)
    private ProductStatus status;

    /** */
    @OneToOne(optional = false, fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE })
    @JoinColumn(name = "payload_product_id")
    private PayloadEntity payloadProduct;

    /** */
    @NotAudited
    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.REMOVE },
            fetch = FetchType.LAZY, mappedBy = "product", orphanRemoval = true)
    @Nonnull
    private final List<SubmissionEntity> submissions;

    /** */
    @NotAudited
    @Size(max = 1000)
    @Column(name = "preferred_general_comment", length = 1000, nullable = true)
    private String preferredGeneralComment;

    /** */
    @Transient
    private transient Product product;

    /** */
    @Size(max = 120)
    @Column(name = "source_file_name", length = 120, nullable = true)
    private String sourceFilename;

    @NotAudited
    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "attachment_id", length = 80, nullable = false)
    @CollectionTable(name = TABLE_NAME_ATTACHMENT, joinColumns = @JoinColumn(name = "product_number"))
    @Nonnull
    private final Set<String> attachments;

    /**
     * Create a new instance of {@link ProductEntity}.
     */
    public ProductEntity() {
        this.submissions = Lists.newArrayList();
        this.attachments = Sets.newHashSet();
    }

    @Override
    public void initialize() {
        Hibernate.initialize(this.payloadProduct);
        for (final ISubmissionEntity submission : getSubmissions()) {
            HibernateUtils.initialize(submission);
        }
        Hibernate.initialize(this.attachments);
        Hibernate.initialize(this.child);
    }

    /**
     * <p>
     * accept.
     * </p>
     *
     * @param visitor
     *                a {@link ISubmissionVisitor} object.
     * @param <T>
     *                a T object.
     * @return a T object.
     */
    @Override
    public <T> T accept(@Nonnull final IProductVisitor<T> visitor) {
        return visitor.visit(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return productNumber;
    }

    public String getInternalProductNumber() {
        return internalProductNumber;
    }

    /**
     * @return Returns the current version of product.
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
     * Gets the indicating whether the {@link ProductEntity} instance is read only.
     *
     * @return Returns {@code true} whether the {@link ProductEntity} instance is read only, otherwise {@code false}.
     */
    @Override
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Gets the indicating whether the product is sendable.
     * <p>
     * A product can be send if:
     * </p>
     * <ul>
     * <li>product is {@link ProductStatus#VALID VALID}</li>
     * <li>latest submission doesn't exist, it is {@link SubmissionStatus#NOT_SEND send}</li>
     * <li>or latest submission has been {@link SubmissionStatus#CANCELLED cancelled}</li>
     * </ul>
     *
     * @return Returns {@code true} whether the product is sendable, otherwise {@code false}.
     */
    @Override
    public boolean isSendable() {
        if (ProductStatus.DRAFT.equals(getStatus()) || ProductStatus.IMPORTED.equals(getStatus())) {
            return false;
        }
        final boolean sendable = ProductStatus.VALID.equals(getStatus());
        final SubmissionEntity latest = getLastestSubmission();
        // check if latest submission doesn't exist
        if (latest == null) {
            return sendable;
        }

        // latest submission is not yet send
        if (SubmissionStatus.NOT_SEND.equals(latest.getSubmissionStatus())) {
            return false;
        }
        if (sendable) {
            return true;
        }

        if (SubmissionStatus.CANCELLED.equals(latest.getSubmissionStatus())) {
            return true;
        }

        return latest.isError();
    }

    /**
     * Gets the product number.
     *
     * @return Returns a {@link String} representing the product number.
     */
    @Override
    public String getProductNumber() {
        return productNumber;
    }

    /**
     * @return Returns a child.
     * @since 1.7
     */
    @Override
    @JsonIgnore
    public ProductEntity getChild() {
        return child;
    }

    /**
     * Gets the previous product number.
     *
     * @return Returns a string representing the the previous product number.
     */
    @Override
    @Nullable
    public String getPreviousProductNumber() {
        if (child != null) {
            return child.getProductNumber();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductType getProductType() {
        return productType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductStatus getStatus() {
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductPirStatus getPirStatus() {
        return pirStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonProperty("submissionType")
    public SubmissionTypeEnum getPreferredSubmissionType() {
        return preferredSubmissionType;
    }

    /**
     * @return Returns a {@link String} representing the submitter ID.
     */
    @Override
    @JsonProperty("submitterId")
    public String getSubmitterId() {
        return submitterId;
    }

    /**
     * @return Returns a {@link String} representing the possible general comment.
     */
    @Override
    @JsonProperty("generalComment")
    public String getPreferredGeneralComment() {
        return preferredGeneralComment;
    }

    /**
     * @return Returns a {@link String} representing the source file name, if any.
     */
    @Override
    public String getSourceFilename() {
        return sourceFilename;
    }

    /**
     * @return Returns the {@link PayloadEntity} associated to this product.
     */
    @Override
    @JsonIgnore
    public PayloadEntity getPayloadProduct() {
        return payloadProduct;
    }

    /**
     * @return Returns a {@link String} representing the xml representation of product.
     * @see #getProduct().
     */
    @Override
    @JsonIgnore
    public String getXmlProduct() {
        if (payloadProduct == null) {
            return null;
        }
        return payloadProduct.getData();
    }

    /**
     * @return Returns the EUCEG {@link Product} associated to this product.
     */
    @Override
    @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "productType")
    @JsonSubTypes({ @Type(name = "TOBACCO", value = TobaccoProduct.class),
            @Type(name = "ECIGARETTE", value = EcigProduct.class) })
    @Transient
    @CheckForNull
    public Product getProduct() {
        if (getXmlProduct() == null) {
            return null;
        }
        if (this.product == null) {
            this.product = Eucegs.unmarshal(getXmlProduct(), Product.class);
        }
        return this.product;
    }

    void addToSubmission(final SubmissionEntity entity) {
        if (this.submissions.contains(entity)) {
            this.submissions.remove(entity);
        }
        this.submissions.add(entity);
        if (entity.getProduct() != this) {
            entity.setProduct(this);
        }
    }

    /**
     * @return Returns a list of submissions associated to this product.
     */
    @SuppressWarnings("unchecked")
    @Override
    @Nonnull
    @JsonIgnore
    public List<ISubmissionEntity> getSubmissions() {
        final List<? extends ISubmissionEntity> l = submissions;
        return (List<ISubmissionEntity>) l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    @Nullable
    public SubmissionEntity getLastestSubmission() {
        if (this.submissions.isEmpty() && this.child != null) {
            return this.child.getLastestSubmission();
        }
        return FluentIterable.from(this.submissions)
                .sort(SubmissionEntity.LAST_MODIFICATION_DESC_ORDERING)
                .first()
                .orNull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonIgnore
    @Nullable
    public SubmissionEntity getLastestSubmittedSubmission() {
        if (this.submissions.isEmpty() && this.child != null) {
            return this.child.getLastestSubmittedSubmission();
        }
        return FluentIterable.from(this.submissions)
                .sort(SubmissionEntity.LAST_MODIFICATION_DESC_ORDERING)
                .firstMatch(s -> SubmissionStatus.SUBMITTED.equals(s.getSubmissionStatus()))
                .orNull();
    }

    @Override
    @Nonnull
    public Set<String> getAttachments() {
        return attachments;
    }

    /**
     * Set the EUCEG {@link PayloadEntity} with its xml representation.
     *
     * @param xmlProduct
     *                   the xml product representation to use.
     */
    protected void setPayloadProduct(final String xmlProduct) {
        if (this.payloadProduct == null) {
            this.payloadProduct = PayloadEntity.builder().data(xmlProduct).build();
        } else {
            this.payloadProduct = this.payloadProduct.copy().data(xmlProduct).build();
        }
    }

    /**
     * Set the EUCEG {@link Product}.
     *
     * @param product
     *                a product to associate.
     */
    protected void setProduct(final Product product) {
        if (product == null) {
            setPayloadProduct(null);
        } else {
            this.product = product;
            setPayloadProduct(Eucegs.marshal(Eucegs.wrap(product, Product.class)));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("productNumber", productNumber)
                .add("child", child)
                .add("productType", productType)
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
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties({ "readOnly", "sendable", "md5", "links", "previousProductNumber", "version", "type" })
    public static class Builder {

        /** */
        @Nonnull
        private final ProductEntity entity;

        /**
         *
         */
        private Builder() {
            this.entity = new ProductEntity();
        }

        /**
         * Create a new instance of {@link Builder} with {@link ProductEntity product}.
         *
         * @param product
         *                a product to use.
         */
        public Builder(@Nonnull final ProductEntity product) {
            this.entity = product;
        }

        /**
         * @param value
         *              a id.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder id(final String value) {
            entity.productNumber = value;
            return self();
        }

        /**
         * @param value
         *              a product number.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder productNumber(final String value) {
            entity.productNumber = value;
            return self();
        }

        /**
         * @param value
         *              a internal productNumber number.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder internalProductNumber(final String value) {
            entity.internalProductNumber = value;
            return self();
        }

        /**
         * @param value
         *              a previous product.
         * @return Returns fluent {@link Builder}.
         * @since 1.7
         */
        @Nonnull
        public Builder child(final ProductEntity value) {
            entity.child = value;
            return self();
        }

        /**
         * @param value
         *              a product type.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder productType(final ProductType value) {
            entity.productType = value;
            return self();
        }

        @Nonnull
        public Builder type(@Nonnull final EcigProductTypeEnum value) {
            entity.type = value.value();
            return self();
        }

        @Nonnull
        public Builder type(@Nonnull final TobaccoProductTypeEnum value) {
            entity.type = value.value();
            return self();
        }

        /**
         * @param value
         *              a product status.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder status(final ProductStatus value) {
            entity.status = value;
            return self();
        }

        /**
         * @param value
         *              a product status.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder pirStatus(final ProductPirStatus value) {
            entity.pirStatus = value;
            return self();
        }

        /**
         * @param value
         *              a submission type.
         * @return Returns fluent {@link Builder}.
         */
        @JsonProperty(value = "submissionType")
        @Nonnull
        public Builder submissionType(final SubmissionTypeEnum value) {
            entity.preferredSubmissionType = value;
            return self();
        }

        /**
         * @param value
         *              a submitter ID.
         * @return Returns fluent {@link Builder}.
         */
        @JsonProperty(value = "submitterId")
        @Nonnull
        public Builder submitterId(final String value) {
            entity.submitterId = value;
            return self();
        }

        /**
         * @param value
         *              a general comment.
         * @return Returns fluent {@link Builder}.
         */
        @JsonProperty(value = "generalComment")
        @Nonnull
        public Builder generalComment(final String value) {
            entity.preferredGeneralComment = value;
            return self();
        }

        /**
         * @param value
         *              imported file name
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder sourceFilename(final String value) {
            entity.sourceFilename = value;
            return self();
        }

        /**
         * @param value
         *              a EUCEG product.
         * @return Returns fluent {@link Builder}.
         */
        @JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.NAME, property = "productType")
        @JsonSubTypes({ @Type(name = "TOBACCO", value = TobaccoProduct.class),
                @Type(name = "ECIGARETTE", value = EcigProduct.class) })
        @Nonnull
        public Builder product(final Product value) {
            entity.setProduct(value);
            return self();
        }

        /**
         * Add a submission.
         *
         * @param value
         *              a submission
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder submission(@Nonnull final SubmissionEntity value) {
            return submissions(value);
        }

        /**
         * Add a list of submissions.
         *
         * @param values
         *               a list of submissions
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder submissions(@Nonnull final Iterable<SubmissionEntity> values) {
            for (final SubmissionEntity submissionEntity : values) {
                entity.addToSubmission(submissionEntity);
            }
            return self();
        }

        /**
         * Add one or more submissions.
         *
         * @param value
         *               a submission
         * @param values
         *               a list of submissions.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder submissions(@Nonnull final SubmissionEntity value, @Nullable final SubmissionEntity... values) {
            entity.addToSubmission(value);
            if (values != null) {
                for (final SubmissionEntity submissionEntity : values) {
                    entity.addToSubmission(submissionEntity);
                }
            }
            return self();
        }

        @Nonnull
        public Builder attachments(@Nonnull final Iterable<String> values) {
            for (final String attachmentId : values) {
                entity.attachments.add(attachmentId);
            }
            return self();
        }

        @Nonnull
        public Builder clearAttachments() {
            if (entity.attachments != null) {
                entity.attachments.clear();
            }
            return self();
        }

        @Nonnull
        public Builder attachments(@Nullable final String attachmentId, @Nullable final String... values) {
            entity.attachments.add(attachmentId);
            if (values != null) {
                for (final String uuid : values) {
                    entity.attachments.add(uuid);
                }
            }
            return self();
        }

        /**
         * @param value
         *              a login.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder createdBy(final String value) {
            entity.setCreatedBy(value);
            return self();
        }

        /**
         * @param value
         *              a login.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder lastModifiedBy(final String value) {
            entity.setLastModifiedBy(value);
            return self();
        }

        /**
         * @param value
         *              a date.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder createdDate(final DateTime value) {
            entity.setCreatedDate(value);
            return self();
        }

        /**
         * @param value
         *              a date.
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder lastModifiedDate(final DateTime value) {
            entity.setLastModifiedDate(value);
            return self();
        }

        /**
         * Builds a {@link ProductEntity}.
         *
         * @return Returns a instance of {@link ProductEntity}.
         */
        @Nonnull
        public ProductEntity build() {
            return this.entity;
        }

        /**
         * @return Returns the fluent instance of {@link Builder}
         */
        @Nonnull
        protected Builder self() {
            return this;
        }

    }

    /**
     * Jpa Listener allowing clear transient fields on load lifecycle event.
     *
     * @author Christophe Friederich
     */
    public static class ProductListener {

        /**
         * clear transient fields on load lifecycle event.
         *
         * @param entity
         *               entity to clear.
         */
        @PostLoad
        public void onPostLoad(final ProductEntity entity) {
            entity.product = null;
        }

    }

}
