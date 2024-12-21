package com.pmi.tpd.database.jpa;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Sets;
import com.pmi.tpd.database.hibernate.HibernateEntityListenersAdapter;

/**
 * Automatically configure Spring-aware entity listeners for every @Entity class annotated with the
 * {@link JpaEntityListeners} annotation.
 * 
 * @author Christophe Friederich <devacfr@me.com>
 */
public class JpaEntityListenersConfigurer implements ApplicationContextAware {

    private ApplicationContext context;

    private final HibernateEntityListenersAdapter adapter = new HibernateEntityListenersAdapter();

    private final EntityManagerFactory entityManagerFactory;

    @Inject
    public JpaEntityListenersConfigurer(@Nonnull final EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @PostConstruct
    public void registerListener() {
        final SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        final EventListenerRegistry registry = sessionFactory.getServiceRegistry()
                .getService(EventListenerRegistry.class);

        registry.appendListeners(EventType.PRE_INSERT, adapter);
        registry.appendListeners(EventType.POST_COMMIT_INSERT, adapter);
        registry.appendListeners(EventType.PERSIST, adapter);
        registry.appendListeners(EventType.PERSIST_ONFLUSH, adapter);
        registry.appendListeners(EventType.PRE_UPDATE, adapter);
        registry.appendListeners(EventType.POST_COMMIT_UPDATE, adapter);
        registry.appendListeners(EventType.PRE_DELETE, adapter);
        registry.appendListeners(EventType.POST_COMMIT_DELETE, adapter);
        registry.appendListeners(EventType.POST_LOAD, adapter);
        registry.appendListeners(EventType.MERGE, adapter);
        registry.appendListeners(EventType.SAVE, adapter);
        registry.appendListeners(EventType.SAVE_UPDATE, adapter);
        registry.appendListeners(EventType.DELETE, adapter);
        registerListeners(entityManagerFactory);
    }

    public void registerEventListener(final Object listener) {
        adapter.addListener(listener);
    }

    private void registerListeners(final EntityManagerFactory entityManagerFactory) {
        final Set<Object> listeners = Sets.newHashSet();

        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            // for every entity known to the system...
            for (final EntityType<?> entity : entityManager.getMetamodel().getEntities()) {

                // ... register event listeners for it.
                if (entity.getJavaType() != null
                        && entity.getJavaType().isAnnotationPresent(JpaEntityListeners.class)) {
                    final JpaEntityListeners annotation = entity.getJavaType().getAnnotation(JpaEntityListeners.class);
                    for (final Class<?> beanClass : annotation.value()) {
                        final Map<String, ?> map = context.getBeansOfType(beanClass);
                        listeners.addAll(map.values());
                    }
                }
            }
        } finally {
            if (entityManager != null) {
                entityManager.close();
            }
        }
        adapter.addListeners(listeners);

    }

}
