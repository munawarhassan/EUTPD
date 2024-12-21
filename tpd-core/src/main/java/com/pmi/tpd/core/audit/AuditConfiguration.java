package com.pmi.tpd.core.audit;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmi.tpd.api.event.publisher.IEventPublisher;
import com.pmi.tpd.core.audit.spi.IAuditEventRepository;
import com.pmi.tpd.core.audit.spi.JpaAuditEventRepository;
import com.pmi.tpd.core.user.IUserService;
import com.pmi.tpd.security.IAuthenticationContext;
import com.pmi.tpd.spring.context.RelaxedPropertyResolver;
import com.pmi.tpd.web.core.request.IRequestManager;

/**
 * @author Christophe Friederich
 * @since 2.4
 */
@Configuration
public class AuditConfiguration implements EnvironmentAware {

  /** */
  private RelaxedPropertyResolver props;

  /** {@inheritDoc} */
  @Override
  public void setEnvironment(final org.springframework.core.env.Environment environment) {
    this.props = new RelaxedPropertyResolver(environment, "audit.");
  }

  /**
   * @param entityManager
   *                      JPA entity manager.
   * @return Returns instance {@link IAuditEventRepository}.
   */
  @Bean
  public IAuditEventRepository auditEventRepository(final EntityManager entityManager) {
    return new JpaAuditEventRepository(entityManager);
  }

  /**
   * <p>
   * auditEventService.
   * </p>
   *
   * @param persistenceAuditEventRepository
   *                                        a
   *                                        {@link com.pmi.tpd.core.audit.spi.IAuditEventRepository}
   *                                        object.
   * @return a {@link com.pmi.tpd.core.audit.IAuditEventService} object.
   */
  @Bean
  public IAuditEventService auditEventService(@Nonnull final IAuditEventRepository persistenceAuditEventRepository,
      @Nonnull final PlatformTransactionManager transactionManager) {
    return new DefaultAuditEventService(persistenceAuditEventRepository, transactionManager);
  }

  @Bean
  public IAuditEntryLoggingService auditEntryLoggingService(final ObjectMapper mapper) {
    return new DefaultAuditEntryLoggingService(mapper,
        props.getProperty("details.max.length", Integer.class, 1024));
  }

  @Bean
  public AuditEventListener auditEventListener(final IAuditEntryLoggingService auditLoggingService) {
    return new AuditEventListener(auditLoggingService,
        props.getProperty("highest.priority.to.log", String.class, "HIGH"));
  }

  @Bean
  public AuditedAnnotatedEventListener auditedAnnotatedEventListener(final IEventPublisher eventPublisher,
      final IRequestManager requestManager,
      final IAuthenticationContext authContext,
      final IAuditEntryLoggingService auditLoggingService) {
    return new AuditedAnnotatedEventListener(eventPublisher, requestManager, authContext, auditLoggingService);
  }

  @Bean
  public AuthenticationEventListener authenticationEventListener(final IAuditEntryLoggingService auditLoggingService,
      final IRequestManager requestManager,
      final IAuthenticationContext authContext,
      final IEventPublisher eventPublisher,
      final IUserService userService) {
    return new AuthenticationEventListener(auditLoggingService, requestManager, authContext, eventPublisher,
        userService);
  }

  @Bean
  public PermissionEventListener permissionEventListener(final IAuditEntryLoggingService auditLoggingService,
      final IRequestManager requestManager,
      final IAuthenticationContext authContext,
      final IEventPublisher eventPublisher) {
    return new PermissionEventListener(auditLoggingService, requestManager, authContext, eventPublisher);
  }

  @Bean
  public ServerEventListener serverEventListener(final IAuditEntryLoggingService auditLoggingService,
      final IRequestManager requestManager,
      final IAuthenticationContext authContext,
      final IEventPublisher eventPublisher) {
    return new ServerEventListener(auditLoggingService, requestManager, authContext, eventPublisher);
  }
}
