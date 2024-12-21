package com.pmi.tpd.core;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.dataset.DataSetLoader;
import com.github.springtestdbunit.dataset.FlatXmlDataSetLoader;
import com.github.springtestdbunit.dataset.ReplacementDataSetLoader;
import com.google.common.collect.Maps;
import com.pmi.tpd.api.ApplicationConstants;
import com.pmi.tpd.testing.AbstractJunitTest;

@ExtendWith(SpringExtension.class)
@TestExecutionListeners(
        value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
                TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class },
        inheritListeners = false)
@ContextConfiguration(classes = { JpaConfig.class })
@ActiveProfiles({ "integration-test" })
@TestPropertySource("classpath:jpa-config.properties")
@Rollback
@Transactional
@DirtiesContext
@DbUnitConfiguration(dataSetLoaderBean = "dataSetLoader")
public abstract class BaseDaoTestIT extends AbstractJunitTest {

    @Named(ApplicationConstants.Jpa.ENTITY_MANAGER_FACTORY_NAME)
    @Inject
    private EntityManagerFactory entityManagerFactory;

    /**
     * Clears the {@code Session} and evicts <i>all</i> entities from both the first- and second-level caches.
     * <p>
     * <b>Warning</b>: Any statements pending {@link #flush() flush} in the {@code Session} <i>will never be applied</i>
     * after calling this method. If there may be pending statements in the session, they must be explicitly flushed
     * before calling {@code clear()}.
     */
    protected void clear() {
        final SessionFactory sessionFactory = getSessionFactory();
        // Clear the session, which clears both any operations pending flush and evicts all L1 cached entities
        entityManager().clear();
        // Evict all entities from the L2 cache (Ehcache) as well
        sessionFactory.getCache().evictEntityData();
        // Also evict any collections that have been cached
        sessionFactory.getCache().evictCollectionData();
    }

    /**
     * Flushes the {@code Session}, ensuring all statements have been applied to the database.
     * <p>
     * when operations are performed on the {@code Session}, it may store them internally for some period of time rather
     * than immediately executing SQL statements against the database. Typically the session is flushed at the end of
     * the transaction, but the transaction wrapping DAO tests is never committed. As a result, some tests may need to
     * explicitly flush the session if they rely on server-side behaviours, such as foreign keys with cascading
     * deletions, or to verify that Unicode characters are stored correctly in the database.
     */
    protected void flush() {
        entityManager().flush();
    }

    /**
     * Retrieves the current Hibernate {@code Session} for the test.
     *
     * @return the current session
     */
    protected Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    /**
     * @return Return Hibernate {@link SessionFactory} for the test.
     */
    protected SessionFactory getSessionFactory() {
        return entityManagerFactory.unwrap(SessionFactory.class);
    }

    protected EntityManager entityManager() {
        return EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
    }

    /**
     * <p>
     * passwordEncoder.
     * </p>
     *
     * @return a {@link org.springframework.security.crypto.password.PasswordEncoder} object.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean(name = "dataSetLoader")
    public DataSetLoader dataSetLoader() {
        final Map<String, Object> map = Maps.newHashMap();
        map.put("[null]", null);
        map.put("[current_timestamp]", new Date());
        return new ReplacementDataSetLoader(new FlatXmlDataSetLoader(), map);
    }
}
