package com.pmi.tpd.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.exception.ServiceException;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * Base type for all exceptions which are thrown while processing user
 * authentication attempts.
 * <p>
 * This base type is provided to stem the authentication exception hierarchy.
 * However, it will usually not be desirable
 * to process exceptions at this level. Instead, callers are encouraged to catch
 * specific exception types where they
 * wish to handle failures.
 *
 * @author Christophe Friederich
 * @since 2.0
 * @see IUserService#authenticate(String, String)
 * @see IUserService#updatePassword(String, String)
 */
public abstract class AuthenticationException extends ServiceException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public AuthenticationException(@Nonnull final KeyedMessage message) {
    super(message);
  }

  public AuthenticationException(@Nonnull final KeyedMessage message, @Nullable final Throwable cause) {
    super(message, cause);
  }
}
