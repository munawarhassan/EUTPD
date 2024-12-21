package com.pmi.tpd.api.i18n;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class KeyedMessage {

  /** */
  private final String key;

  /** */
  private final String localisedMessage;

  /** */
  private final String rootMessage;

  /**
   * @param key
   *                         a i18n key of message.
   * @param localisedMessage
   *                         the localised message
   * @param rootMessage
   *                         the root message
   */
  public KeyedMessage(@Nonnull final String key, @Nonnull final String localisedMessage,
      @Nonnull final String rootMessage) {
    this.key = checkNotNull(key, "key");
    this.localisedMessage = checkNotNull(localisedMessage, "localisedMessage");
    this.rootMessage = checkNotNull(rootMessage, "rootMessage");
  }

  /**
   * @return Returns i18n key of message.
   */
  @Nonnull
  public String getKey() {
    return key;
  }

  /**
   * @return Returns the localised message.
   */
  @Nonnull
  public String getLocalisedMessage() {
    return localisedMessage;
  }

  /**
   * @return Returns the root message.
   */
  @Nonnull
  public String getRootMessage() {
    return rootMessage;
  }
}
