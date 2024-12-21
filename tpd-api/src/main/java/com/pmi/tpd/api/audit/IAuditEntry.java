package com.pmi.tpd.api.audit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Christophe Friederich
 * @since 2.4
 */
public interface IAuditEntry extends IMinimalAuditEntry {

  @Nullable
  String getSourceIpAddress();

  @Nonnull
  String getTarget();
}
