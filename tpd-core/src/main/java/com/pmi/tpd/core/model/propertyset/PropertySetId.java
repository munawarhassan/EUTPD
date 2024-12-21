package com.pmi.tpd.core.model.propertyset;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * <p>
 * PropertySetId class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Embeddable
public class PropertySetId implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2406197815261790782L;

    /** */
    @Column(name = "entitty_name", length = 125)
    private String entityName;

    /** */
    @Column(name = "entity_key")
    private String key;

    /** */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * <p>
     * Constructor for PropertySetId.
     * </p>
     */
    public PropertySetId() {
    }

    /**
     * <p>
     * Constructor for PropertySetId.
     * </p>
     *
     * @param builder
     *            a {@link com.pmi.tpd.core.model.propertyset.PropertySetId.Builder} object.
     */
    public PropertySetId(final Builder builder) {
        // Assert.isTrue(builder.id > 0, "PropertySetId.id can not be <=0");
        // Assert.checkHasText(builder.key, "PropertySetId.key");
        // Assert.checkHasText(builder.name, "PropertySetId.name");
        this.entityId = builder.id;
        this.key = builder.key;
        this.entityName = builder.name;
    }

    /**
     * <p>
     * Getter for the field <code>entityId</code>.
     * </p>
     *
     * @return a long.
     */
    public Long getEntityId() {
        return entityId;
    }

    /**
     * <p>
     * Getter for the field <code>entityName</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * <p>
     * Getter for the field <code>key</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getKey() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PropertySetId)) {
            return false;
        }

        final PropertySetId item = (PropertySetId) obj;

        return item.getEntityId().equals(entityId) && item.getEntityName().equals(entityName)
                && item.getKey().equals(key);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return (int) (entityId + entityName.hashCode() + key.hashCode());
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @param name
     *            a {@link java.lang.String} object.
     * @param id
     *            a long.
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link com.pmi.tpd.core.model.propertyset.PropertySetId.Builder} object.
     */
    public static Builder builder(final String name, final Long id, final String key) {
        return new Builder().id(id).key(key).name(name);
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class Builder {

        /** */
        private Long id;

        /** */
        private String key;

        /** */
        private String name;

        /**
         *
         */
        public Builder() {
        }

        public Builder id(final Long id) {
            this.id = id;
            return this;
        }

        public Builder key(final String key) {
            this.key = key;
            return this;
        }

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public PropertySetId build() {
            return new PropertySetId(this);
        }
    }
}
