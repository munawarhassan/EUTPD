package com.pmi.tpd.euceg.backend.core.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;
import java.util.EventObject;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.annotation.TransactionAware;
import com.pmi.tpd.euceg.backend.core.message.IBackendMessage;

/**
 * @author christophe friederich
 * @since 2.5
 * @param <T>
 *            the type of message.
 */
@TransactionAware(value = TransactionAware.When.IMMEDIATE)
public class EventBackendReceived<T extends IBackendMessage> extends EventObject {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final long date;

  /** */
  private final T message;

  /**
   * @param source
   * @param message
   */
  public EventBackendReceived(@Nonnull final Object source, @Nonnull final T message) {
    super(checkNotNull(source, "source"));

    date = System.currentTimeMillis();
    this.message = message;
  }

  /**
   * Gets the indicating whether the message is a instance of.
   *
   * @param cl
   *           the type of message.
   * @return Returns {@code true} whether the event contains a message of type
   *         {@code cl}.
   */
  public boolean isMessageInstanceOf(final Class<? extends IBackendMessage> cl) {
    return cl.isInstance(this.message);
  }

  /**
   * Gets the created date of event.
   *
   * @return Returns a new instance of {@link Date} representing when the event
   *         has been created.
   */
  @Nonnull
  public final Date getDate() {
    return new Date(date);
  }

  /**
   * Gets the message.
   *
   * @return Returns the message.
   */
  @Nonnull
  public T getMessage() {
    return message;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getClass().getName() + "[source=" + source + ", message=" + message + "]";
  }
}
