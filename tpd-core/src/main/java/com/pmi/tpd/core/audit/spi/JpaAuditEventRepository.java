package com.pmi.tpd.core.audit.spi;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.audit.AuditEventEntity;
import com.pmi.tpd.core.model.audit.QAuditEventEntity;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class JpaAuditEventRepository extends DefaultJpaRepository<AuditEventEntity, String>
    implements IAuditEventRepository {

  /**
   * @param entityManager
   */
  public JpaAuditEventRepository(final EntityManager entityManager) {
    super(AuditEventEntity.class, entityManager);
  }

  @Override
  public QAuditEventEntity entity() {
    return QAuditEventEntity.auditEventEntity;
  }

  @Override
  public List<AuditEventEntity> findByPrincipal(@Nonnull final String principal) {
    return from().where(entity().principal.eq(checkNotNull(principal, "principal"))).fetch();
  }

  @Override
  public List<AuditEventEntity> findByPrincipalAndAuditEventDateAfter(@Nullable final String principal,
      @Nullable final LocalDate after) {
    return from()
        .where(entity().principal.eq(principal)
            .and(entity().timestamp.after(after != null ? after.toDate() : null)))
        .fetch();
  }

  @Override
  public Page<AuditEventEntity> findAllByAuditEventDateBetween(@Nonnull final Pageable pageable,
      @Nullable final LocalDate fromDate,
      @Nullable final LocalDate toDate,
      @Nullable final String... channels) {
    BooleanExpression predicate = null;
    if (fromDate != null && toDate != null) {
      predicate = entity().timestamp.between(fromDate.toDate(), toDate.plusDays(1).toDate());

    }
    if (channels != null && channels.length > 0) {
      if (predicate == null) {
        predicate = entity().channels.any().in(channels);
      } else {
        predicate = predicate.and(entity().channels.any().in(channels));
      }
    }
    return this.findAll(predicate, checkNotNull(pageable, "pageable"));
  }

}
