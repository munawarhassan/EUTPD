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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.api.util.ClassLoaderUtils;

/**
 * <p>
 * PropertyUtils class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class PropertyUtils {

    /** logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtils.class);

    /**
     * Private constructor.
     */
    private PropertyUtils() {
    }

    /**
     * <p>
     * createPropertyKey.
     * </p>
     *
     * @param prefix
     *            a {@link java.lang.String} object.
     * @param attribute
     *            a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String createPropertyKey(final String prefix, final String... attribute) {
        final StringBuilder key = new StringBuilder(prefix);
        if (attribute != null) {
            for (final String attr : attribute) {
                key.append('.');
                key.append(attr.toLowerCase());
            }
        }
        return key.toString();
    }

    /**
     * <p>
     * getProperties.
     * </p>
     *
     * @param resource
     *            a {@link java.lang.String} object.
     * @param callingClass
     *            a {@link java.lang.Class} object.
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getProperties(final String resource, final Class<?> callingClass) {
        return getPropertiesFromStream(ClassLoaderUtils.getResourceAsStream(resource, callingClass));
    }

    /**
     * <p>
     * getPropertiesFromFile.
     * </p>
     *
     * @param file
     *            a {@link java.io.File} object.
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getPropertiesFromFile(final File file) {
        try {
            return getPropertiesFromStream(new FileInputStream(file));
        } catch (final FileNotFoundException e) {
            LOGGER.error("Error loading properties from file: " + file.getPath() + ". File does not exist.", e);
            return null;
        }
    }

    /**
     * <p>
     * getPropertiesFromStream.
     * </p>
     *
     * @param is
     *            a {@link java.io.InputStream} object.
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getPropertiesFromStream(final InputStream is) {
        if (is == null) {
            return null;
        }

        final Properties props = new Properties();
        try {
            props.load(is);
        } catch (final IOException e) {
            LOGGER.error("Error loading properties from stream.", e);
        } finally {
            Closeables.closeQuietly(is);
        }

        return props;
    }

    /**
     * Check to see if the two propertySet contain the same values and types NOTE If both PropertySets are null then
     * <i>true</i> is returned.
     *
     * @param pThis
     *            First PropertySet
     * @param pThat
     *            Second PropertySet
     * @return Are the two PropertySets identical
     */
    public static boolean identical(final PropertySet pThis, final PropertySet pThat) {
        // Check to see if both of the collections are null
        if (pThis == null && pThat == null) {
            return true;
        }

        // Check to see if either of the collections are null
        if (pThis == null || pThat == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        final Collection<String> thisKeys = pThis.getKeys();
        final Collection<?> thatKeys = pThat.getKeys();

        if (!thisKeys.containsAll(thatKeys) || !thatKeys.containsAll(thisKeys)) {
            return false;
        }

        final Iterator<String> thisKeysIterator = thisKeys.iterator();
        String key;
        int keyType;
        while (thisKeysIterator.hasNext()) {
            key = thisKeysIterator.next();
            keyType = pThis.getType(key);
            if (PropertySet.BOOLEAN == keyType) {
                if (pThis.getBoolean(key) != pThat.getBoolean(key)) {
                    return false;
                }
            } else if (PropertySet.DATA == keyType) {
                throw new IllegalArgumentException("DATA Comparision has not been implemented in PropertyUtil");
            } else if (PropertySet.DATE == keyType) {
                if (!pThis.getDate(key).equals(pThat.getDate(key))) {
                    return false;
                }
            } else if (PropertySet.DOUBLE == keyType) {
                if (pThis.getDouble(key) != pThat.getDouble(key)) {
                    return false;
                }
            } else if (PropertySet.INT == keyType) {
                if (pThis.getInt(key) != pThat.getInt(key)) {
                    return false;
                }
            } else if (PropertySet.OBJECT == keyType) {
                throw new IllegalArgumentException("OBJECT Comparision has not been implemented in PropertyUtil");
            } else if (PropertySet.PROPERTIES == keyType) {
                throw new IllegalArgumentException("PROPERTIES Comparision has not been implemented in PropertyUtil");
            } else if (PropertySet.LONG == keyType) {
                if (pThis.getLong(key) != pThat.getLong(key)) {
                    return false;
                }
            } else if (PropertySet.STRING == keyType) {
                if (!pThis.getString(key).equals(pThat.getString(key))) {
                    return false;
                }
            } else if (PropertySet.TEXT == keyType) {
                if (!pThis.getText(key).equals(pThat.getText(key))) {
                    return false;
                }
            } else if (PropertySet.XML == keyType) {
                throw new IllegalArgumentException("XML Comparision has not been implemented in PropertyUtil");
            }
        }

        return true;
    }

}
