package com.pmi.tpd.keystore.preference;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.keystore.IKeyStorePropertyManager;

/**
 * <p>
 * UserPreferences class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class KeyStorePreferences implements IKeyStorePreferences, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -7252715001573642947L;

  /** */
  private Supplier<IPropertyAccessor> backingPSSupplier = Suppliers.ofInstance(null);

  /** */
  private IKeyStorePropertyManager propertyManager;

  /** */
  private String alias;

  /**
   * <p>
   * Constructor for KeyStorePreferences.
   * </p>
   *
   * @param alias
   *                            alias certificate
   * @param userPropertyManager
   *                            a {@link IKeyStorePropertyManager} object.
   */
  public KeyStorePreferences(@Nonnull final IKeyStorePropertyManager propertyManager, @Nullable final String alias) {
    this.propertyManager = checkNotNull(propertyManager, "propertyManager");
    if (alias != null) {
      this.alias = alias;
      backingPSSupplier = () -> KeyStorePreferences.this.propertyManager.getPropertySet(alias);
    }
  }

  /**
   * <p>
   * Constructor for KeyStorePreferences.
   * </p>
   *
   * @param alias
   *              alias certificate.
   */
  public KeyStorePreferences(@Nullable final String alias) {
    if (alias != null) {
      backingPSSupplier = () -> propertyManager.getPropertySet(alias);
    }
  }

  /**
   * <p>
   * Constructor for UserPreferences.
   * </p>
   *
   * @param userPs
   *               a {@link com.opensymphony.module.propertyset.PropertySet}
   *               object.
   */
  public KeyStorePreferences(final IPropertyAccessor userPs) {
    if (userPs != null) {
      backingPSSupplier = Suppliers.ofInstance(userPs);
    }
  }

  /**
   * @return
   */
  @Override
  public String getAlias() {
    return alias;
  }

  /**
   * {@inheritDoc}
   *
   * @throws ApplicationException
   */
  @Override
  public boolean exists(final String key) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to remove a property on a null alias this is not allowed");
    } else {
      return backingPS.exists(key);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Long> getLong(final String key) {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS != null && backingPS.exists(key)) {
      return backingPS.getLong(key);
    } else {
      return Optional.of(Long.MIN_VALUE);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setLong(final String key, final long i) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null alias this is not allowed");
    } else {
      backingPS.setLong(key, i);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getString(final String key) {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS != null && backingPS.exists(key)) {
      return backingPS.getString(key);
    } else {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setString(final String key, final String value) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      backingPS.setString(key, value);
    }
  }

  @Override
  public Optional<Date> getDate(final String key) {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS != null && backingPS.exists(key)) {
      return backingPS.getDate(key);
    } else {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDate(final String key, final Date value) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      backingPS.setDate(key, value);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Boolean> getBoolean(final String key) {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS != null && backingPS.exists(key)) {
      return backingPS.getBoolean(key);
    } else {
      return Optional.of(false);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBoolean(final String key, final boolean b) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      backingPS.setBoolean(key, b);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void remove(final String key) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to remove a property on a null user this is not allowed");
    } else {
      if (backingPS.exists(key)) {
        backingPS.remove(key);
      } else {
        throw new ApplicationException("The property with key '" + key + "' does not exist.");
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object o) {
    // NOTE: the defaultKeys is not used to determine equality of this object.
    if (this == o) {
      return true;
    }
    if (!(o instanceof KeyStorePreferences)) {
      return false;
    }

    final KeyStorePreferences preferences = (KeyStorePreferences) o;
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      return preferences.backingPSSupplier.get() == null;
    } else {
      return backingPS.identical(preferences.backingPSSupplier.get());
    }
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    // NOTE: the defaultKeys is not used to determine the hashCode of this object.
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    return backingPS != null ? backingPS.hashCode() : 0;
  }
}
