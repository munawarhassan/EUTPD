package com.pmi.tpd.core.aop;

import javax.inject.Inject;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.i18n.KeyedMessage;
import com.pmi.tpd.core.exception.DataStoreException;
import com.pmi.tpd.core.exception.EntityOutOfDateException;
import com.pmi.tpd.security.AuthorisationException;

@Aspect
@Order(value = 0)
public class ExceptionRewriteAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionRewriteAdvice.class);

    private final I18nService i18nService;

    @Inject
    public ExceptionRewriteAdvice(final I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
    public void accessDenied(final AccessDeniedException ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("pointcut afterThrowing on AccessDeniedException:", ex);
        }
        // This message needs to be kept in sync with NoSuchEntityExceptionMapper
        final KeyedMessage message = i18nService.createKeyedMessage("app.service.accessdenied");
        throw new AuthorisationException(message, ex);
    }

    @AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
    public void hibernateFailed(final HibernateException ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("pointcut afterThrowing on HibernateException:", ex);
        }
        final KeyedMessage message = i18nService.createKeyedMessage("app.service.datastorefail");
        throw new DataStoreException(message, ex);
    }

    @AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
    public void dataAccessFailed(final DataAccessException ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("pointcut afterThrowing on DataAccessException:", ex);
        }
        final KeyedMessage message = i18nService.createKeyedMessage("app.service.datastorefail");
        throw new DataStoreException(message, ex);
    }

    @AfterThrowing(pointcut = "@within(org.springframework.stereotype.Service)", throwing = "ex")
    public void entityOutOfDate(final ObjectOptimisticLockingFailureException ex) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("pointcut afterThrowing on ObjectOptimisticLockingFailureException:", ex);
        }
        throw new EntityOutOfDateException(i18nService.createKeyedMessage("app.service.entity.outofdate"), ex);
    }

}
