package com.pmi.tpd.core.jpa;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import com.opensymphony.module.propertyset.PropertySet;
import com.pmi.tpd.core.BaseDaoTestIT;
import com.pmi.tpd.core.context.propertyset.IPropertySetDAO;
import com.pmi.tpd.core.context.propertyset.spi.provider.JpaPropertySetDAOImpl;
import com.pmi.tpd.core.model.propertyset.PropertySetItem;
import com.pmi.tpd.database.IDataSourceConfiguration;
import com.pmi.tpd.database.jpa.ISwappableEntityManagerFactory;

@Configuration
@ContextConfiguration(classes = { DelegatingSwappableEntityManagerFactoryIT.class })
@TestExecutionListeners(
        value = { DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class },
        inheritListeners = false)
public class DelegatingSwappableEntityManagerFactoryIT extends BaseDaoTestIT {

    @Inject
    private ISwappableEntityManagerFactory swappableEntityManagerFactory;

    @Inject
    private BeanFactory beanFactory;

    @Inject
    private IPropertySetDAO propertySetDao;

    @Inject
    private DataSource dataSource;

    @Inject
    IDataSourceConfiguration targetConfiguration;

    @Bean
    public IPropertySetDAO jpaPropertySetDAO(final EntityManager entityManager) {
        return new JpaPropertySetDAOImpl(entityManager);
    }

    @Test
    public void testSwapEntityManagerFactory() {
        final PropertySetItem item = PropertySetItem.builder("property", 1L, "key")
                .value(PropertySet.STRING, "value")
                .build();
        propertySetDao.persist(item);
        assertTrue(propertySetDao.existsById(item.getId()), "property must exist");
        final EntityManagerFactory old = swappableEntityManagerFactory.swap((EntityManagerFactory) beanFactory
                .getBean("prototypeEntityManagerFactory", dataSource, targetConfiguration, true));
        assertNotNull(old, "old entity manager must exist");
        assertTrue(propertySetDao.existsById(item.getId()), "property must exist");
    }
}
