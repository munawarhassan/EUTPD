package com.pmi.tpd.api.audit;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.user.IUser;

/**
 * @author Christophe Friederich
 * @since 2.4
 */
public interface IMinimalAuditEntry {

  /**
   * @return the type of event
   */
  @Nonnull
  String getAction();

  /**
   * @return any extra details not covered by the other fields
   */
  @Nullable
  Map<String, String> getDetails();

  /**
   * @return the date and time the action occurred
   */
  @Nonnull
  Date getTimestamp();

  /**
   * @return the user who performed the action
   */
  @Nullable
  IUser getUser();
}
