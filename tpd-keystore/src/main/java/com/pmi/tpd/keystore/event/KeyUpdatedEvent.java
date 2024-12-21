package com.pmi.tpd.keystore.event;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.audit.AuditEntryConverter;
import com.pmi.tpd.api.audit.Channels;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.Priority;
import com.pmi.tpd.api.audit.annotation.Audited;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.keystore.event.KeyUpdatedEvent.AuditUpdatedKeyConverter;

/**
 * Raised when a certificate key is created.
 *
 * @since 2.4
 * @author devacfr
 */
@Audited(converter = AuditUpdatedKeyConverter.class, priority = Priority.HIGH, channels = { Channels.ADMIN_LOG,
    Channels.KEYSTORE })
public class KeyUpdatedEvent extends BaseEvent {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /** */
  private final String alias;

  /**
   * Default constructor.
   *
   * @param source
   *               The object on which the Event initially occurred.
   * @param alias
   *               the certificate alias.
   */
  public KeyUpdatedEvent(@Nonnull final Object source, @Nonnull final String alias) {
    super(source);
    this.alias = checkNotNull(alias, "alias");
  }

  @Nonnull
  public String getAlias() {
    return alias;
  }

  public static class AuditUpdatedKeyConverter implements AuditEntryConverter<KeyUpdatedEvent> {

    @Override
    public IAuditEntry convert(final KeyUpdatedEvent event, final AuditEntryBuilder builder) {
      return builder.details(ImmutableMap.of("alias", event.alias)).build();
    }

  }

}
