package com.pmi.tpd.keystore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.context.IPropertyAccessor;

/**
 * <p>
 * ICertificatePropertyManager interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IKeyStorePropertyManager {

  /**
   * Get the property set associated with a user.
   *
   * @param alias
   *              the property set is associated with.
   * @return Property set.
   */
  @Nullable
  IPropertyAccessor getPropertySet(@Nonnull String alias);

  /**
   * <p>
   * clearCache.
   * </p>
   *
   * @param alias
   *              alias of certificate.
   */
  void clearCache(@Nonnull String alias);

}
