package com.pmi.tpd.spring.i18n;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import com.pmi.tpd.api.i18n.I18nKey;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * An {@linkI18nService} that uses a {@link MessageSource} for message
 * generation.
 */
public class MessageSourceI18nService implements I18nService {

  /** */
  private static final Object[] NO_ARGS = {};

  /** */
  private final MessageSource messageSource;

  /** */
  private final Locale defaultLocale;

  /**
   * @param messageSource
   * @param defaultLocale
   */
  public MessageSourceI18nService(@Nonnull final MessageSource messageSource, @Nonnull final Locale defaultLocale) {
    this.messageSource = messageSource;
    this.defaultLocale = defaultLocale;
  }

  @Override
  @Nonnull
  public KeyedMessage createKeyedMessage(@Nonnull final String key, @Nonnull final Object... arguments) {
    return getKeyedText(key, key, arguments);
  }

  @Override
  @Nonnull
  public Map<String, String> getAllTranslationsForPrefix(@Nonnull final String prefix) {
    return Collections.emptyMap();
  }

  @Override
  @Nonnull
  public Map<String, String> getAllTranslationsForPrefix(@Nonnull final String prefix, @Nonnull final Locale locale) {
    return Collections.emptyMap();
  }

  @Override
  @Nonnull
  public KeyedMessage getKeyedText(@Nonnull final I18nKey i18nKey) {
    return getKeyedText(i18nKey.getKey(), null, i18nKey.getArguments());
  }

  @Override
  @Nonnull
  public KeyedMessage getKeyedText(@Nonnull final String key,
      final String fallbackMessage,
      @Nonnull final Object... arguments) {
    final String message = getText(key, fallbackElseKey(key, fallbackMessage), arguments);

    return new KeyedMessage(key, message, message);
  }

  @Override
  @Nonnull
  public String getMessage(@Nonnull final String key, @Nonnull final Object... arguments) {
    return getText(key, key, arguments);
  }

  @Override
  @Nonnull
  public String getMessage(@Nonnull final I18nKey i18nKey) {
    return getText(i18nKey.getKey(), i18nKey.getKey(), i18nKey.getArguments());
  }

  @Override
  @Nonnull
  public String getMessagePattern(@Nonnull final String key) {
    return getMessagePattern(key, key);
  }

  @Override
  public String getMessagePattern(@Nonnull final Locale locale, @Nonnull final String key) {
    return messageSource.getMessage(key, NO_ARGS, key, locale);
  }

  @Override
  public String getMessagePattern(@Nonnull final String key, final String fallbackMessage) {
    return messageSource.getMessage(key, NO_ARGS, fallbackElseKey(key, fallbackMessage), defaultLocale);
  }

  @Override
  @Nonnull
  public String getText(@Nonnull final Locale locale,
      @Nonnull final String key,
      final String fallbackMessage,
      @Nonnull final Object... arguments) {
    return messageSource.getMessage(key, arguments, fallbackElseKey(key, fallbackMessage), locale);
  }

  @Override
  public String getText(@Nonnull final String key, final String fallbackMessage, @Nonnull final Object... arguments) {
    return messageSource.getMessage(key, arguments, fallbackElseKey(key, fallbackMessage), defaultLocale);
  }

  private String fallbackElseKey(final String key, final String fallbackMessage) {
    return fallbackMessage == null ? key : fallbackMessage;
  }

  @Override
  public String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) {
    return messageSource.getMessage(code, args, defaultMessage, locale);
  }

  @Override
  public String getMessage(final String code, final Object[] args, final Locale locale)
      throws NoSuchMessageException {
    return messageSource.getMessage(code, args, locale);
  }

  @Override
  public String getMessage(final MessageSourceResolvable resolvable, final Locale locale)
      throws NoSuchMessageException {
    return messageSource.getMessage(resolvable, locale);
  }
}
