package com.pmi.tpd.api.audit;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.api.util.BuilderSupport;

/**
 * A builder for constructing {@link IAuditEntry audit entries}.
 *
 * @author Christophe Friederich
 * @since 2.4
 */
public class AuditEntryBuilder extends BuilderSupport {

  /** */
  private String action;

  /** */
  private Map<String, String> details;

  /** */
  private String sourceIpAddress;

  /** */
  private String target;

  /** */
  private long timestamp;

  /** */
  private IUser user;

  @Nonnull
  public AuditEntryBuilder action(@Nonnull final Class<?> value) {
    action = requireNonNull(value, "action").getSimpleName();

    return this;
  }

  @Nonnull
  public AuditEntryBuilder action(@Nonnull final String value) {
    action = checkNotBlank(value, "action");

    return this;
  }

  @Nonnull
  public IAuditEntry build() {
    return new SimpleAuditEntry(this);
  }

  @Nonnull
  public AuditEntryBuilder details(@Nullable final Map<String, String> value) {
    details = value;

    return this;
  }

  @Nonnull
  public AuditEntryBuilder sourceIpAddress(@Nullable final String value) {
    sourceIpAddress = value;

    return this;
  }

  @Nonnull
  public AuditEntryBuilder target(@Nonnull final String value) {
    target = requireNonNull(value, "value");

    return this;
  }

  @Nonnull
  public AuditEntryBuilder timestamp(@Nonnull final Date value) {
    timestamp = requireNonNull(value, "timestamp").getTime();

    return this;
  }

  @Nonnull
  public AuditEntryBuilder timestamp(final long value) {
    timestamp = value;

    return this;
  }

  @Nonnull
  public AuditEntryBuilder user(@Nullable final IUser value) {
    user = value;

    return this;
  }

  /**
   * A simple, immutable implementation of {@link AuditEntry}.
   */
  private static class SimpleAuditEntry implements IAuditEntry {

    private final IUser user;

    private final Map<String, String> details;

    private final long timestamp;

    private final String action;

    private final String sourceIpAddress;

    private final String target;

    private SimpleAuditEntry(final AuditEntryBuilder builder) {
      action = builder.action;
      details = builder.details;
      sourceIpAddress = builder.sourceIpAddress;
      target = builder.target;
      timestamp = builder.timestamp;
      user = builder.user;
    }

    /**
     * @return the type of event occuring
     */
    @Nonnull
    @Override
    public String getAction() {
      return action;
    }

    /**
     * @return relevant details of the audited event
     */
    @Override
    public Map<String, String> getDetails() {
      return details;
    }

    /**
     * @return time the audited event was received
     */
    @Nonnull
    @Override
    public Date getTimestamp() {
      return new Date(timestamp);
    }

    /**
     * @return the source IP address of the user causing the audit event
     */
    @Override
    public String getSourceIpAddress() {
      return sourceIpAddress;
    }

    /**
     * @return details of the affected component (e.g. a plugin key or project slug)
     */
    @Nonnull
    @Override
    public String getTarget() {
      return target;
    }

    /**
     * @return the user causing the audited event
     */
    @Override
    public IUser getUser() {
      return user;
    }
  }
}
