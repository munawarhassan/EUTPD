package com.pmi.tpd.core.audit;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Maps;
import com.pmi.tpd.api.audit.IAuditEntry;
import com.pmi.tpd.api.audit.IAuditEvent;
import com.pmi.tpd.api.event.annotation.EventListener;
import com.pmi.tpd.api.user.IUser;
import com.pmi.tpd.core.audit.spi.IAuditEventRepository;
import com.pmi.tpd.core.event.audit.AuditEvent;
import com.pmi.tpd.core.event.auth.AuthenticationFailureEvent;
import com.pmi.tpd.core.event.auth.AuthenticationSuccessEvent;
import com.pmi.tpd.core.model.audit.AuditEventEntity;
import com.pmi.tpd.core.model.audit.QAuditEventEntity;
import com.pmi.tpd.spring.transaction.SpringTransactionUtils;

/**
 * Service for managing audit events.
 * <p/>
 * <p>
 * This is the default implementation to support SpringBoot Actuator AuditEventRepository
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Singleton
@Named
@Transactional(readOnly = true)
public class DefaultAuditEventService implements IAuditEventService {

    /** */
    private final IAuditEventRepository persistenceAuditEventRepository;

    /** */
    private final TransactionTemplate requiresTransactionTemplate;

    /**
     * <p>
     * Constructor for AuditEventService.
     * </p>
     *
     * @param persistenceAuditEventRepository
     *                                        a {@link com.pmi.tpd.core.audit.spi.IAuditEventRepository} object.
     */
    public DefaultAuditEventService(@Nonnull final IAuditEventRepository persistenceAuditEventRepository,
            @Nonnull final PlatformTransactionManager transactionManager) {
        this.persistenceAuditEventRepository = checkNotNull(persistenceAuditEventRepository,
            "persistenceAuditEventRepository");
        requiresTransactionTemplate = new TransactionTemplate(checkNotNull(transactionManager, "transactionManager"),
                SpringTransactionUtils.REQUIRES_NEW);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Page<? extends IAuditEvent> findAll(@Nonnull final Pageable pageable) {
        return persistenceAuditEventRepository.findAll(
            QAuditEventEntity.auditEventEntity.action.notIn(AuthenticationFailureEvent.class.getName(),
                AuthenticationSuccessEvent.class.getName()),
            pageable);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Iterable<? extends IAuditEvent> find(@Nullable final String principal, @Nullable final LocalDate after) {
        List<? extends IAuditEvent> persistentAuditEvents;
        if (principal == null && after == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findAll();
        } else if (after == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipal(principal);
        } else {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfter(principal,
                after);
        }
        return persistentAuditEvents;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Page<? extends IAuditEvent> findByDates(@Nonnull final Pageable pageable,
        @Nullable final LocalDate fromDate,
        @Nullable final LocalDate toDate,
        @Nullable final String... channels) {
        return persistenceAuditEventRepository.findAllByAuditEventDateBetween(pageable, fromDate, toDate, channels);
    }

    /**
     * <p>
     * onEvent.
     * </p>
     *
     * @param event
     *              a {@link AuditEvent} object.
     */
    @EventListener
    public void onEvent(final AuditEvent event) {
        requiresTransactionTemplate.execute(status -> {
            final IAuditEntry entry = event.getEntry();
            IUser user = entry.getUser();
            if (user == null) {
                user = event.getUser();
            }
            final Map<String, String> details = entry.getDetails() != null ? Maps.newHashMap(entry.getDetails())
                    : Collections.emptyMap();

            details.entrySet().stream().forEach(e -> {
                String value = e.getValue();
                if (value != null && value.length() >= 512) {
                    value = value.substring(0, 511);
                    e.setValue(value);
                }

            });
            this.persistenceAuditEventRepository.save(AuditEventEntity.builder()
                    .created(event.getDate())
                    .data(details)
                    .principal(user != null ? user.getName() : "INTERNAL")
                    .action(entry.getAction())
                    .channels(event.getChannels())
                    .build());
            return null;
        });
    }

}
