package com.pmi.tpd.keystore;

import javax.annotation.Nonnull;

import com.pmi.tpd.keystore.model.KeyStoreEntry;
import com.pmi.tpd.keystore.preference.IKeyStorePreferences;

/**
 * A simple manager for retrieving, caching and updating keystore preferences
 * objects.
 *
 * @author Christophe Friederich
 * @since 2.2
 */
public interface IKeyStorePreferencesManager {

  /**
   * <p>
   * getPreferences.
   * </p>
   *
   * @return The keystore preferences for a key entry, or null if the key is null
   * @param key
   *            a {@link KeyStoreEntry} object.
   */
  @Nonnull
  IKeyStorePreferences getPreferences(KeyStoreEntry key);

  /**
   * <p>
   * getPreferences.
   * </p>
   *
   * @return The keystore preferences for a key entry, or null if the key is null
   * @param alias
   *              alias of certificate.
   */
  @Nonnull
  IKeyStorePreferences getPreferences(String alias);

  /**
   * Clear all cached preferences.
   */
  void clearCache();

  /**
   * Clear any cached preferences for a given key entry.
   *
   * @param alias
   *              alias of certificate
   */
  void clearCache(String alias);

}
