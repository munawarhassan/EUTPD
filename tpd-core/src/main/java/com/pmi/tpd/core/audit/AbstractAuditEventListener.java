package com.pmi.tpd.core.audit;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.audit.AuditEntryBuilder;
import com.pmi.tpd.api.event.BaseEvent;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * @author Christophe Friederich
 * @since 2.4
 */
public abstract class AbstractAuditEventListener extends AbstractAuditEventPublisher {

  public AbstractAuditEventListener(@Nonnull final IAuditEntryLoggingService auditLoggingService,
      @Nonnull final IRequestManager requestManager, @Nonnull final IAuthenticationContext authContext,
      @Nonnull final IEventPublisher eventPublisher) {
    super(auditLoggingService, requestManager, authContext, eventPublisher);
  }

  static ImmutableMap.Builder<String, String> getMapBuilder() {
    return ImmutableMap.builder();
  }

  static <K, V> void safePut(final ImmutableMap.Builder<K, V> builder, final K key, final V value) {
    if (key != null && value != null) {
      builder.put(key, value);
    }
  }

  /**
   * Used by all the Application event methods to get an audit event builder with
   * common fields set: details, action
   * (based on class name), timestamp (from the event) and user (from the event)
   *
   * @param event
   *              the event to add details for
   * @return a new builder, with username, action and timestamp set
   */
  AuditEntryBuilder createAuditEntryBuilder(final BaseEvent event) {
    return setCommonFields(event, new AuditEntryBuilder()).user(event.getUser());
  }
}
