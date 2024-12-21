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
package com.pmi.tpd.api.context;

import java.io.File;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides a number of utility methods to get a handle on a property set.
 * Please note that this class' sole
 * responsibility is to create a property set. It does not cache this property
 * set internal and repeated method calls
 * will create a new property set every time. The returned PropertySet should be
 * referenced by the caller to avoid
 * re-creating it every time.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IPropertySetFactory {

  /**
   * Returns a {@link IPropertyAccessor} for a particular entity. Specifies a
   * default entity id of 1. Any access (read
   * and write) to this PropertySet will result in a DB call.
   *
   * @param entityName
   *                   The entity name to lookup your properties. E.g.
   *                   app.svn.plugin
   * @return a {@link IPropertyAccessor}
   */
  @Nonnull
  IPropertyAccessor buildPersistent(@Nonnull String entityName);

  /**
   * Returns a {@link IPropertyAccessor}. Any access (read and write) to this
   * PropertySet will result in a DB call.
   *
   * @param entityName
   *                   The entity name to lookup your properties. E.g.
   *                   app.svn.plugin
   * @param entityId
   *                   The entity id if you multiple properties per entity name.
   *                   (E.g. OSUser properties per user id)
   * @return a {@link IPropertyAccessor}
   */
  @Nonnull
  IPropertyAccessor buildPersistent(@Nonnull String entityName, Long entityId);

  /**
   * Returns a {@link IPropertyAccessor} for a particular entity. Entries in this
   * set are cached in memory for better
   * performance.Specifies a default entity id of 1. Please note that this is a
   * write-through-cache, meaning that
   * reads will be cached up, however any write will call through to the database
   * and invalidate the relevant cache
   * entry.
   *
   * @param entityName
   *                   The entity name to lookup your properties. E.g.
   *                   app.svn.plugin
   * @param bulkLoad
   *                   If true, all properties will be loaded during
   *                   initialisation of the accessor and cached
   * @return a {@link IPropertyAccessor} backed by a caching property set
   */
  @Nonnull
  IPropertyAccessor buildCachingDefault(@Nonnull String entityName, boolean bulkLoad);

  /**
   * Returns a {@link IPropertyAccessor}. Entries in this set are cached in memory
   * for better performance. Please note
   * that this is a write-through-cache, meaning that reads will be cached up,
   * however any write will call through to
   * the database and invalidate the relevant cache entry.
   *
   * @param entityName
   *                   The entity name to lookup your properties. E.g.
   *                   app.svn.plugin
   * @param entityId
   *                   The entity id if you multiple properties per entity name.
   *                   (E.g. OSUser properties per user id)
   * @param bulkLoad
   *                   If true, all properties will be loaded during
   *                   initialisation of the accessor and cached
   * @return a {@link IPropertyAccessor} backed by a caching property set
   */
  @Nonnull
  IPropertyAccessor buildCaching(@Nonnull String entityName, @Nonnull Long entityId, boolean bulkLoad);

  /**
   * Returns a caching {@link IPropertyAccessor} that wraps the provided accessor.
   * Entries in this set are cached in
   * memory for better performance. Please note that this is a
   * write-through-cache, meaning that reads will be cached
   * up, however any write will call through to the database and invalidate the
   * relevant cache entry.
   *
   * @param accessor
   *                 A IPropertyAccessor to wrap by a caching property set
   * @param bulkLoad
   *                 If true, all properties will be loaded during initialisation
   *                 of the accessor and cached
   * @return a {@link IPropertyAccessor} backed by a caching property set
   */
  @Nonnull
  IPropertyAccessor buildCaching(@Nonnull IPropertyAccessor accessor, boolean bulkLoad);

  /**
   * <p>
   * buildProperties.
   * </p>
   *
   * @param propertyFile
   *                     a {@link java.io.File} object.
   * @return a {@link com.opensymphony.module.propertyset.PropertySet} object.
   */
  @Nonnull
  IPropertyAccessor buildProperties(@Nonnull File propertyFile);

  /**
   * <p>
   * buildAggregator.
   * </p>
   *
   * @param accessor
   *                 a {@link IPropertyAccessor} object.
   * @return a {@link IPropertyAccessor} object.
   */
  @Nonnull
  IPropertyAccessor buildAggregator(@Nonnull IPropertyAccessor... accessor);

  /**
   * Returns an in memory copy of a property set from the database. This property
   * set will not have its configuration
   * saved to the database on each change. It is up to the caller of this method
   * to manually synchronize the returned
   * property set with the database.
   *
   * @param entityName
   *                   The entity name to lookup your properties. E.g.
   *                   app.svn.plugin
   * @param entityId
   *                   The entity id if you multiple properties per entity name.
   *                   (E.g. OSUser properties per user id)
   * @return a {@link IPropertyAccessor} held completely in memory. Changes will
   *         not be written to the database.
   */
  @Nonnull
  IPropertyAccessor buildPersitentMemory(@Nonnull String entityName, @Nonnull Long entityId);

  /**
   * @return
   */
  @Nonnull
  IPropertyAccessor buildMemory();

  /**
   * @param entityName
   * @param type
   * @param key
   * @param value
   * @return
   * @since 2.0
   */
  Optional<Long> findByValue(@Nonnull final String entityName,
      final int type,
      @Nonnull final String key,
      @Nonnull final Object value);
}
