package com.pmi.tpd.api.audit;

import javax.annotation.Nonnull;

import com.pmi.tpd.api.audit.annotation.Audited;

/**
 * Converts an {@link Audited} event into a standard audit format.
 * <p>
 * Implementations <i>must</i> have a nullary (no argument) constructor and are
 * intended to be <i>lightweight, stateless
 * classes</i>. Converters are instantiated for <i>each event</i> and discarded
 * after the event has been converted.
 *
 * @param <T>
 *            the type of the original event
 * @author Christophe Friederich
 * @since 2.4
 */
public interface AuditEntryConverter<T> {

  /**
   * Converts an {@link Audited} event into a standard audit format.
   *
   * @param builder
   *                an audit entry builder which has the source IP address set by
   *                the caller
   * @param event
   *                the original annotated event
   * @return the details of the original event in the standard audit format
   */
  @Nonnull
  IAuditEntry convert(@Nonnull T event, @Nonnull AuditEntryBuilder builder);
}
