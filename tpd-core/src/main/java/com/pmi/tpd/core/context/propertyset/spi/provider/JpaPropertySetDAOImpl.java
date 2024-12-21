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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.opensymphony.module.propertyset.PropertyException;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;
import com.pmi.tpd.core.model.propertyset.PropertySetId;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;

/**
 * <p>
 * Jpa PropertySet DAO implementation.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class JpaPropertySetDAOImpl extends SimpleJpaRepository<PropertySetItem, PropertySetId>
        implements IPropertySetDAO {

    private final EntityManager entityManager;

    /**
     * Creates a new {@link com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySetDAOImpl} to manage
     * PropertySetItem object.
     *
     * @param entityManager
     *            must not be {@literal null}.
     */
    @Inject
    public JpaPropertySetDAOImpl(@Nonnull final EntityManager entityManager) {
        super(PropertySetItem.class, entityManager);
        this.entityManager = entityManager;
    }

    /** {@inheritDoc} */
    @Override
    public void persist(@Nonnull final PropertySetItem item) {
        Assert.checkNotNull(item, "item");
        try {
            super.saveAndFlush(item);
        } catch (final DataAccessException e) {
            throw new PropertyException("Could not save key '" + item.getId().getKey() + "':" + e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getKeys(final String entityName, final Long entityId, final String prefix, final int type) {

        List<String> list = null;
        try {
            list = getKeysImpl(entityName, entityId, prefix, type);
        } catch (final DataAccessException e) {
            list = Collections.emptyList();
        }

        return list;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PropertySetItem> findByKey(final String entityName, final Long entityId, final String key) {
        return this.findById(PropertySetId.builder(entityName, entityId, key).build());
    }

    /** {@inheritDoc} */
    @Override
    public Optional<PropertySetItem> findOneByValue(final String entityName,
        final String key,
        final int type,
        final Object value) {
        final PropertySetItem exemple = PropertySetItem.builder(entityName, null, key).value(type, value).build();
        return this.findOne(Example.of(exemple));
    }

    @Override
    public List<PropertySetItem> findAllByValue(final String entityName,
        final String key,
        final int type,
        final Object value) {
        final PropertySetItem exemple = PropertySetItem.builder(entityName, null, key).value(type, value).build();
        return this.findAll(Example.of(exemple));
    }

    @Override
    public Page<PropertySetItem> findAllByValue(final String entityName,
        final String key,
        final int type,
        final Object value,
        final Pageable request) {
        final PropertySetItem exemple = PropertySetItem.builder(entityName, null, key).value(type, value).build();
        return this.findAll(Example.of(exemple), request);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(@Nonnull final String entityName, @Nonnull final Long entityId) {
        try {
            final Collection<String> keys = getKeys(entityName, entityId, null, 0);
            for (final String key : keys) {
                deleteById(PropertySetId.builder(entityName, entityId, key).build());
            }
            flush();
        } catch (final DataAccessException e) {
            throw new PropertyException("Could not remove all keys: " + e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void remove(@Nonnull final String entityName, @Nonnull final Long entityId, @Nonnull final String key) {
        try {
            deleteById(PropertySetId.builder(entityName, entityId, key).build());
            flush();
        } catch (final DataAccessException e) {
            throw new PropertyException("Could not remove key '" + key + "': " + e.getMessage());
        }
    }

    /**
     * This is the body of the getKeys() method, so that you can reuse it wrapped by your own session management.
     *
     * @param entityName
     *            a name of entity (can <b>not</b> be {@code null}).
     * @param entityId
     *            a entity identifier (can <b>not</b> be {@code null}).
     * @param prefix
     *            a prefix of keys (can be {@code null}).
     * @param type
     *            the type of associated keys (select all if equals to 0).
     * @return Returns list of string representing key of each entity found.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public List<String> getKeysImpl(@Nonnull final String entityName,
        @Nonnull final Long entityId,
        @Nullable final String prefix,
        final int type) {
        Query query;
        Assert.checkHasText(entityName, "entityName");
        Assert.checkNotNull(entityId, "entityId");
        if (prefix != null && type > 0) {
            query = this.entityManager.createNamedQuery("PropertySet.allKeysWithTypeLike")
                    .setParameter("like", prefix + '%')
                    .setParameter("type", type);
        } else if (prefix != null) {
            query = this.entityManager.createNamedQuery("PropertySet.allKeysLike").setParameter("like", prefix + '%');
        } else if (type > 0) {
            query = this.entityManager.createNamedQuery("PropertySet.allKeysWithType").setParameter("type", type);
        } else {
            query = this.entityManager.createNamedQuery("PropertySet.allKeys");
        }

        return query.setParameter("entityName", entityName).setParameter("entityId", entityId).getResultList();
    }

}
