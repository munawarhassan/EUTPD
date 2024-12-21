package com.pmi.tpd.web.core.rs.renderer;

import java.lang.reflect.Constructor;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class DefaultSurrogateBuilder<S, R> implements ISurrogateBuilder<S, R> {

  /** */
  private final Constructor<S> ctor;

  /** */
  private final Object[] extraCtorArgs;

  public DefaultSurrogateBuilder(final Constructor<S> ctor, final Object... extraCtorArgs) {
    this.ctor = ctor;
    this.extraCtorArgs = extraCtorArgs;
    if (ctor.getParameterTypes().length != extraCtorArgs.length + 1) {
      throw new IllegalArgumentException(String.format(
          "Incorrect number of arguments for the supplied constructor: %d expected but constructor supports %d",
          ctor.getParameterTypes().length,
          extraCtorArgs.length + 1));
    }
    final Class<?>[] paramTypes = ctor.getParameterTypes();
    for (int i = 0; i < extraCtorArgs.length; i++) {
      if (!paramTypes[i + 1].isAssignableFrom(extraCtorArgs[i].getClass())) {
        throw new IllegalArgumentException(
            String.format("Additional argument %d is not compatible with construct argument", i));
      }
    }
  }

  @Override
  public S buildFor(final R object) {
    final Object[] args = new Object[extraCtorArgs.length + 1];
    args[0] = object;
    if (extraCtorArgs.length > 0) {
      System.arraycopy(extraCtorArgs, 0, args, 1, extraCtorArgs.length);
    }
    try {
      return ctor.newInstance(args);
    } catch (final Exception e) {
      throw new RuntimeException("Failed to instantiate surrogate instance", e);
    }
  }
}