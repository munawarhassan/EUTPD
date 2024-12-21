package com.pmi.tpd.keystore;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.crypto.IKeyManagerProvider;
import com.pmi.tpd.api.crypto.KeyPair;
import com.pmi.tpd.keystore.model.KeyStoreEntry;

/**
 * Provides methods for querying, <i>storing and updating</i> {@link KeyPair
 * KeyPair} and {@link Certificate
 * Certificate} stored in unique {@link java.security.KeyStore KeyStore} used by
 * application.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IKeyStoreService extends IKeyManagerProvider {

  /**
   * @return Returns the keystore configuration.
   * @since 2.2
   */
  @Nonnull
  KeyStoreProperties getConfiguration();

  /**
   * @param alias
   * @return
   */
  @Nullable
  KeyStoreEntry get(@Nonnull String alias);

  /**
   * Retrieves a page of keystore entries, optionally filtering using
   * {@link com.pmi.tpd.api.paging.FilteredPagingRequest FilteredPagingRequest}
   * the returned results to those
   * containing the specified filter.
   *
   * @param pageRequest
   *                    defines the page of keystore entry to retrieve (can
   *                    <b>not</b> be {@code null}).
   * @return Returns the requested page of keystore entries, potentially filtered,
   *         which may be empty but never
   *         {@code null}.
   */
  @Nonnull
  Page<KeyStoreEntry> findAll(@Nonnull Pageable pageRequest);

  /**
   * @param pageRequest
   * @param date
   * @return
   * @since 2.2
   */
  @Nonnull
  Page<KeyStoreEntry> findAllWithExpireDateBefore(@Nonnull final Pageable pageRequest, @Nonnull final DateTime date);

  /**
   * Gets all keystore entries.
   *
   * @return Returns a {@link List} of all keystore entry, which may be empty but
   *         never {@code null}.
   */
  @Nonnull
  List<KeyStoreEntry> getEntries();

  /**
   * Gets all alias names.
   *
   * @return Returns a {@link List} of all alias names, which may be empty but
   *         never {@code null}.
   */
  @Nonnull
  List<String> getAliases();

  /**
   * Deletes the entry identified by the given alias.
   *
   * @param alias
   *              the alias name (can <b>not</b> be {@code null} or empty).
   */
  void remove(@Nonnull String alias);

  /**
   * Stores or updates a key pair with a specific alias name.
   *
   * @param keypair
   *                a key pair entry to store (can <b>not</b> be {@code null}).
   * @param alias
   *                the alias name (can <b>not</b> be {@code null}).
   */
  void storeKey(@Nonnull KeyPair keypair, @Nonnull String alias);

  /**
   * Stores or updates a key pair with a specific alias name.
   *
   * @param keypair
   *                 a key pair entry to store (can <b>not</b> be {@code null} ).
   * @param alias
   *                 the alias name (can <b>not</b> be {@code null} or empty).
   * @param password
   *                 The password to protect the KeyStore with (can <b>not</b> be
   *                 {@code null} or empty).
   */
  void storeKey(@Nonnull KeyPair keypair, @Nonnull String alias, @Nonnull String password);

  /**
   * @param keypairFile
   * @param password
   * @return
   */
  String validateKeyPair(final InputStream keypairFile, final String password);

  /**
   * Stores or updates a certificate with a specific alias name.
   *
   * @param certificate
   *                    a certificate to store (can <b>not</b> be {@code null}).
   * @param alias
   *                    the alias name (can <b>not</b> be {@code null} or empty).
   */
  void storeCertificate(@Nonnull Certificate certificate, @Nonnull String alias);

  /**
   * Gets indicating whether alias exists.
   *
   * @param alias
   *              the alias name (can <b>not</b> be {@code null}).
   * @return Returns {@code true} whether alias exists ,otherwise {@code false}.
   */
  boolean isEntryExist(@Nonnull String alias);

}
