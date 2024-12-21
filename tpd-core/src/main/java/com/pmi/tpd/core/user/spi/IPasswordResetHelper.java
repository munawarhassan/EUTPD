package com.pmi.tpd.core.user.spi;

import java.util.Optional;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.user.IUser;

/**
 * Password Helper allow to generate or convert password in application.
 *
 * @author Christophe Friederich
 * @since 2.0
 */
public interface IPasswordResetHelper {

  /**
   * Gets the password token to the specific user if exists and not expired.
   *
   * @param user
   *             the user to use (can <b>not</b> be {@code null}).
   * @return Returns a {@link String} representing the password token, or
   *         {@code null} if doesn't exists and has
   *         expired.
   */
  Optional<String> getPasswordToken(@Nonnull IUser user);

  /**
   * Reset the password of {@code user}.
   *
   * @param user
   *                   the user to use (can <b>not</b> be {@code null}).
   * @param newPassord
   *                   the new password of user (can <b>not</b> be {@code null}).
   */
  void resetPassword(@Nonnull IUser user, @Nonnull String newPassord);

  /**
   * @param username
   *                 the user to use (can <b>not</b> be {@code null}).
   * @return Returns a {@link String} representing the new generated password
   *         token, or {@code null} if doesn't exists
   *         and has expired.
   */
  String addResetPasswordToken(@Nonnull String username);

  /**
   * Encode the {@code password} with specific encryption.
   *
   * @param password
   *                 the password to encode (can <b>not</b> be {@code null}).
   * @return Return a {@link String} representing the encoded password.
   */
  @Nonnull
  String encodePassord(@Nonnull String password);

  /**
   * Find the user associated to the {@code token}.
   *
   * @param token
   *              the token associated to user
   * @return Returns the {@link IUser user} associated to {@code token}.
   */
  Optional<IUser> findUserByResetToken(String token);

  /**
   * @return Returns a new isntance {@link String} representing a generated
   *         password.
   */
  @Nonnull
  String generatePassword();
}