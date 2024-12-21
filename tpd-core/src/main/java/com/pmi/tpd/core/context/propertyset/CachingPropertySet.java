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
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.util.ReflectionUtils;
import org.w3c.dom.Document;

import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetSchema;
import com.opensymphony.util.DataUtil;
import com.pmi.tpd.api.util.Assert;

/**
 * A PropertySet which decorates another PropertySet and caches the results. Must be correctly initialised via the
 * {@link #init(Map, Map)} before use.
 * <p>
 * Similar to com.opensymphony.module.propertyset.cached.CachingPropertySet class but does more caching.
 * <p>
 * This class is threadsafe. It uses a {@link java.util.concurrent.locks.Lock} to co-ordinate concurrent access. This
 * means that improper publication is avoided as long as {@link #init(Map, Map)} is called from the creating thread
 * before use.
 *
 * @author devacfr
 * @since 1.0
 */
public class CachingPropertySet implements PropertySet, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5203258195595123554L;

    /** concurrent lock used. (default {@link ReentrantLock} ) */
    private final Lock lock;

    /** Property set to cache. */
    private PropertySet decoratedPS;

    /** the associate property set cache. */
    private PropertySetCache propertySetCache;

    /**
     * <p>
     * Constructor for CachingPropertySet.
     * </p>
     */
    public CachingPropertySet() {
        this(new ReentrantLock());
    }

    /**
     * @param lock
     */
    CachingPropertySet(@Nonnull final Lock lock) {
        this.lock = Assert.checkNotNull(lock, "lock");
    }

    /**
     * <p>
     * flush.
     * </p>
     */
    public void flush() {
        lock.lock();
        try {
            final Method method = ReflectionUtils.findMethod(decoratedPS.getClass(), "flush");
            if (method != null) {
                ReflectionUtils.invokeMethod(method, decoratedPS);
            }
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setAsActualType(@Nonnull final String key, @Nonnull final Object value) throws PropertyException {
        if (value instanceof Boolean) {
            setBoolean(key, DataUtil.getBoolean((Boolean) value));
        } else if (value instanceof Integer) {
            setInt(key, DataUtil.getInt((Integer) value));
        } else if (value instanceof Long) {
            setLong(key, DataUtil.getLong((Long) value));
        } else if (value instanceof Double) {
            setDouble(key, DataUtil.getDouble((Double) value));
        } else if (value instanceof String) {
            if (value.toString().length() > 255) {
                setText(key, (String) value);
            } else {
                setString(key, (String) value);
            }
        } else if (value instanceof Date) {
            setDate(key, (Date) value);
        } else if (value instanceof Document) {
            setXML(key, (Document) value);
        } else if (value instanceof byte[]) {
            setData(key, (byte[]) value);
        } else if (value instanceof Properties) {
            setProperties(key, (Properties) value);
        } else {
            setObject(key, value);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getAsActualType(@Nonnull final String key) throws PropertyException {
        final int type = getType(key);
        Object value = null;
        switch (type) {
            case BOOLEAN:
                value = Boolean.valueOf(getBoolean(key));
                break;

            case INT:
                value = Integer.valueOf(getInt(key));
                break;

            case LONG:
                value = Long.valueOf(getLong(key));
                break;

            case DOUBLE:
                value = Double.valueOf(getDouble(key));
                break;

            case STRING:
                value = getString(key);
                break;

            case TEXT:
                value = getText(key);
                break;
            case DATE:
                value = getDate(key);
                break;

            case XML:
                value = getXML(key);
                break;

            case DATA:
                value = getData(key);
                break;

            case PROPERTIES:
                value = getProperties(key);
                break;

            case OBJECT:
                value = getObject(key);
                break;
            default:
                value = null;
                break;
        }
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public void setBoolean(@Nonnull final String key, final boolean value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setBoolean(key, value);
            propertySetCache.setBoolean(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean getBoolean(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getBoolean(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getBoolean(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final boolean value = decoratedPS.getBoolean(key);
                // cache it
                propertySetCache.setBoolean(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setData(@Nonnull final String key, @Nullable final byte[] value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setData(key, value);
            propertySetCache.setData(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public byte[] getData(final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getData(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getData(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final byte[] value = decoratedPS.getData(key);
                // cache it
                propertySetCache.setData(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDate(@Nonnull final String key, @Nullable final Date value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setDate(key, value);
            propertySetCache.setDate(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Date getDate(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getDate(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getDate(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final Date value = decoratedPS.getDate(key);
                // cache it
                propertySetCache.setDate(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setDouble(@Nonnull final String key, final double value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setDouble(key, value);
            propertySetCache.setDouble(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getDouble(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getDouble(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getDouble(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final double value = decoratedPS.getDouble(key);
                // cache it
                propertySetCache.setDouble(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setInt(@Nonnull final String key, final int value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setInt(key, value);
            propertySetCache.setInt(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getInt(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getInt(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getInt(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final int value = decoratedPS.getInt(key);
                // cache it
                propertySetCache.setInt(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    @Nonnull
    public Collection<String> getKeys() throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.getKeys();
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    @Nonnull
    public Collection<String> getKeys(final int type) throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.getKeys(type);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    @Nonnull
    public Collection<String> getKeys(@Nullable final String prefix) throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.getKeys(prefix);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    @Nonnull
    public Collection<String> getKeys(@Nullable final String prefix, final int type) throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.getKeys(prefix, type);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setLong(@Nonnull final String key, final long value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setLong(key, value);
            propertySetCache.setLong(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public long getLong(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getLong(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getLong(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final long value = decoratedPS.getLong(key);
                // cache it
                propertySetCache.setLong(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setObject(@Nonnull final String key, @Nullable final Object value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setObject(key, value);
            propertySetCache.setObject(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Object getObject(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getObject(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getObject(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final Object value = decoratedPS.getObject(key);
                // cache it
                propertySetCache.setObject(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setProperties(@Nonnull final String key, @Nonnull final Properties value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setProperties(key, value);
            propertySetCache.setProperties(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Properties getProperties(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getProperties(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getProperties(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final Properties value = decoratedPS.getProperties(key);
                // cache it
                propertySetCache.setProperties(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setSchema(@Nullable final PropertySetSchema schema) throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            decoratedPS.setSchema(schema);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public PropertySetSchema getSchema() throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.getSchema();
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSettable(@Nullable final String property) {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.isSettable(property);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setString(@Nonnull final String key, @Nullable final String value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setString(key, value);
            propertySetCache.setString(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getString(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getString(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getString(key);
                // Another thread just filled it in - lucky us!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final String value = decoratedPS.getString(key);
                // cache it
                propertySetCache.setObject(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setText(@Nonnull final String key, @Nullable final String value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setText(key, value);
            propertySetCache.setText(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getText(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getText(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getText(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final String value = decoratedPS.getText(key);
                // cache it
                propertySetCache.setText(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getType(@Nonnull final String key) throws PropertyException {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.getType(key);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setXML(@Nonnull final String key, @Nullable final Document value) throws PropertyException {
        lock.lock();
        try {
            decoratedPS.setXML(key, value);
            propertySetCache.setXML(key, value);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Document getXML(@Nonnull final String key) throws PropertyException {
        // First try to get the value from cache
        try {
            return propertySetCache.getXML(key);
        } catch (final NoValueCachedException ex) {
            // Cache miss - we obtain a lock so we can safely read from the
            // underlying PropertySet and write the
            // retrieved value to our cache in an atomic operation.
            lock.lock();
            try {
                // Try the cache again - we may save a DB lookup. The faster we
                // unlock the better.
                return propertySetCache.getXML(key);
                // If we get here, then we got lucky!
            } catch (final NoValueCachedException ex2) {
                // The cache is still missing this key - retrieve the value from
                // the underlying PropertySet
                final Document value = decoratedPS.getXML(key);
                // cache it
                propertySetCache.setXML(key, value);
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(@Nonnull final String key) throws PropertyException {
        // Check if we have the result for the exists() call cached.
        Boolean exists = propertySetCache.exists(key);
        if (exists != null) {
            // Cache hit - use it.
            return exists.booleanValue();
        }
        // Cache missed. Obtain a lock so we can safely update the cache.
        lock.lock();
        try {
            // Try the cache again in case another thread has already updated
            // it.
            exists = propertySetCache.exists(key);
            if (exists != null) {
                return exists.booleanValue();
            }
            // If not check the decoratedPS
            final boolean keyExists = decoratedPS.exists(key);
            // Cache the result
            propertySetCache.cacheExistance(key, keyExists);
            return keyExists;
        } finally {
            lock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Initialises this PorpertyCachingPropertySet. PropertySetManager first constructs an instance of a PropertySet,
     * and then calls init().
     * </p>
     * <p>
     * The <code>args</code> parameter must always contain an entry of type PropertySet under the key "PropertySet". If
     * <code>args</code> contains a <code>Boolean</code> entry under the key of "bulkload" which is set to
     * <code>true</code>, then all the values in the underlying PropertySet will be preloaded into the cache.
     * </p>
     *
     * @see PropertySet#init(java.util.Map,java.util.Map)
     * @see com.opensymphony.module.propertyset.PropertySetManager#getInstance(String,java.util.Map)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void init(final Map config, @Nonnull final Map args) {
        lock.lock();
        try {
            decoratedPS = (PropertySet) args.get("PropertySet");
            if (decoratedPS == null) {
                throw new NullPointerException("Decorated property set is missing! Cannot initialise.");
            }
            propertySetCache = new PropertySetCache();
            final Boolean bulkload = (Boolean) args.get("bulkload");
            if (bulkload != null && bulkload.booleanValue()) {
                propertySetCache.bulkLoad(decoratedPS);
            }
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove(final String key) throws PropertyException {
        lock.lock();
        try {
            propertySetCache.remove(key);
            decoratedPS.remove(key);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove() throws PropertyException {
        lock.lock();
        try {
            propertySetCache.remove();
            decoratedPS.remove();
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsType(final int type) {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.supportsType(type);
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsTypes() {
        // TODO: Do we really need to lock in this case?
        lock.lock();
        try {
            return decoratedPS.supportsTypes();
        } finally {
            lock.unlock();
        }
    }
}
