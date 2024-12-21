package com.pmi.tpd.core.model.euceg;

import javax.annotation.Nonnull;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.Audited;
import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.base.MoreObjects;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.core.model.BaseAuditingEntity;
import com.pmi.tpd.euceg.api.entity.IPayloadEntity;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Audited()
@AuditOverride(forClass = BaseAuditingEntity.class)
@Entity(name = "Payload")
@Table(name = PayloadEntity.TABLE_NAME)
@Cacheable(false)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonDeserialize(builder = PayloadEntity.Builder.class)
public class PayloadEntity extends BaseAuditingEntity<Long> implements IInitializable, IPayloadEntity {

    /** generator identifier. */
    private static final String ID_GEN = "payloadIdGenerator";

    /** */
    public static final String GENERATOR_COLUMN_NAME = "payload_id";

    /** table name. */
    public static final String TABLE_NAME = "t_payload";

    /** */
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME, //
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME, //
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME,
            pkColumnValue = GENERATOR_COLUMN_NAME, allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
    private Long id;

    /** */
    @NotNull
    @Lob
    @Column(name = "payload_data", length = 1024000, nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    private String data;

    @Override
    public void initialize() {

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("id", id).toString();
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
    public static class Builder {

        private final PayloadEntity entity;

        /**
        *
        */
        private Builder() {
            this.entity = new PayloadEntity();
        }

        /**
        *
        */
        public Builder(@Nonnull final PayloadEntity entity) {
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
        public Builder data(final String value) {
            if (value != null) {
                entity.data = value;
            } else {
                entity.data = null;
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

        public PayloadEntity build() {
            return entity;
        }

        protected Builder self() {
            return this;
        }

    }

}
