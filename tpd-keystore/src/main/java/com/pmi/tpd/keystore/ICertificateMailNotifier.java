package com.pmi.tpd.keystore;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.exception.MailException;
import com.pmi.tpd.api.exception.NoMailHostConfigurationException;
import com.pmi.tpd.keystore.model.KeyStoreEntry;

/**
 * @author Christophe Friederich
 * @since 2.2
 */
public interface ICertificateMailNotifier {

  /**
   * Sends the specified user an e-mail a certificate should be updated.
   *
   * @param email
   *              the user to send the e-mail to
   * @param token
   *              the token to use in the password reset URL
   * @throws MailException
   *                                          if an e-mail server has been
   *                                          configured but the e-mail could not
   *                                          be sent
   * @throws NoMailHostConfigurationException
   *                                          if an e-mail server has not been
   *                                          configured
   */
  void sendExpiredCertificate(@Nonnull String email, @Nonnull KeyStoreEntry key) throws MailException;
}
