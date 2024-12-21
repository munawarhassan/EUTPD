/**
 * Copyright 2015 Christophe Friederich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pmi.tpd.core.context.propertyset;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;

import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertySet;

/**
 * This class provides a cache for PropertySet values as used by CachingPropertySet.
 * <p>
 * The intention is that the cache is non-blocking for reads, and it is up to the CachingPropertySet to synchronise
 * writes with reading/writing from the underlying Property Set.
 * <p>
 * This cache looks and works a lot like a PropertySet for obvious reasons, however it is not declared to implement
 * PropertySet because it does not implement all methods - only the ones we want to cache for. With that in mind, it
 * generally attempts to follow the contract of the {@link com.opensymphony.module.propertyset.PropertySet} interface.
 * Namely:
 * <ul>
 * <li>If a property is retrieved that exists but contains a value of different type, a
 * {@link com.opensymphony.module.propertyset.InvalidPropertyTypeException} should be thrown.</li>
 * <li>If a property is retrieved that does not exist, null (or the primitive equivalent) is returned.</li>
 * </ul>
 *
 * @see CachingPropertySet
 * @see PropertySet
 * @author Christophe Friederich
 * @since 1.0
 */
public class PropertySetCache implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final ConcurrentHashMap<String, Object> valueCache;

    /** */
    private final ConcurrentHashMap<String, Boolean> existanceCache;

    /**
     * ConcurrentHashMap does not support null values, so we use a replacement token in the map.
     */
    private static final Object NULL_TOKEN = new Object();

    /**
     *
     */
    PropertySetCache() {
        valueCache = new ConcurrentHashMap<>();
        existanceCache = new ConcurrentHashMap<>();
    }

    /**
     * <p>
     * setBoolean.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a boolean.
     */
    public void setBoolean(final String key, final boolean value) {
        setObject(key, Boolean.valueOf(value));
    }

    /**
     * <p>
     * getBoolean.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a boolean.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public boolean getBoolean(final String key) throws NoValueCachedException {
        final Object value = valueCache.get(key);
        if (value == NULL_TOKEN) {
            // This behaviour is consistent with
            // AbstractPropertySet.getBoolean()
            return false;
        }
        if (value == null) {
            // We don't know about this key.
            throw new NoValueCachedException();
        }
        // We got an actual value from the valueCache - return it
        try {
            return ((Boolean) value).booleanValue();
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a boolean, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setData.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            an array of byte.
     */
    public void setData(final String key, final byte[] value) {
        setObject(key, value);
    }

    /**
     * <p>
     * getData.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return an array of byte.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public byte[] getData(final String key) throws NoValueCachedException {
        try {
            return (byte[]) getObject(key);
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a byte[], but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setDate.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.util.Date} object.
     */
    public void setDate(final String key, final Date value) {
        setObject(key, value);
    }

    /**
     * <p>
     * getDate.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.Date} object.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public Date getDate(final String key) throws NoValueCachedException {
        try {
            return (Date) getObject(key);
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a Date, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setDouble.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a double.
     */
    public void setDouble(final String key, final double value) {
        setObject(key, Double.valueOf(value));
    }

    /**
     * <p>
     * getDouble.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a double.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public double getDouble(final String key) throws NoValueCachedException {
        try {
            final Double value = (Double) getObject(key);
            if (value == null) {
                return 0;
            } else {
                return value.doubleValue();
            }
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a double, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setInt.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a int.
     */
    public void setInt(final String key, final int value) {
        setObject(key, Integer.valueOf(value));
    }

    /**
     * <p>
     * getInt.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a int.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public int getInt(final String key) throws NoValueCachedException {
        try {
            final Integer value = (Integer) getObject(key);
            if (value == null) {
                return 0;
            } else {
                return value.intValue();
            }
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as an int, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setLong.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a long.
     */
    public void setLong(final String key, final long value) {
        setObject(key, Long.valueOf(value));
    }

    /**
     * <p>
     * getLong.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a long.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public long getLong(final String key) throws NoValueCachedException {
        try {
            final Long value = (Long) getObject(key);
            if (value == null) {
                return 0;
            } else {
                return value.longValue();
            }
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a long, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setObject.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.Object} object.
     */
    public void setObject(final String key, final Object value) {
        if (value == null) {
            valueCache.put(key, NULL_TOKEN);
            // This seems to usually mean that the value "exists".
            // Just in case, we will allow the underlying PropertySet define how
            // this works.
            existanceCache.remove(key);
        } else {
            valueCache.put(key, value);
            existanceCache.put(key, Boolean.TRUE);
        }
    }

    /**
     * <p>
     * getObject.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public Object getObject(final String key) throws NoValueCachedException {
        final Object value = valueCache.get(key);
        if (value == NULL_TOKEN) {
            return null;
        }
        if (value == null) {
            // We don't know about this key.
            throw new NoValueCachedException();
        }
        // We got an actual value from the valueCache - return it
        return value;
    }

    /**
     * <p>
     * setProperties.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.util.Properties} object.
     */
    public void setProperties(final String key, final Properties value) {
        setObject(key, value);
    }

    /**
     * <p>
     * getProperties.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.Properties} object.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public Properties getProperties(final String key) throws NoValueCachedException {
        try {
            return (Properties) getObject(key);
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException("Tried to retrieve PropertySet key '" + key
                    + "' as a Properties object, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setString.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.String} object.
     */
    public void setString(final String key, final String value) {
        setObject(key, value);
    }

    /**
     * <p>
     * getString.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws com.opensymphony.module.propertyset.InvalidPropertyTypeException
     *             if any.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public String getString(final String key) throws NoValueCachedException, InvalidPropertyTypeException {
        try {
            return (String) getObject(key);
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a String, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * setText.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link java.lang.String} object.
     */
    public void setText(final String key, final String value) {
        setObject(key, value);
    }

    /**
     * <p>
     * getText.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public String getText(final String key) throws NoValueCachedException {
        return getString(key);
    }

    /**
     * <p>
     * setXML.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param value
     *            a {@link org.w3c.dom.Document} object.
     */
    public void setXML(final String key, final Document value) {
        setObject(key, value);
    }

    /**
     * <p>
     * getXML.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @return a {@link org.w3c.dom.Document} object.
     * @throws com.pmi.tpd.core.context.propertyset.NoValueCachedException
     *             if any.
     */
    public Document getXML(final String key) throws NoValueCachedException {
        try {
            return (Document) getObject(key);
        } catch (final ClassCastException ex) {
            // A value exists, but it is the wrong type. Throw
            // InvalidPropertyTypeException as defined in PropertySet.
            throw new InvalidPropertyTypeException(
                    "Tried to retrieve PropertySet key '" + key + "' as a DOM Document, but it is the wrong type.");
        }
    }

    /**
     * <p>
     * remove.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     */
    public void remove(final String key) {
        valueCache.put(key, NULL_TOKEN);
        existanceCache.put(key, Boolean.FALSE);
    }

    /**
     * <p>
     * remove.
     * </p>
     */
    public void remove() {
        valueCache.clear();
        existanceCache.clear();
    }

    /**
     * Eagerly loads all the values from the given PropertySet into this cache.
     *
     * @param source
     *            The PropertySet to bulk load from.
     */
    public void bulkLoad(final PropertySet source) {
        for (final Object key : source.getKeys()) {
            cloneProperty((String) key, source);
        }
    }

    /**
     * Copy individual property from source to this cache.
     * <p>
     * This is copied from PropertySetCloner.
     *
     * @param key
     *            The key to clone.
     * @param source
     *            The PropertySet we are cloning.
     * @see com.opensymphony.module.propertyset.PropertySetCloner
     */
    private void cloneProperty(final String key, final PropertySet source) {
        switch (source.getType(key)) {
            case PropertySet.BOOLEAN:
                this.setBoolean(key, source.getBoolean(key));
                break;

            case PropertySet.INT:
                this.setInt(key, source.getInt(key));
                break;

            case PropertySet.LONG:
                this.setLong(key, source.getLong(key));
                break;

            case PropertySet.DOUBLE:
                this.setDouble(key, source.getDouble(key));
                break;

            case PropertySet.STRING:
                this.setString(key, source.getString(key));
                break;

            case PropertySet.TEXT:
                this.setText(key, source.getText(key));
                break;

            case PropertySet.DATE:
                this.setDate(key, source.getDate(key));
                break;

            case PropertySet.OBJECT:
                this.setObject(key, source.getObject(key));
                break;

            case PropertySet.XML:
                this.setXML(key, source.getXML(key));
                break;

            case PropertySet.DATA:
                this.setData(key, source.getData(key));
                break;

            case PropertySet.PROPERTIES:
                this.setProperties(key, source.getProperties(key));
                break;
            default:
                throw new UnsupportedOperationException("unsupported source type");
        }
    }

    /**
     * Returns the cached value for whether the given key exists in the underlying PropertySet.
     * <p>
     * It is important not to confuse this method with the {@link java.util.Map#containsKey} method.
     *
     * @param key
     *            The property key.
     * @return A Boolean object containing the cached existance of a value for the key, or null if we haven't cached the
     *         existance for this key.
     * @see CachingPropertySet#exists(String)
     */
    public Boolean exists(final String key) {
        return existanceCache.get(key);
    }

    /**
     * <p>
     * cacheExistance.
     * </p>
     *
     * @param key
     *            a {@link java.lang.String} object.
     * @param keyExists
     *            a boolean.
     */
    public void cacheExistance(final String key, final boolean keyExists) {
        existanceCache.put(key, Boolean.valueOf(keyExists));
    }

}
