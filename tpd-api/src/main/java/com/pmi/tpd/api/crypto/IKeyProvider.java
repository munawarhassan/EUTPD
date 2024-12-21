package com.pmi.tpd.api.crypto;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides KeyPair and {@link Certificate Certificate} stored in
 * {@link java.security.KeyStore KeyStore} used by
 * application.
 * <p>
 * This provider used in unsecured action.
 * </p>
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IKeyProvider {

  /**
   * Gets the key associated with the given alias.
   *
   * @param alias
   *              the alias name (can <b>not</b> be {@code null} or empty).
   * @return Returns the key associated with the given alias, or {@code null} if
   *         alias doesn't exist.
   */
  @Nonnull
  Optional<Key> getKey(@Nonnull String alias);

  /**
   * Gets the certificate associated with the given alias.
   *
   * @param alias
   *              the alias name (can <b>not</b> be {@code null} or empty).
   * @return Returns the {@link Certificate}, or {@code null} if the given alias
   *         does not exist or does not contain a
   *         certificate, or {@code null} if alias doesn't exist.
   */
  @Nonnull
  Optional<Certificate> getCertificate(@Nonnull String alias);

  /**
   * Gets the key associated with the given alias.
   *
   * @param alias
   *                 the alias name (can <b>not</b> be {@code null} or empty).
   * @param password
   *                 the password for recovering the key (can <b>not</b> be
   *                 {@code null} or empty).
   * @return Returns the key associated with the given alias, using the given
   *         password to recover it, or {@code null}
   *         if alias doesn't exist.
   */
  @Nonnull
  Optional<Key> getKey(@Nonnull String alias, @Nonnull String password);
}
