package com.pmi.tpd.core.avatar;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.event.annotation.AsynchronousPreferred;
import com.pmi.tpd.api.event.annotation.EventName;
import com.pmi.tpd.api.user.IUser;

@AsynchronousPreferred
@EventName("app.user.avatar.updated")
public class UserAvatarUpdatedEvent extends BaseEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final IUser updatedUser;

  public UserAvatarUpdatedEvent(@Nonnull final Object source, @Nonnull final IUser updatedUser) {
    super(source);

    this.updatedUser = checkNotNull(updatedUser, "updatedUser");
  }

  @Nonnull
  public IUser getUpdatedUser() {
    return updatedUser;
  }
}
