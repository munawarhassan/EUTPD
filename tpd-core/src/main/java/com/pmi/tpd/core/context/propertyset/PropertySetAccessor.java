package com.pmi.tpd.core.context.propertyset;

import static com.pmi.tpd.api.util.Assert.checkNotNull;
import static java.util.Optional.ofNullable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.api.context.IPropertyAccessor;
import com.pmi.tpd.api.context.IPropertyChangeCallback;

public final class PropertySetAccessor implements IPropertyAccessor {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(PropertySetAccessor.class);

  private final PropertySet delegate;

  public PropertySetAccessor(@Nonnull final PropertySet delegate) {
    this.delegate = checkNotNull(delegate, "delegate");
  }

  public PropertySet getPropertySet() {
    return delegate;
  }

  @Override
  public Optional<String> getText(final String name) {
    return ofNullable(delegate.getText(name));
  }

  @Override
  public void setText(final String name, final @Nullable String value) {
    setValue(name, value, delegate::getText, delegate::setText, null);
  }

  @Override
  public void setText(final @Nonnull String name, final @Nullable String value,
      final @Nullable IPropertyChangeCallback<String> callback) {
    setValue(name, value, delegate::getText, delegate::setText, callback);
  }

  @Override
  public Optional<String> getString(@Nonnull final String name) {

    try {
      return Optional.of(delegate.getString(name));
    } catch (final InvalidPropertyTypeException e) {
      // do nothing. This is a problem because velocity loops over all
      // the properties, even the non-string ones.
      // Yes this is stupid. It is because they use Apache Commons -
      // enough said.
      return Optional.of("");
    } catch (final Exception e) {
      // or occurs during setup or database is not initialised
    }
    return Optional.empty();
  }

  @Override
  public void setString(final String name, final String value) {
    setValue(name, value, delegate::getString, delegate::setString, null);
  }

  @Override
  public void setString(final String name, final String value, final IPropertyChangeCallback<String> callback) {
    setValue(name, value, delegate::getString, delegate::setString, callback);
  }

  @Override
  public Optional<Date> getDate(final String key) {
    return ofNullable(delegate.getDate(key));
  }

  @Override
  public void setDate(final String name, final Date value) {
    setValue(name, value, delegate::getDate, delegate::setDate, null);
  }

  @Override
  public Optional<Long> getLong(final String key) {
    return ofNullable(delegate.getLong(key));
  }

  @Override
  public void setLong(final String name, final Long value) {
    setValue(name, value, delegate::getLong, delegate::setLong, null);

  }

  @Override
  public Optional<Boolean> getBoolean(final String key) {
    return ofNullable(delegate.getBoolean(key));
  }

  @Override
  public void setBoolean(final String name, final Boolean value) {
    setValue(name, value, delegate::getBoolean, delegate::setBoolean, null);
  }

  @Override
  public void setBoolean(final String name, final Boolean value, final IPropertyChangeCallback<Boolean> callback) {
    setValue(name, value, delegate::getBoolean, delegate::setBoolean, callback);
  }

  @Override
  public boolean exists(final String key) {
    return delegate.exists(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public final <T> T getAsActualType(final String key) {
    return (T) delegate.getAsActualType(key);
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public List<String> getKeys() {
    return (List<String>) delegate.getKeys(PropertySet.STRING).stream().collect(Collectors.toUnmodifiableList());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getKeysWithPrefix(final String prefix) {
    return (List<String>) delegate.getKeys(prefix, PropertySet.STRING)
        .stream()
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public void remove(final String key) {
    if (delegate.exists(key)) {
      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Remove property path '{}'", key);
      }
      delegate.remove(key);
    }
  }

  @Override
  public boolean identical(final IPropertyAccessor pThat) {
    if (!(pThat instanceof PropertySetAccessor)) {
      return false;
    }
    return PropertyUtils.identical(delegate, ((PropertySetAccessor) pThat).delegate);
  }

  @Override
  public void flush() {
    if (delegate instanceof CachingPropertySet) {
      ((CachingPropertySet) delegate).flush();
    }
  }

  @Override
  public void refresh() {
    // TODO Auto-generated method stub
  }

  private <T> void setValue(final String name,
      final T value,
      final IPropertyGetFunction<T> getFunction,
      final IPropertySetFunction<T> setFunction,
      final IPropertyChangeCallback<T> callback) {
    T oldValue = null;
    if (value == null) {
      if (delegate.exists(name)) {
        if (callback != null) {
          oldValue = getFunction.get(name);
        }
        delegate.remove(name);
        if (callback != null) {
          callback.change(oldValue, null);
        }
      }
    } else {
      if (delegate.exists(name) && callback != null) {
        oldValue = getFunction.get(name);
      }
      setFunction.set(name, value);
      if (!value.equals(oldValue) && callback != null) {
        callback.change(oldValue, value);
      }
    }
  }

}
