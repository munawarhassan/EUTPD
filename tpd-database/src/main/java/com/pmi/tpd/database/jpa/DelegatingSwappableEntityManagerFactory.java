package com.pmi.tpd.database.jpa;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

import org.springframework.context.ApplicationContext;
import org.springframework.core.InfrastructureProxy;

/**
 * @author Christophe Friederich
 * @since 1.3
 */
@SuppressWarnings("rawtypes")
public class DelegatingSwappableEntityManagerFactory implements ISwappableEntityManagerFactory {

    /** */
    private volatile EntityManagerFactory delegate;

    /**
     * @param delegate
     */
    public DelegatingSwappableEntityManagerFactory(@Nonnull final EntityManagerFactory delegate,
            @Nonnull final ApplicationContext context) {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    @Override
    public EntityManager createEntityManager() {
        return delegate.createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(final Map map) {
        return delegate.createEntityManager(map);
    }

    @Override
    public EntityManager createEntityManager(final SynchronizationType synchronizationType) {
        return delegate.createEntityManager(synchronizationType);
    }

    @Override
    public EntityManager createEntityManager(final SynchronizationType synchronizationType, final Map map) {
        return delegate.createEntityManager(synchronizationType, map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    @Override
    public void addNamedQuery(final String name, final Query query) {
        delegate.addNamedQuery(name, query);
    }

    @Override
    public <T> T unwrap(final Class<T> cls) {
        return delegate.unwrap(cls);
    }

    @Override
    public <T> void addNamedEntityGraph(final String graphName, final EntityGraph<T> entityGraph) {
        delegate.addNamedEntityGraph(graphName, entityGraph);
    }

    @Override
    public EntityManagerFactory getWrappedObject() {
        return wrappedObject(this.delegate);
    }

    @Override
    public @Nonnull EntityManagerFactory swap(final @Nonnull EntityManagerFactory target) {
        final EntityManagerFactory old = delegate;
        delegate = target;
        return old;
    }

    private static EntityManagerFactory wrappedObject(final EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory instanceof InfrastructureProxy
                ? (EntityManagerFactory) ((InfrastructureProxy) entityManagerFactory).getWrappedObject()
                : entityManagerFactory;
    }

}
