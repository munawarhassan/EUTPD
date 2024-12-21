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
import com.pmi.tpd.keystore.event.KeyDeletedEvent.AuditDeletedKeyConverter;

/**
 * Raised when a certificate key is deleted.
 *
 * @since 2.4
 * @author devacfr
 */
@Audited(converter = AuditDeletedKeyConverter.class, priority = Priority.HIGH, channels = { Channels.ADMIN_LOG,
    Channels.KEYSTORE })
public class KeyDeletedEvent extends BaseEvent {

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
  public KeyDeletedEvent(@Nonnull final Object source, @Nonnull final String alias) {
    super(source);
    this.alias = checkNotNull(alias, "alias");
  }

  @Nonnull
  public String getAlias() {
    return alias;
  }

  public static class AuditDeletedKeyConverter implements AuditEntryConverter<KeyDeletedEvent> {

    @Override
    public IAuditEntry convert(final KeyDeletedEvent event, final AuditEntryBuilder builder) {
      return builder.details(ImmutableMap.of("alias", event.alias)).build();
    }

  }

}
