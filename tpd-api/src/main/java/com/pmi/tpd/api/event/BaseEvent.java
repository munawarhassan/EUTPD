package com.pmi.tpd.api.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Date;
import java.util.EventObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.user.IUser;

/**
 * Base event class for all application events.
 */
public abstract class BaseEvent extends EventObject {

  /**
   *
   */
  private static final long serialVersionUID = -2609710132629503375L;

  /** */
  private final long date;

  /**
   * uninitialised properties will be set by the IEventPublisher when the event is
   * published.
   */
  private final IUser user;

  /**
   * @param source
   *               The object on which the Event initially occurred.
   */
  protected BaseEvent(@Nonnull final Object source) {
    super(checkNotNull(source, "source"));

    date = System.currentTimeMillis();
    user = null; // will be set by the EventPublisher
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
   * Gets the user initiator of this event.
   * <p>
   * uninitialised will be set by the
   * {@link com.pmi.tpd.api.event.publisher.IEventPublisher} when the event is
   * published.
   * </p>
   *
   * @return Returns a {@link IUser} representing the initiator of this event.
   */
  @Nullable
  public final IUser getUser() {
    return user;
  }
}
