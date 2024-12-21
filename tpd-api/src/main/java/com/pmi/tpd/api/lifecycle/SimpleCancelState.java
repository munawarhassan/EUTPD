package com.pmi.tpd.api.lifecycle;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.KeyedMessage;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class SimpleCancelState implements ICancelState {

  /** */
  private final List<KeyedMessage> messages;

  /**
   *
   */
  public SimpleCancelState() {
    messages = Lists.newArrayList();
  }

  @Override
  public void cancel(@Nonnull final KeyedMessage message) {
    messages.add(checkNotNull(message, "message"));
  }

  @Nonnull
  public List<KeyedMessage> getCancelMessages() {
    return Collections.unmodifiableList(messages);
  }

  @Override
  public boolean isCanceled() {
    return !messages.isEmpty();
  }
}
