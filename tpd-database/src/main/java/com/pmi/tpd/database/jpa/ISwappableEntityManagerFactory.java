package com.pmi.tpd.database.jpa;

import javax.annotation.Nonnull;
import javax.persistence.EntityManagerFactory;

import org.springframework.core.InfrastructureProxy;

public interface ISwappableEntityManagerFactory extends InfrastructureProxy, EntityManagerFactory {

  /**
   * Overrides the return type of {@code InfrastructureProxy.getWrappedObject()}
   * to be
   * {@code SessionFactoryImplementor} to simplify callers.
   *
   * @return the wrapped {@code EntityManagerFactory}
   */
  @Override
  EntityManagerFactory getWrappedObject();

  /**
   * Swaps in the provided target {@code EntityManagerFactory} for processing
   * method invocations, returning the old
   * target.
   *
   * @param target
   *               the new session factory to use
   * @return the previous session factory
   */
  @Nonnull
  EntityManagerFactory swap(@Nonnull EntityManagerFactory target);
}
