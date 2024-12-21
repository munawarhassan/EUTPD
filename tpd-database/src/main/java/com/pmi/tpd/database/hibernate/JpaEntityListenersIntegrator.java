package com.pmi.tpd.database.hibernate;

import java.util.Map;
import java.util.Set;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Sets;
import com.pmi.tpd.database.jpa.JpaEntityListeners;

/**
 * Automatically configure Spring-aware entity listeners for every @Entity class annotated with the
 * {@link JpaEntityListeners} annotation.
 * 
 * @author Christophe Friederich <devacfr@me.com>
 */
public class JpaEntityListenersIntegrator implements Integrator {

    private final ApplicationContext context;

    private final HibernateEntityListenersAdapter adapter = new HibernateEntityListenersAdapter();

    public JpaEntityListenersIntegrator(final ApplicationContext context) {
        super();
        this.context = context;
    }

    @Override
    public void integrate(final Metadata metadata,
        final SessionFactoryImplementor sessionFactory,
        final SessionFactoryServiceRegistry serviceRegistry) {

        register(metadata, sessionFactory);
    }

    @Override
    public void disintegrate(final SessionFactoryImplementor sessionFactory,
        final SessionFactoryServiceRegistry serviceRegistry) {

    }

    public void registerEventListener(final Object listener) {
        adapter.addListener(listener);
    }

    private void register(final Metadata metadata, final SessionFactoryImplementor sessionFactory) {
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
        registerListeners(metadata);
    }

    private void registerListeners(final Metadata metadata) {
        final Set<Object> listeners = Sets.newHashSet();

        // for every entity known to the system...
        for (final PersistentClass entity : metadata.getEntityBindings()) {
            final Class<?> entityType = entity.getMappedClass();
            // ... register event listeners for it.
            if (entityType != null && entityType.isAnnotationPresent(JpaEntityListeners.class)) {
                final JpaEntityListeners annotation = entityType.getAnnotation(JpaEntityListeners.class);
                for (final Class<?> beanClass : annotation.value()) {
                    final Map<String, ?> map = context.getBeansOfType(beanClass);
                    listeners.addAll(map.values());
                }
            }
        }
        adapter.addListeners(listeners);

    }

}
