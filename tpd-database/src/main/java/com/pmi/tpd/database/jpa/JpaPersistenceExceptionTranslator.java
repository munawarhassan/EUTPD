package com.pmi.tpd.database.jpa;

import javax.persistence.PersistenceException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaVendorAdapter;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
public class JpaPersistenceExceptionTranslator implements PersistenceExceptionTranslator {

    /** */
    private final JpaDialect jpaDialect;

    /**
     * @param jpaVendorAdapter
     */
    public JpaPersistenceExceptionTranslator(final JpaVendorAdapter jpaVendorAdapter) {
        this.jpaDialect = jpaVendorAdapter.getJpaDialect();
    }

    /**
     * Implementation of the PersistenceExceptionTranslator interface, as autodetected by Spring's
     * PersistenceExceptionTranslationPostProcessor.
     * <p>
     * Uses the dialect's conversion if possible; otherwise falls back to standard JPA exception conversion.
     *
     * @see org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
     * @see JpaDialect#translateExceptionIfPossible
     * @see EntityManagerFactoryUtils#convertJpaAccessExceptionIfPossible
     */
    @Override
    public DataAccessException translateExceptionIfPossible(final RuntimeException ex) {
        // not convert JPA Exception
        if (ex instanceof PersistenceException) {
            return null;
        }
        return this.jpaDialect != null ? this.jpaDialect.translateExceptionIfPossible(ex)
                : EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible(ex);
    }
}
