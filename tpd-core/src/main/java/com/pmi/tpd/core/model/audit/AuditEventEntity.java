package com.pmi.tpd.core.model.audit;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.util.ObjectUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.api.audit.IAuditEvent;
import com.pmi.tpd.api.model.AbstractEntityBuilder;
import com.pmi.tpd.api.model.IIdentityEntity;
import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.api.util.Assert;

/**
 * Persist AuditEvent.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Entity(name = "AuditEvent")
@Table(name = AuditEventEntity.TABLE_NAME)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuditEventEntity implements IAuditEvent, IIdentityEntity<Long>, IInitializable {

    /** The name of the primary key generator to use. */
    private static final String ID_GEN = "auditEventIdGenerator";

    /** table name associate to this entity. */
    public static final String TABLE_NAME = "t_audit_event";

    /** */
    public static final String TABLE_NAME_DATA = "t_audit_event_data";

    public static final String TABLE_NAME_CHANNEL = "t_audit_event_channel";

    /** primary key. */
    @Id
    @TableGenerator(name = ID_GEN, table = ApplicationConstants.Jpa.Generator.NAME,
            pkColumnName = ApplicationConstants.Jpa.Generator.COLUMN_NAME,
            valueColumnName = ApplicationConstants.Jpa.Generator.COLUMN_VALUE_NAME, pkColumnValue = "event_id",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = ID_GEN)
    @Column(name = "event_id")
    private Long id;

    /** */
    @NotNull
    @Column(nullable = false)
    private String principal;

    /** */
    @Column(name = "event_date")
    private Date timestamp;

    /** */
    @Column(name = "event_type")
    private String action;

    /** */
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "data_name")
    @Column(name = "data_value", length = 512)
    @CollectionTable(name = TABLE_NAME_DATA, joinColumns = @JoinColumn(name = "event_id"))
    private Map<String, String> data;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "channel_value")
    @CollectionTable(name = TABLE_NAME_CHANNEL, joinColumns = @JoinColumn(name = "event_id"))
    private Set<String> channels;

    /**
     * <p>
     * Constructor for AuditEventEntity.
     * </p>
     */
    public AuditEventEntity() {
    }

    /**
     * <p>
     * Constructor for AuditEventEntity.
     * </p>
     *
     * @param builder
     *                a {@link com.pmi.tpd.core.model.audit.AuditEventEntity.Builder} object.
     */
    public AuditEventEntity(final Builder builder) {
        this.id = builder.id();
        this.timestamp = builder.timestamp;
        this.action = builder.action;
        this.data = Maps.newHashMap(builder.data);
        this.channels = Sets.newHashSet(builder.channels);
        this.principal = Assert.checkNotNull(builder.principal, "auditEventEntity.principal");
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof AuditEventEntity) {
            final AuditEventEntity that = (AuditEventEntity) o;
            return ObjectUtils.nullSafeEquals(getId(), that.getId());
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return ObjectUtils.nullSafeHashCode(getId());
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
    }

    /** {@inheritDoc} */
    @Override
    public Long getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getPrincipal() {
        return principal;
    }

    /** {@inheritDoc} */
    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    /** {@inheritDoc} */
    @Override
    public String getAction() {
        return action;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getData() {
        return data;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getChannels() {
        return channels;
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.model.audit.AuditEventEntity.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * <p>
     * copy.
     * </p>
     *
     * @return a {@link com.pmi.tpd.core.model.audit.AuditEventEntity.Builder} object.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder extends AbstractEntityBuilder<Long, AuditEventEntity, Builder> {

        /** */
        private String principal;

        /** */
        private Date timestamp;

        /** */
        private String action;

        /** */
        private final Map<String, String> data;

        /** */
        private final Set<String> channels;

        /**
         *
         */
        public Builder() {
            this.data = Maps.newHashMap();
            this.channels = Sets.newHashSet();
        }

        /**
         * @param audit
         */
        public Builder(final AuditEventEntity audit) {
            super(audit);
            this.timestamp = audit.timestamp;
            this.action = audit.action;
            this.data = Maps.newHashMap(audit.data);
            this.channels = Sets.newHashSet(audit.channels);
            this.principal = audit.principal;
        }

        /**
         * @param principal
         * @return
         */
        public Builder principal(final String principal) {
            this.principal = principal;
            return self();
        }

        /**
         * @param created
         * @return
         */
        public Builder created(final Date created) {
            this.timestamp = created;
            return self();
        }

        /**
         * @param action
         * @return
         */
        public Builder action(final String action) {
            this.action = action;
            return self();
        }

        /**
         * @param key
         * @param value
         * @return
         */
        public Builder data(@Nonnull final String key, @Nonnull final String value) {
            this.data.put(key, value);
            return self();
        }

        /**
         * @param data
         * @return
         */
        public Builder data(final Map<String, String> data) {
            if (data != null) {
                this.data.putAll(data);
            }
            return self();
        }

        /**
         * @param key
         * @param value
         * @return
         */
        public Builder channel(@Nonnull final String value) {
            this.channels.add(value);
            return self();
        }

        /**
         * @param data
         * @return
         */
        public Builder channels(final Set<String> channels) {
            if (channels != null) {
                this.channels.addAll(channels);
            }
            return self();
        }

        @Override
        public AuditEventEntity build() {
            return new AuditEventEntity(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Builder self() {
            return this;
        }

    }

}
