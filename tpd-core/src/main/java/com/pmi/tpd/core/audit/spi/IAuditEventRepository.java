package com.pmi.tpd.core.audit.spi;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.audit.AuditEventEntity;
import com.pmi.tpd.database.jpa.IDslAccessor;

/**
 * Spring Data JPA repository for the AuditEventEntity entity.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IAuditEventRepository extends IDslAccessor<AuditEventEntity, String> {

  /**
   * <p>
   * findByPrincipal.
   * </p>
   *
   * @param principal
   *                  a {@link java.lang.String} object.
   * @return a {@link java.util.List} object.
   */
  @Nonnull
  List<AuditEventEntity> findByPrincipal(@Nonnull String principal);

  /**
   * <p>
   * findByPrincipalAndAuditEventDateAfter.
   * </p>
   *
   * @param principal
   *                  a {@link java.lang.String} object.
   * @param after
   *                  a {@link java.util.Date} object.
   * @return a {@link java.util.List} object.
   */
  @Nonnull
  List<AuditEventEntity> findByPrincipalAndAuditEventDateAfter(@Nullable String principal, @Nullable LocalDate after);

  /**
   * Finds all audit events by range date.
   *
   * @param fromDate
   *                 a {@link java.util.Date} object.
   * @param toDate
   *                 a {@link java.util.Date} object.
   * @param channels
   *                 channels to use.
   * @return Returns the requested page of {@link AuditEventEntity} filtered from
   *         {@code fromDate} to {@code toDate},
   *         which may be empty but never {@code null}.
   */
  @Nonnull
  Page<AuditEventEntity> findAllByAuditEventDateBetween(@Nonnull Pageable pageable,
      @Nullable LocalDate fromDate,
      @Nullable LocalDate toDate,
      @Nullable final String... channels);

}
