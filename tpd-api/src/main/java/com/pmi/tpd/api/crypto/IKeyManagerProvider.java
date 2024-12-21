package com.pmi.tpd.api.crypto;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.annotation.Nonnull;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 * @author christophe friederich
 * @since 2.5
 */
public interface IKeyManagerProvider {

  /**
   * @return Returns one key manager for each type of key material.
   * @throws NoSuchAlgorithmException
   *                                   if the specified algorithm is not available
   *                                   from the specified provider.
   * @throws UnrecoverableKeyException
   *                                   if the key cannot be recovered (e.g. the
   *                                   given password is wrong).
   * @throws KeyStoreException
   *                                   if this operation fails
   */
  @Nonnull
  KeyManager[] getKeyManagers() throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException;

  /**
   * @return
   * @throws NoSuchAlgorithmException
   *                                  if no Provider supports a
   *                                  TrustManagerFactorySpi implementation for
   *                                  the specified algorithm
   * @throws KeyStoreException
   *                                  if this operation fails
   */
  @Nonnull
  TrustManager[] getTrustManagers() throws NoSuchAlgorithmException, KeyStoreException;
}
