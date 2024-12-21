package com.pmi.tpd.api.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Class utility methods.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class ClassUtils {

  private ClassUtils() {
  }

  /**
   * Cast templated class.
   *
   * @param cl
   *           a templated class.
   * @return Returns same casted instance.
   * @param <T>
   *            a T object type.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> cast(final Class<?> cl) {
    return (Class<T>) cl;
  }

  /**
   * Finds all super classes and interfaces for a given class.
   *
   * @param cls
   *            The class to scan
   * @return The collected related classes found
   */
  @Nonnull
  public static Set<Class<?>> findAllTypes(@Nonnull final Class<?> cls) {
    final Set<Class<?>> types = Sets.newHashSet();
    findAllTypes(cls, types);
    return types;
  }

  /**
   * Finds all super classes and interfaces for a given class.
   *
   * @param cls
   *              The class to scan
   * @param types
   *              The collected related classes found
   */
  public static void findAllTypes(@Nullable final Class<?> cls, @Nonnull final Set<Class<?>> types) {
    if (cls == null) {
      return;
    }

    // check to ensure it hasn't been scanned yet
    if (types.contains(cls)) {
      return;
    }

    types.add(cls);

    findAllTypes(cls.getSuperclass(), types);
    for (int x = 0; x < cls.getInterfaces().length; x++) {
      findAllTypes(cls.getInterfaces()[x], types);
    }
  }

  /**
   * Get the underlying class for a type, or null if the type is a variable type.
   *
   * @param type
   *             the type
   * @return the underlying class
   */
  @Nullable
  @CheckReturnValue
  private static Class<?> getClass(@Nullable final Type type) {
    if (type instanceof Class) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      return getClass(((ParameterizedType) type).getRawType());
    } else if (type instanceof GenericArrayType) {
      final Type componentType = ((GenericArrayType) type).getGenericComponentType();
      final Class<?> componentClass = getClass(componentType);
      if (componentClass != null) {
        return Array.newInstance(componentClass, 0).getClass();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  /**
   * Get the actual type arguments a child class has used to extend a generic base
   * class.
   *
   * @param baseClass
   *                   the base class
   * @param childClass
   *                   the child class
   * @return a list of the raw classes for the actual type arguments.
   * @throws java.lang.IllegalArgumentException
   *                                            If the child class is not the base
   *                                            of the baseClass
   * @since 1.0
   * @param <T>
   *            a T object.
   */
  @Nonnull
  public static <T> List<Class<?>> getTypeArguments(@Nullable final Class<T> baseClass,
      @Nonnull final Class<? extends T> childClass) {
    final Map<Type, Type> resolvedTypes = Maps.newHashMap();
    Type type = childClass;
    Class<?> typeClass = getClass(type);
    // start walking up the inheritance hierarchy until we hit baseClass
    while (!typeClass.equals(baseClass)) {
      if (type instanceof Class) {
        // there is no useful information for us in raw types, so just keep going.
        type = ((Class<?>) type).getGenericSuperclass();
      } else {
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        final Class<?> rawType = (Class<?>) parameterizedType.getRawType();

        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        final TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
        for (int i = 0; i < actualTypeArguments.length; i++) {
          resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
        }

        if (!rawType.equals(baseClass)) {
          type = rawType.getGenericSuperclass();
        }
      }
      typeClass = getClass(type);
      if (typeClass == null) {
        throw new IllegalArgumentException("Unable to find the class for the type " + type);
      }
    }

    // finally, for each actual type argument provided to baseClass, determine (if
    // possible)
    // the raw class for that type argument.
    Type[] actualTypeArguments;
    if (type instanceof Class) {
      actualTypeArguments = ((Class<?>) type).getTypeParameters();
    } else {
      actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
    }
    final List<Class<?>> typeArgumentsAsClasses = new ArrayList<>();
    // resolve types by chasing down type variables.
    for (Type baseType : actualTypeArguments) {
      while (resolvedTypes.containsKey(baseType)) {
        baseType = resolvedTypes.get(baseType);
      }
      typeArgumentsAsClasses.add(getClass(baseType));
    }
    return typeArgumentsAsClasses;
  }

}
