package com.pmi.tpd.core.model.propertyset;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.w3c.dom.Document;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.Data;
import com.opensymphony.util.XMLUtils;

/**
 * <p>
 * PropertySetItem class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@NamedQueries({ @NamedQuery(name = "PropertySet.allKeys", //
        query = "select i.id.key from PropertySetItem i "
                + "where i.id.entityName = :entityName and i.id.entityId = :entityId"),
        @NamedQuery(name = "PropertySet.allKeysWithType", //
                query = "select i.id.key from PropertySetItem i "
                        + "where i.id.entityName = :entityName and i.id.entityId = :entityId and i.type = :type"),
        @NamedQuery(name = "PropertySet.allKeysLike", //
                query = "select i.id.key from PropertySetItem i "
                        + "where i.id.entityName = :entityName and i.id.entityId = :entityId and i.id.key like :like"),
        @NamedQuery(name = "PropertySet.allKeysWithTypeLike", //
                query = "select i.id.key from PropertySetItem i " + "where i.id.entityName = :entityName "
                        + " and i.id.entityId = :entityId and i.type = :type and i.id.key like :like") })
@Entity(name = "PropertySetItem")
@Table(name = "os_propertyentry")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PropertySetItem implements Serializable {

    /** table name associate to this entity. */
    public static final String TABLE_NAME = "os_propertyentry";

    /**
     *
     */
    private static final long serialVersionUID = 2406197815261790782L;

    /** */
    @EmbeddedId
    private PropertySetId id;

    /** */
    @Column(name = "key_type")
    private int type;

    /** */
    @Column(name = "date_value")
    private Date dateVal;

    /** */
    @Column(name = "string_value", length = 2000)
    private String stringVal;

    /** */
    @Column(name = "boolean_value")
    private Boolean booleanVal;

    /** */
    @Column(name = "double_value")
    private Double doubleVal;

    /** */
    @Column(name = "int_value")
    private Integer intVal;

    /** */
    @Column(name = "long_value")
    private Long longVal;

    /** */
    @Column(name = "data_value")
    @Lob
    private byte[] dataVal;

    /**
     * <p>
     * Constructor for PropertySetItem.
     * </p>
     */
    public PropertySetItem() {
    }

    /**
     * Create new instance with {@code builder}.
     *
     * @param builder
     *            builder allowing to initialise.
     */
    public PropertySetItem(final Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        final Object value = builder.value;
        if (value == null) {
            return;
        }
        switch (builder.type) {
            case PropertySet.BOOLEAN:
                booleanVal = ((Boolean) value).booleanValue();
                break;
            case PropertySet.DOUBLE:
                doubleVal = ((Double) value).doubleValue();
                break;
            case PropertySet.STRING:
            case PropertySet.TEXT:
                stringVal = (String) value;
                break;
            case PropertySet.LONG:
                longVal = ((Long) value).longValue();
                break;
            case PropertySet.INT:
                intVal = ((Integer) value).intValue();
                break;
            case PropertySet.DATE:
                dateVal = (Date) value;
                break;
            case PropertySet.DATA:
                if (value instanceof Data) {
                    dataVal = ((Data) value).getBytes();
                } else {
                    dataVal = (byte[]) value;
                }
                break;
            case PropertySet.XML:
                try {
                    stringVal = XMLUtils.print((Document) value);
                } catch (final IOException e) {
                    // /CLOVER:OFF
                    throw new PropertyException("Unexpected error when read xml document: " + e.getMessage());
                    // /CLOVER:ON
                }
                break;
            default:
                throw new PropertyException("type " + type + " not supported");
        }
    }

    /**
     * <p>
     * Getter for the field <code>id</code>.
     * </p>
     *
     * @return the id
     */
    public PropertySetId getId() {
        return id;
    }

    /**
     * <p>
     * Getter for the field <code>type</code>.
     * </p>
     *
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * <p>
     * getBooleanValue.
     * </p>
     *
     * @return the boolean value
     */
    public boolean getBooleanValue() {
        return booleanVal;
    }

    /**
     * <p>
     * getDateValue.
     * </p>
     *
     * @return the date value
     */
    public Date getDateValue() {
        return dateVal;
    }

    /**
     * <p>
     * getDoubleValue.
     * </p>
     *
     * @return the double value
     */
    public double getDoubleValue() {
        return doubleVal;
    }

    /**
     * <p>
     * getIntValue.
     * </p>
     *
     * @return the integer value
     */
    public int getIntValue() {
        return intVal;
    }

    /**
     * <p>
     * getLongValue.
     * </p>
     *
     * @return the long value
     */
    public long getLongValue() {
        return longVal;
    }

    /**
     * <p>
     * getStringValue.
     * </p>
     *
     * @return the string value
     */
    public String getStringValue() {
        return stringVal;
    }

    /**
     * <p>
     * getDataValue.
     * </p>
     *
     * @return large data value
     */
    public byte[] getDataValue() {
        return dataVal;
    }

    /**
     * <p>
     * getValue.
     * </p>
     *
     * @return return the value
     */
    public Object getValue() {
        Object value = null;
        switch (type) {
            case PropertySet.BOOLEAN:
                value = getBooleanValue() ? Boolean.TRUE : Boolean.FALSE;
                break;
            case PropertySet.DOUBLE:
                value = Double.valueOf(getDoubleValue());
                break;
            case PropertySet.STRING:
            case PropertySet.TEXT:
                value = getStringValue();
                break;
            case PropertySet.LONG:
                value = Long.valueOf(getLongValue());
                break;
            case PropertySet.INT:
                value = Integer.valueOf(getIntValue());
                break;
            case PropertySet.DATE:
                value = getDateValue();
                break;
            case PropertySet.DATA:
                return getDataValue();
            case PropertySet.XML:
                try {
                    return XMLUtils.parse(getStringValue());
                } catch (final Exception e) {
                    // /CLOVER:OFF
                    throw new PropertyException(
                            "Unexpected error when read xml document from database: " + e.getMessage());
                    // /CLOVER:ON
                }
            default:
                break;
        }
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof PropertySetItem)) {
            return false;
        }

        final PropertySetItem item = (PropertySetItem) obj;

        return item.id.equals(id);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Copies the instance in builder for modification.
     *
     * @return Returns new {@link com.pmi.tpd.core.model.propertyset.PropertySetItem.Builder} instance.
     */
    public Builder copy() {
        return new Builder().id(id.getEntityName(), id.getEntityId(), id.getKey()).value(type, getValue());
    }

    /**
     * Creates new {@link com.pmi.tpd.core.model.propertyset.PropertySetItem.Builder} instance for the primary keys.
     *
     * @param name
     *            the name of property set
     * @param id
     *            the identifier of property set
     * @param key
     *            the key
     * @return Returns new {@link com.pmi.tpd.core.model.propertyset.PropertySetItem.Builder} instance
     */
    public static Builder builder(final String name, final Long id, final String key) {
        return builder().id(name, id, key);
    }

    /**
     * @return
     * @since 2.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     * @since 1.0
     */
    public static class Builder {

        /** */
        private PropertySetId id;

        /** */
        private int type;

        /**
         *
         */
        private Object value;

        /**
         * Sets the primary keys.
         *
         * @param name
         *            the name
         * @param id
         *            the identifier
         * @param key
         *            the key
         * @return Returns
         */
        public Builder id(final String name, final Long id, final String key) {
            this.id = PropertySetId.builder(name, id, key).build();
            return this;
        }

        public Builder value(final int type, final Object value) {

            this.value = value;
            this.type = type;
            return this;
        }

        public PropertySetItem build() {
            return new PropertySetItem(this);
        }
    }
}
