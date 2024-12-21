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
package com.pmi.tpd.core.context.propertyset.spi.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;

/**
 * This is the property set implementation for storing properties using Jpa.
 * <p/>
 * <p/>
 * <b>Required Args</b>
 * <ul>
 * <li><b>entityId</b> - Long that holds the ID of this entity.</li>
 * <li><b>entityName</b> - String that holds the name of this entity type</li>
 * </ul>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
public class JpaPropertySet extends AbstractPropertySet {

    /**
     * property name used allowing initialise {@link JpaPropertySet#entityName} field of a {@link JpaPropertySet}
     * instance.
     *
     * @see #init(Map, Map)
     */
    public static final String ENTITY_NAME_PROPERTY = "entityName";

    /**
     * property name used allowing initialise {@link JpaPropertySet#entityId} field of a {@link JpaPropertySet}
     * instance.
     *
     * @see #init(Map, Map)
     */
    public static final String ENTITY_ID_PROPERTY = "entityId";

    /** logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JpaPropertySet.class.getName());

    /** */
    private IJpaConfigurationProvider configProvider;

    /** */
    private Long entityId;

    /** */
    private String entityName;

    /** {@inheritDoc} */
    @Override
    public Collection<String> getKeys(final String prefix, final int type) throws PropertyException {
        return configProvider.getPropertySetDAO().getKeys(entityName, entityId, prefix, type);
    }

    /** {@inheritDoc} */
    @Override
    public int getType(final String key) throws PropertyException {
        final Optional<PropertySetItem> item = findByKey(key);
        if (item.isPresent()) {
            return item.get().getType();
        }
        return -1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean exists(final String key) throws PropertyException {
        return findByKey(key).isPresent();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public void init(final Map config, final Map args) {
        super.init(config, args);
        this.entityId = (Long) args.get(ENTITY_ID_PROPERTY);
        this.entityName = (String) args.get(ENTITY_NAME_PROPERTY);

        // first let's see if we got given a configuration provider to use already
        configProvider = (IJpaConfigurationProvider) args.get(IJpaConfigurationProvider.PROVIDER_NAME_PROPERTY);

        if (configProvider != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Setting up property set with Jpa provider passed in args.");
            }
        } else {
            throw new IllegalArgumentException("configurationProvider is required");
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void remove(@Nonnull final String key) throws PropertyException {
        configProvider.getPropertySetDAO().remove(entityName, entityId, key);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void remove() throws PropertyException {
        configProvider.getPropertySetDAO().remove(entityName, entityId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean supportsType(final int type) {
        switch (type) {
            case PropertySet.OBJECT:
            case PropertySet.PROPERTIES:
                return false;
            default:
                break;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    protected void setImpl(final int type, final String key, final Object value) throws PropertyException {
        PropertySetItem item = null;

        final Optional<PropertySetItem> option = configProvider.getPropertySetDAO()
                .findByKey(entityName, entityId, key);

        if (!option.isPresent()) {
            item = PropertySetItem.builder(entityName, entityId, key).value(type, value).build();
        } else {
            item = option.get();
            if (item.getType() != type) {
                throw new PropertyException("Existing key '" + key + "' does not have matching type of " + type);
            } else {
                item = item.copy().value(type, value).build();
            }
        }

        configProvider.getPropertySetDAO().persist(item);
    }

    /** {@inheritDoc} */
    @Override
    protected Object get(final int type, final String key) throws PropertyException {
        final Optional<PropertySetItem> option = findByKey(key);
        if (!option.isPresent()) {
            return null;
        }

        final PropertySetItem item = option.get();
        if (item.getType() != type) {
            throw new PropertyException("key '" + key + "' does not have matching type of " + type);
        }
        if (!supportsType(type)) {
            throw new PropertyException("type " + type + " not supported");
        }
        return item.getValue();
    }

    private Optional<PropertySetItem> findByKey(final String key) throws PropertyException {
        return configProvider.getPropertySetDAO().findByKey(entityName, entityId, key);
    }
}
