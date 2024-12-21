package com.pmi.tpd.keystore.preference;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.concurrent.ExecutionException;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.lifecycle.ClearCacheEvent;
import com.pmi.tpd.keystore.IKeyStorePreferencesManager;
import com.pmi.tpd.keystore.IKeyStorePropertyManager;
import com.pmi.tpd.keystore.model.KeyStoreEntry;

/**
 * <p>
 * KeyStorePreferencesManager class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 2.2
 */
@Named
@Singleton
public class DefaultKeyStorePreferencesManager implements IKeyStorePreferencesManager {

  /** */
  private final LoadingCache<String, KeyStorePreferences> cache = CacheBuilder.newBuilder()
      .concurrencyLevel(4)
      .maximumSize(1000)
      .build(new CacheLoader<String, KeyStorePreferences>() {

        @Override
        public KeyStorePreferences load(final String alias) {
          return new KeyStorePreferences(propertyManager, alias);
        }
      });

  /** */
  private final IKeyStorePropertyManager propertyManager;

  /**
   * <p>
   * Constructor for KeyStorePreferencesManager.
   * </p>
   *
   * @param userPropertyManager
   *                            a
   *                            {@link com.pmi.tpd.core.user.preference.spi.IUserPropertyManager}
   *                            object.
   */
  @Inject
  public DefaultKeyStorePreferencesManager(@Nonnull final IKeyStorePropertyManager propertyManager) {
    this.propertyManager = checkNotNull(propertyManager, "propertyManager");
  }

  /**
   * <p>
   * onClearCache.
   * </p>
   *
   * @param event
   *              a {@link com.pmi.tpd.core.lifecycle.ClearCacheEvent} object.
   */
  @EventListener
  public void onClearCache(final ClearCacheEvent event) {
    clearCache();
  }

  /** {@inheritDoc} */
  @Override
  public IKeyStorePreferences getPreferences(final KeyStoreEntry key) {
    if (key == null) {
      return new KeyStorePreferences((String) null);
    }

    try {
      return cache.get(key.getAlias());
    } catch (final ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public IKeyStorePreferences getPreferences(final String alias) {
    if (alias == null) {
      return new KeyStorePreferences((String) null);
    }

    try {
      return cache.get(alias);
    } catch (final ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clearCache() {
    cache.invalidateAll();
  }

  /** {@inheritDoc} */
  @Override
  public void clearCache(final String alias) {
    if (alias == null) {
      return;
    }

    cache.invalidate(alias);
    propertyManager.clearCache(alias);
  }
}
