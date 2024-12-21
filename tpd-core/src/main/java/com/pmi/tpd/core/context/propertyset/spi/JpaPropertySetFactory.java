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
package com.pmi.tpd.core.context.propertyset.spi;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.module.propertyset.PropertySetManager;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.context.IPropertySetFactory;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;
import com.pmi.tpd.core.context.propertyset.PropertySetAccessor;
import com.pmi.tpd.core.context.propertyset.spi.provider.DefaultJpaConfigurationProvider;
import com.pmi.tpd.core.context.propertyset.spi.provider.IJpaConfigurationProvider;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;

/**
 * Default implementation of the PortalPropertySetFactory. It relies heavily on
 * the
 * {@link com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySet
 * JpaPropertySet} and
 * {@link com.pmi.tpd.core.context.propertyset.CachingPropertySet
 * CachingPropertySet}.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Named
public class JpaPropertySetFactory implements IPropertySetFactory {

  /** */
  private static final Long DEFAULT_ENTITY_ID = Long.valueOf(1);

  /** */
  private final IPropertySetDAO propertySetDAO;

  /**
   * <p>
   * Constructor for JpaPropertySetFactory.
   * </p>
   *
   * @param propertySetDAO
   *                       a
   *                       {@link com.pmi.tpd.core.context.propertyset.IPropertySetDAO}
   *                       object.
   */
  @Inject
  public JpaPropertySetFactory(@Nonnull final IPropertySetDAO propertySetDAO) {
    this.propertySetDAO = Assert.checkNotNull(propertySetDAO, "propertySetDAO");
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildPersistent(@Nonnull final String entityName) {
    return buildPersistent(entityName, DEFAULT_ENTITY_ID);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildPersistent(@Nonnull final String entityName, @Nonnull final Long entityId) {
    final Map<String, Object> args = new ImmutableMap.Builder<String, Object>().put("entityName", entityName)
        .put("entityId", entityId)
        .put(IJpaConfigurationProvider.PROVIDER_NAME_PROPERTY,
            new DefaultJpaConfigurationProvider(propertySetDAO))
        .build();
    return new PropertySetAccessor(createPropertySet("jpa", args));
  }

  /** {@inheritDoc} */
  @Override
  public IPropertyAccessor buildProperties(@Nonnull final File propertyFile) {
    Assert.checkNotNull(propertyFile, "propertyFile");
    return new PropertySetAccessor(createPropertySet("properties", //
        ImmutableMap.<String, Object>of("file", propertyFile.getAbsolutePath())));
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildAggregator(final IPropertyAccessor... accessors) {
    checkNotNull(accessors, "accessors");
    return new PropertySetAccessor(createPropertySet("aggregate",
        ImmutableMap.<String, Object>of("PropertySets",
            Arrays.asList(accessors).stream().map(this::toPropertySet).collect(Collectors.toUnmodifiableList()))));
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildCachingDefault(@Nonnull final String entityName, final boolean bulkLoad) {
    return buildCaching(buildPersistent(entityName), bulkLoad);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildCaching(@Nonnull final String entityName,
      @Nonnull final Long entityId,
      final boolean bulkLoad) {
    return buildCaching(buildPersistent(entityName, entityId), bulkLoad);
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildCaching(@Nonnull final IPropertyAccessor accessor, final boolean bulkLoad) {
    checkNotNull(accessor, "accessor");
    Assert.state(accessor instanceof PropertySetAccessor, "Only PropertySetAccessor accepted");
    final PropertySet propertySet = toPropertySet(accessor);
    final Map<String, Object> args = new ImmutableMap.Builder<String, Object>().put("PropertySet", propertySet)
        .put("bulkload", Boolean.valueOf(bulkLoad))
        .build();
    return new PropertySetAccessor(createPropertySet("cached", args));
  }

  /** {@inheritDoc} */
  @Override
  @Nonnull
  public IPropertyAccessor buildPersitentMemory(@Nonnull final String entityName, @Nonnull final Long entityId) {

    final PropertySetAccessor dbPropertySet = (PropertySetAccessor) buildPersistent(entityName, entityId);
    final PropertySet memoryPropertySet = createPropertySet("memory", Maps.newHashMap());

    // Clone the property set.
    PropertySetManager.clone(dbPropertySet.getPropertySet(), memoryPropertySet);

    return new PropertySetAccessor(memoryPropertySet);
  }

  @Override
  @Nonnull
  public IPropertyAccessor buildMemory() {
    return new PropertySetAccessor(createPropertySet("memory", null));
  }

  @Override
  public Optional<Long> findByValue(@Nonnull final String entityName,
      final int type,
      @Nonnull final String key,
      @Nonnull final Object value) {
    final Optional<PropertySetItem> item = propertySetDAO.findOneByValue(entityName, key, type, value);
    return item.map(prop -> prop.getId().getEntityId());
  }

  @Nonnull
  private PropertySet createPropertySet(@Nonnull final String propertySetDelegator,
      @Nonnull final Map<String, Object> args) {
    return PropertySetManager.getInstance(propertySetDelegator, args);
  }

  private PropertySet toPropertySet(final IPropertyAccessor accessor) {
    return ((PropertySetAccessor) accessor).getPropertySet();
  }
}
