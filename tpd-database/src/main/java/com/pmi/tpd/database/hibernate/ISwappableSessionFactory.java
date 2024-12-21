package com.pmi.tpd.database.hibernate;

import javax.annotation.Nonnull;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.core.InfrastructureProxy;

/**
 * An extension to Hibernate's standard {@code SessionFactoryImplementor} which
 * allows swapping out the instance to use
 * when servicing requests.
 *
 * @author Christophe Friederich
 * @since 1.3
 */
public interface ISwappableSessionFactory extends SessionFactoryImplementor, InfrastructureProxy {

  /**
   * Overrides the return type of {@code InfrastructureProxy.getWrappedObject()}
   * to be
   * {@code SessionFactoryImplementor} to simplify callers.
   *
   * @return the wrapped {@code SessionFactoryImplementor}
   */
  @Override
  SessionFactoryImplementor getWrappedObject();

  /**
   * Swaps in the provided target {@code SessionFactoryImplementor} for processing
   * method invocations, returning the
   * old target.
   *
   * @param target
   *               the new session factory to use
   * @return the previous session factory
   */
  @Nonnull
  SessionFactoryImplementor swap(@Nonnull SessionFactoryImplementor target);
}
