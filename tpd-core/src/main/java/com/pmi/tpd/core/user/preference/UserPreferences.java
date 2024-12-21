package com.pmi.tpd.core.user.preference;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.exception.ApplicationException;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.user.preference.spi.IUserPropertyManager;

/**
 * <p>
 * UserPreferences class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class UserPreferences implements IPreferences, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -7252715001573642947L;

  /** */
  private Supplier<IPropertyAccessor> backingPSSupplier = Suppliers.ofInstance(null);

  /** stores the keys for preferences that the default values should be used. */
  private final Set<String> defaultKeys = new HashSet<>();

  /** */
  private IUserPropertyManager userPropertyManager;

  /** */
  private IPreferences defaultPreference;

  /** */
  private Long id;

  /**
   * <p>
   * Constructor for UserPreferences.
   * </p>
   *
   * @param user
   *                            a {@link com.pmi.tpd.api.user.IUser} object.
   * @param userPropertyManager
   *                            a
   *                            {@link com.pmi.tpd.core.user.preference.spi.IUserPropertyManager}
   *                            object.
   */
  public UserPreferences(@Nonnull final IUserPropertyManager userPropertyManager,
      @Nonnull final IPreferences defaultPreference, @Nullable final IUser user) {
    this.userPropertyManager = checkNotNull(userPropertyManager, "userPropertyManager");
    this.defaultPreference = checkNotNull(defaultPreference, "defaultPreference");
    if (user != null) {
      this.id = user.getId();
      backingPSSupplier = () -> UserPreferences.this.userPropertyManager.getPropertySetAccessor(user);
    }
  }

  /**
   * <p>
   * Constructor for UserPreferences.
   * </p>
   *
   * @param user
   *             a {@link com.pmi.tpd.api.user.IUser} object.
   */
  public UserPreferences(final IUser user) {
    if (user != null) {
      backingPSSupplier = () -> userPropertyManager.getPropertySetAccessor(user);
    }
  }

  /**
   * <p>
   * Constructor for UserPreferences.
   * </p>
   *
   * @param userPs
   *               a {@link IPropertyAccessor} object.
   */
  public UserPreferences(final IPropertyAccessor userPs) {
    if (userPs != null) {
      backingPSSupplier = Suppliers.ofInstance(userPs);
    }
  }

  /**
   * @return
   */
  @Override
  public Long getId() {
    return id;
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
      throw new ApplicationException("Trying to remove a property on a null user this is not allowed");
    } else {
      return backingPS.exists(key);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Long> getLong(final String key) {
    // Check if the default value should be used for this key
    if (defaultKeys.contains(key)) {
      // If the default value is used for the key just return it
      return defaultPreference.getLong(key);
    } else {
      final IPropertyAccessor backingPS = backingPSSupplier.get();
      if (backingPS != null && backingPS.exists(key)) {
        return backingPS.getLong(key);
      } else {
        // Remember that the default value for this key is used for the user
        // So that we do not have look it up again
        defaultKeys.add(key);
        // Return the default value
        return defaultPreference.getLong(key);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setLong(final String key, final long i) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      // Do not use the default value (if one was not used before, it does not matter)
      defaultKeys.remove(key);
      backingPS.setLong(key, i);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<String> getString(final String key) {
    // Check if the default value should be used for this key
    if (defaultKeys.contains(key)) {
      // If the default value is used for the key just return it
      return defaultPreference.getString(key);
    } else {
      final IPropertyAccessor backingPS = backingPSSupplier.get();
      if (backingPS != null && backingPS.exists(key)) {
        return backingPS.getString(key);
      } else {
        // Remember that the default value for this key is used for the user
        defaultKeys.add(key);
        return defaultPreference.getString(key);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setString(final String key, final String value) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      // Do not use the default value (if one was not used before, it does not matter)
      defaultKeys.remove(key);
      backingPS.setString(key, value);
    }
  }

  @Override
  public Optional<Date> getDate(final String key) {
    // Check if the default value should be used for this key
    if (defaultKeys.contains(key)) {
      // If the default value is used for the key just return it
      return defaultPreference.getDate(key);
    } else {
      final IPropertyAccessor backingPS = backingPSSupplier.get();
      if (backingPS != null && backingPS.exists(key)) {
        return backingPS.getDate(key);
      } else {
        // Remember that the default value for this key is used for the user
        defaultKeys.add(key);
        return defaultPreference.getDate(key);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDate(final String key, final Date value) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      // Do not use the default value (if one was not used before, it does not matter)
      defaultKeys.remove(key);
      backingPS.setDate(key, value);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Optional<Boolean> getBoolean(final String key) {
    // Check if the default value should be used for this key
    if (defaultKeys.contains(key)) {
      // If the default value is used for the key just return it
      return defaultPreference.getBoolean(key);
    } else {
      final IPropertyAccessor backingPS = backingPSSupplier.get();
      if (backingPS != null && backingPS.exists(key)) {
        return backingPS.getBoolean(key);
      } else {
        // Remember that the default value for this key is used for the user
        defaultKeys.add(key);
        return defaultPreference.getBoolean(key);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBoolean(final String key, final boolean b) throws ApplicationException {
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      throw new ApplicationException("Trying to set a property on a null user this is not allowed");
    } else {
      // Do not use the default value (if one was not used before, it does not matter)
      defaultKeys.remove(key);
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
        // Do not use the default value (if one was not used before, it does not matter)
        defaultKeys.remove(key);
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
    if (!(o instanceof UserPreferences)) {
      return false;
    }

    final UserPreferences appUserPreferences = (UserPreferences) o;
    final IPropertyAccessor backingPS = backingPSSupplier.get();
    if (backingPS == null) {
      return appUserPreferences.backingPSSupplier.get() == null;
    } else {
      return backingPS.identical(appUserPreferences.backingPSSupplier.get());
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
