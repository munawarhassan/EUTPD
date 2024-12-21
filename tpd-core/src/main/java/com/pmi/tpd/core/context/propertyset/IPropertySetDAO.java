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

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.propertyset.PropertySetId;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;

/**
 * <p>
 * IPropertySetDAO interface.
 * </p>
 *
 * @author devacfr
 * @since 1.0
 */
public interface IPropertySetDAO {

  /**
   * Save the implementation of a PropertySetItem.
   *
   * @param item
   *             a {@link com.pmi.tpd.core.model.propertyset.PropertySetItem}
   *             object.
   */
  void persist(@Nonnull PropertySetItem item);

  /**
   * <p>
   * getKeys.
   * </p>
   *
   * @param entityName
   *                   a {@link java.lang.String} object.
   * @param entityId
   *                   a {@link java.lang.Long} object.
   * @param prefix
   *                   a {@link java.lang.String} object.
   * @param type
   *                   a int.
   * @return a {@link java.util.List} object.
   */
  @Nonnull
  List<String> getKeys(String entityName, Long entityId, String prefix, int type);

  /**
   * <p>
   * findByKey.
   * </p>
   *
   * @param entityName
   *                   a {@link java.lang.String} object.
   * @param entityId
   *                   a {@link java.lang.Long} object.
   * @param key
   *                   a {@link java.lang.String} object.
   * @return a {@link com.pmi.tpd.core.model.propertyset.PropertySetItem} object.
   */
  @Nonnull
  Optional<PropertySetItem> findByKey(String entityName, Long entityId, String key);

  /**
   * @param entityName
   * @param key
   * @param type
   * @param value
   * @return
   * @since 2.0
   */
  @Nonnull
  Optional<PropertySetItem> findOneByValue(String entityName, String key, int type, Object value);

  /**
   * @param entityName
   * @param key
   * @param type
   * @param value
   * @return
   * @since 2.0
   */
  @Nonnull
  List<PropertySetItem> findAllByValue(String entityName, String key, final int type, final Object value);

  /**
   * @param entityName
   * @param key
   * @param type
   * @param value
   * @param request
   * @return
   * @since 2.0
   */
  @Nonnull
  Page<PropertySetItem> findAllByValue(String entityName,
      String key,
      final int type,
      final Object value,
      final Pageable request);

  /**
   * <p>
   * remove.
   * </p>
   *
   * @param entityName
   *                   a {@link java.lang.String} object.
   * @param entityId
   *                   a {@link java.lang.Long} object.
   * @param key
   *                   a {@link java.lang.String} object.
   */
  void remove(String entityName, Long entityId, String key);

  /**
   * <p>
   * remove.
   * </p>
   *
   * @param entityName
   *                   a {@link java.lang.String} object.
   * @param entityId
   *                   a {@link java.lang.Long} object.
   */
  void remove(String entityName, Long entityId);

  /**
   * Returns whether an entity with the given id exists.
   *
   * @param id
   *           must not be {@literal null}.
   * @return true if an entity with the given id exists, {@literal false}
   *         otherwise
   * @throws IllegalArgumentException
   *                                  if {@code id} is {@literal null}
   */
  boolean existsById(PropertySetId id);

}
