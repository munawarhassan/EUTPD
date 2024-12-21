package com.pmi.tpd.api.i18n;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.context.MessageSource;

/**
 * Serves localized text from properties files. Fallback messages should never
 * be specified by internal developers. All
 * internal messages should be added to properties files for translation. The
 * variants accepting a fallback message are
 * intended for <i>external plugin developers only</i>. Internal developers
 * should use:
 * <ul>
 * <li>{@link #getMessage(String, Object[])}</li>
 * <li>{@link #createKeyedMessage(String, Object[])}</li>
 * <li>{@link #getMessagePattern(String)}</li>
 * </ul>
 */
public interface I18nService extends MessageSource {

  /**
   * Given a property key and a list of arguments, it returns:
   * <ol>
   * <li>a {@link KeyedMessage} that composes the localized and formatted message
   * for the current locale, the
   * localized and formatted message for the root locale and the original key for
   * the message. Otherwise...</li>
   * <li>if the message lookup for the key fails a KeyedMessage with the given key
   * and a string representation of the
   * arguments supplied</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available.
   * <p>
   * The KeyedMessage returned is useful for priming Exceptions where the
   * localized and non-localized messages need to
   * differ to ensure the end use sees the message localized in their language but
   * the logs record the message for the
   * root locale.
   *
   * @param key
   *                  the key for the i18ned message
   * @param arguments
   *                  optional list of arguments for the message.
   * @return a KeyedMessage
   */
  @Nonnull
  KeyedMessage createKeyedMessage(@Nonnull String key, @Nonnull Object... arguments);

  /**
   * Given a property key and a list of arguments, it returns:
   * <ol>
   * <li>the localized text formatted with the given arguments if the key can be
   * resolved. Otherwise...</li>
   * <li>the key and a string representation of the arguments supplied</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available
   *
   * @param key
   *                  the key for the i18ned message
   * @param arguments
   *                  optional list of arguments for the message.
   * @return I18ned string
   */
  @Nonnull
  String getMessage(@Nonnull String key, @Nonnull Object... arguments);

  /**
   * Given a {@link I18nKey key} it returns:
   * <ol>
   * <li>the localized text formatted with the given arguments if the key can be
   * resolved. Otherwise...</li>
   * <li>the key and a string representation of the arguments supplied</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available
   *
   * @param key
   *            the key for the i18ned message
   * @return I18ned string
   */
  @Nonnull
  String getMessage(@Nonnull I18nKey key);

  /**
   * Given a property key, a locale, a fallback text message and a list of
   * arguments, it returns:
   * <ol>
   * <li>the localized text formatted with the given arguments if the key can be
   * resolved. Otherwise...</li>
   * <li>the fallbackMessage formatted with the arguments will be returned if
   * non-null. Otherwise...</li>
   * <li>the key and a string representation of the arguments supplied</li>
   * </ol>
   *
   * @param locale
   *                        the locale for the lookup
   * @param key
   *                        key for the i18ned message
   * @param fallbackMessage
   *                        the optional message to fallback to if lookup fails
   * @param arguments
   *                        optional list of arguments for the message.
   * @return I18ned string
   */
  @Nonnull
  String getText(@Nonnull Locale locale,
      @Nonnull String key,
      @Nullable String fallbackMessage,
      @Nonnull Object... arguments);

  /**
   * Given a property key, the fallback text message and a list of arguments, it
   * returns:
   * <ol>
   * <li>the localized text formatted with the given arguments if the key can be
   * resolved. Otherwise...</li>
   * <li>the fallbackMessage formatted with the arguments will be returned if
   * non-null. Otherwise...</li>
   * <li>the key and a string representation of the arguments supplied</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available
   *
   * @param key
   *                        the key for the i18ned message
   * @param fallbackMessage
   *                        the optional message to fallback to if lookup fails
   * @param arguments
   *                        optional list of arguments for the message.
   * @return I18ned string
   */
  @Nonnull
  String getText(@Nonnull String key, @Nullable String fallbackMessage, @Nonnull Object... arguments);

  /**
   * Given a property key it returns:
   * <ol>
   * <li>the associated message pattern if the key can be resolved.
   * Otherwise...</li>
   * <li>null</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available
   *
   * @param key
   *            the key for the i18n pattern
   * @return a message pattern suitable for use with
   *         {@code java.text.MessageFormat} or with {@code AJS.format()}
   */
  @Nullable
  String getMessagePattern(@Nonnull String key);

  /**
   * Given a property key and the fallback text message, it returns:
   * <ol>
   * <li>the associated message pattern if the key can be resolved.
   * Otherwise...</li>
   * <li>the fallbackPattern</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available
   *
   * @param key
   *                        the key for the i18n pattern
   * @param fallbackPattern
   *                        the optional pattern to fallback to if lookup fails
   * @return a message pattern suitable for use with
   *         {@code java.text.MessageFormat} or with {@code AJS.format()}
   */
  @Nullable
  String getMessagePattern(@Nonnull String key, @Nullable String fallbackPattern);

  /**
   * Given a locale and a property key it returns:
   * <ol>
   * <li>the localized message pattern if the key can be resolved.
   * Otherwise...</li>
   * <li>null</li>
   * </ol>
   *
   * @param locale
   *               the locale for the lookup
   * @param key
   *               the key for the i18n pattern
   * @return a message pattern suitable for use with
   *         {@code java.text.MessageFormat} or with {@code AJS.format()}
   */
  @Nullable
  String getMessagePattern(@Nonnull Locale locale, @Nonnull String key);

  /**
   * Given an {@link I18nKey} it returns:
   * <ol>
   * <li>a {@link KeyedMessage} that composes the localized and formatted message
   * for the current locale, the
   * localized and formatted message for the root locale and the original key for
   * the message. Otherwise...</li>
   * <li>if the message lookup for the key fails a KeyedMessage with the given key
   * and a string representation of the
   * arguments supplied</li>
   * </ol>
   * The locale used is the one for the request currently in scope or the default
   * locale if that is not available.
   * <p>
   * The KeyedMessage returned is useful for priming Exceptions where the
   * localized and non-localized messages need to
   * differ to ensure the end use sees the message localized in their language but
   * the logs record the message for the
   * root locale. The result of this method should be the same as calling
   * {@code #createKeyedMessage(i18nKey.getKey(), i18nKey.getArguments())}
   * </p>
   *
   * @param i18nKey
   *                the key for the i18ned message
   * @return a KeyedMessage
   */
  @Nonnull
  KeyedMessage getKeyedText(@Nonnull I18nKey i18nKey);

  /**
   * Given a property key, a fallback message and a list of arguments, it returns:
   * <ol>
   * <li>a {@link KeyedMessage} that composes the localized and formatted message
   * for the current locale, the
   * localized and formatted message for the root locale and the original key for
   * the message. Otherwise...</li>
   * <li>if the message lookup for the key fails for either the current locale or
   * the root locale, the formatted
   * fallback message is used if non-null. Otherwise...</li>
   * <li>a KeyedMessage with the given key and a string representation of the
   * arguments supplied</li>
   * </ol>
   * The current locale used is the one for the request currently in scope or the
   * default locale if that is not
   * available.
   * <p>
   * The KeyedMessage returned is useful for priming Exceptions where the
   * localized and non-localized messages need to
   * differ to ensure the end user sees the message localized in their language
   * but the logs record the message for
   * the root locale
   * </p>
   *
   * @param key
   *                        the key for the i18ned message
   * @param fallbackMessage
   *                        the optional message to fallback to if lookup fails
   * @param arguments
   *                        optional list of arguments for the message.
   * @return a KeyedMessage
   */
  @Nonnull
  KeyedMessage getKeyedText(@Nonnull String key, @Nullable String fallbackMessage, @Nonnull Object... arguments);

  /**
   * Given a property key prefix, this method will return all translations where
   * the key starts with the given prefix
   * as key -&gt; value mappings using the current locale. The current locale used
   * is the one for the request currently
   * in scope or the default locale if that is not available.
   *
   * @param prefix
   *               the prefix for a particular key to start with. Empty string
   *               will match everything, which may be slow.
   *               Throws {@code NullPointerException} if {@code null}.
   * @return a {@link Map} of i18nKey -&gt; translation mappings where i18nKey
   *         starts with the prefix. An empty map if no
   *         matches.
   * @throws NullPointerException
   *                              if {@code link} is {@code null}
   */
  @Nonnull
  Map<String, String> getAllTranslationsForPrefix(@Nonnull String prefix);

  /**
   * Given a property key prefix, this method will return all translations where
   * the key starts with the given prefix
   * as key -&gt; value mappings.
   *
   * @param prefix
   *               the prefix for a particular key to start with. Empty string
   *               will match everything, which may be slow.
   *               Throws {@code NullPointerException} if {@code null}.
   * @param locale
   *               the locale for which to lookup translations. Throws
   *               {@code NullPointerException} if {@code null}.
   * @return a {@link Map} of i18nKey -&gt; translation mappings where i18nKey
   *         starts with the prefix. An empty map if no
   *         matches.
   * @throws NullPointerException
   *                              if {@code prefix} or {@code link} are
   *                              {@code null}
   */
  @Nonnull
  Map<String, String> getAllTranslationsForPrefix(@Nonnull String prefix, @Nonnull Locale locale);
}
