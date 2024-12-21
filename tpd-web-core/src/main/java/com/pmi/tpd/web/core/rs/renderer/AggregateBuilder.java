package com.pmi.tpd.web.core.rs.renderer;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.pmi.tpd.web.core.rs.annotation.JsonSurrogate;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class AggregateBuilder implements ISurrogateBuilder<Object, Object> {

  /** */
  private static final Logger LOGGER = LoggerFactory.getLogger(AggregateBuilder.class);

  /** */
  @SuppressWarnings("rawtypes")
  protected final Map<Class<?>, ISurrogateBuilder> surrogates;

  /**
   *
   */
  public AggregateBuilder() {

    surrogates = Maps.newHashMap();

    addSurrogateBuilder(Collection.class, new CollectionSurrogateBuilder<>());
    addSurrogateBuilder(Map.class, new MapSurrogateBuilder<>());
  }

  public void addSurrogateBuilder(final Class<?> targetClass, final ISurrogateBuilder<?, ?> builder) {
    surrogates.put(targetClass, builder);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Object buildFor(final Object object) {
    if (object == null) {
      return object;
    }

    final Class<?> objectClass = object.getClass();
    // Package can be null if we don't have access to the package via OSGI
    // This can happen due to the non-deterministic ordering of JsonRendererModule
    // plugins
    if (objectClass.getPackage() == null) {
      return null;
    }
    if ("java.lang".equals(objectClass.getPackage().getName())) {
      // java.lang types do not require a JSON surrogate. Checking for one is just a
      // waste of time.
      return object;
    }

    final List<Map.Entry<Class<?>, ISurrogateBuilder>> potentialSurrogates = Lists
        .newArrayList(Sets.filter(surrogates.entrySet(), entry -> entry.getKey()
            .isInstance(object)));

    if (!potentialSurrogates.isEmpty()) {
      if (potentialSurrogates.size() > 1) {
        // Order so more specialized surrogates come first
        Collections.sort(potentialSurrogates,
            (lhs, rhs) -> rhs.getKey().isAssignableFrom(lhs.getKey()) ? -1
                : lhs.getKey().isAssignableFrom(rhs.getKey()) ? 1 : 0);
      }

      try {
        return potentialSurrogates.get(0).getValue().buildFor(object);
      } catch (final Exception e) {
        LOGGER.error(
            String.format("Failed to create the JSON surrogate for an object of type %s",
                objectClass.getName()),
            e);
      }
    } else {
      LOGGER.trace("Unable to find a JSON surrogate for an object of type {}", objectClass.getName());
    }

    return object;
  }

  public void registerSurrogate(@Nonnull final Class<?> surrogateClass) {
    final JsonSurrogate surrogateInfo = surrogateClass.getAnnotation(JsonSurrogate.class);
    if (surrogateInfo == null) {
      throw new IllegalArgumentException(
          String.format("Cannot add %s as a surrogate - it is not annotated with %s",
              surrogateClass.getName(),
              JsonSurrogate.class.getName()));
    }

    final ISurrogateBuilder<?, ?> surrogateBuilder = createSurrogate(surrogateClass, surrogateInfo.value());
    if (surrogateBuilder == null) {
      throw new IllegalArgumentException(
          String.format("Cannot add %s as a surrogate - it does not have an appropriate constructor",
              surrogateClass.getName()));
    }
    addSurrogateBuilder(surrogateInfo.value(), surrogateBuilder);
  }

  protected <S, R> ISurrogateBuilder<S, R> createSurrogate(final Class<S> surrogateClass, final Class<R> targetClass) {
    Constructor<S> ctor = ClassUtils.getConstructorIfAvailable(surrogateClass, new Class[] { targetClass });
    if (ctor != null) {
      return new DefaultSurrogateBuilder<S, R>(ctor);
    }
    ctor = ClassUtils.getConstructorIfAvailable(surrogateClass, new Class[] { targetClass });
    if (ctor != null) {
      return new DefaultSurrogateBuilder<S, R>(ctor);
    }

    return null;
  }

  public class CollectionSurrogateBuilder<T extends Object, R extends Object>
      implements ISurrogateBuilder<Collection<T>, R> {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Collection<T> buildFor(final R object) {
      // Let's try to transform collections to surrogate collections, but if values
      // can't be transformed we won't
      // complain because Jackson may know how to JSONify them
      //
      // Guava is lazy with transformations and does not memoize the
      // result of the transform. As a result we take a copy of the
      // list so that if enrichment occurs on the values, it is not lost
      return Lists.newArrayList(Collections2.transform((Collection) object, AggregateBuilder.this::buildFor));
    }
  }

  public class MapSurrogateBuilder<T, R> implements ISurrogateBuilder<Map<R, T>, R> {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<R, T> buildFor(final R object) {
      // Let's try to transform maps to surrogate maps, but if values can't be
      // transformed we won't complain
      // because Jackson may know how to JSONify them
      //
      // Note: We don't attempt to transform keys
      //
      // Guava is lazy with transformations and does not memoize the
      // result of the transform. As a result we take a copy of the
      // map so that if enrichment occurs on the values, it is not lost
      return Maps
          .newLinkedHashMap(Maps.transformValues((Map) object, (Function) AggregateBuilder.this::buildFor));
    }
  }

}
