package com.pmi.tpd.core.audit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.api.audit.IAuditEvent;

/**
 * <p>
 * IAuditEventService interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IAuditEventService {

    /**
     * <p>
     * find.
     * </p>
     *
     * @param principal
     *            a {@link java.lang.String} object.
     * @param after
     *            a {@link java.util.Date} object.
     * @return a {@link java.lang.Iterable} object.
     */
    @Nonnull
    Iterable<? extends IAuditEvent> find(@Nullable final String principal, @Nullable final LocalDate after);

    /**
     * <p>
     * findAll.
     * </p>
     *
     * @param pageable
     *            defines the page of audit to retrieve.
     * @return Returns the requested page of {@link DefaultAuditEvent}, potentially filtered, which may be empty but
     *         never {@code null}.
     */
    @Nonnull
    Page<? extends IAuditEvent> findAll(@Nonnull Pageable pageable);

    /**
     * Finds all audit events by range date.
     *
     * @param pageable
     *            defines the page of audit to retrieve.
     * @param fromDate
     *            a {@link java.util.Date} object.
     * @param toDate
     *            a {@link java.util.Date} object.
     * @param channels
     *            channels to use.
     * @return Returns the requested page of {@link DefaultAuditEvent} filtered from {@code fromDate} to {@code toDate},
     *         which may be empty but never {@code null}.
     */
    @Nonnull
    Page<? extends IAuditEvent> findByDates(@Nonnull final Pageable pageable,
        @Nonnull LocalDate fromDate,
        @Nonnull LocalDate toDate,
        @Nullable String... channels);

}
