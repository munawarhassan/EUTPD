package com.pmi.tpd.database.hibernate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.Page;

import com.pmi.tpd.api.model.IInitializable;
import com.pmi.tpd.api.util.Assert;

/**
 * Utility classes to simplify working with Hibernate entities.
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public final class HibernateUtils {

  private HibernateUtils() {
    throw new UnsupportedOperationException();
  }

  /**
   * Casts the provided entity to the specified derived class in a way that is
   * {@code HibernateProxy}-safe.
   * <p/>
   * When working with joined inheritance, Hibernate proxies entities using the
   * mapped class, rather than instance-
   * by-instance. In such a situation, entities cannot be directly cast to derived
   * classes because the proxy is not an
   * instance of that type. This method detects Hibernate proxies and initializes
   * them, casting and returning the
   * fully-initialized instance instead of the proxy. If the provided entity is
   * not a proxy it is cast and returned
   * directly.
   *
   * @param baseEntity
   *                     the entity returned by Hibernate, which may be a proxy
   * @param derivedClass
   *                     the class the fully initialized entity is expected to be
   * @param <B>
   *                     The base entity type, as returned by Hibernate
   * @return the provided entity cast to the expected type, or {@code null} if the
   *         provided entity was {@code null}
   * @throws java.lang.ClassCastException
   *                                        if the provided entity is not an
   *                                        instance of the expected derived class
   * @throws java.lang.NullPointerException
   *                                        if the provided {@code derivedClass}
   *                                        is {@code null}
   * @see #tryCast(Object, Class)
   * @param <D>
   *            a D object.
   */
  public static <B, D extends B> D cast(@Nullable B baseEntity, @Nonnull final Class<D> derivedClass) {
    baseEntity = unwrap(baseEntity);

    // There is no need to null-check the value:
    // - instanceof returns false for null
    // - Class.cast is null-safe to be symmetrical with instanceof
    return Assert.checkNotNull(derivedClass, "derivedClass").cast(baseEntity);
  }

  /**
   * Takes the provided {@code Criteria}, applies a row count projection and runs
   * the query, returning the count.
   *
   * @param criteria
   *                 the criteria defining the query to retrieve a row count for
   * @return the row count for the specified query
   */
  public static long count(@Nonnull final Criteria criteria) {
    return (Long) Assert.checkNotNull(criteria, "criteria").setProjection(Projections.rowCount()).uniqueResult();
  }

  /**
   * <p>
   * Initializes the provided {@code entity}. If the entity is a
   * {@code HibernateProxy}, the proxy is unwrapped to its
   * implementation. For proxies created at a lower level , this effectively turns
   * the entity into its real type.
   * </p>
   * In addition to unwrapping proxies, if the entity implements
   * {@link com.pmi.tpd.api.model.IInitializable}
   * that will be invoked <i>after</i> the proxy is unwrapped.
   *
   * @param entity
   *               the entity to initialize
   * @param <T>
   *               The entity's declared type (which is assumed to be identical to
   *               or a superclass of its actual type)
   * @return the initialized entity, <i>which may not be the provided instance</i>
   */
  public static <T> T initialize(T entity) {
    if (entity == null) {
      return null;
    }

    entity = unwrap(entity);
    if (entity instanceof IInitializable) {
      ((IInitializable) entity).initialize();
    }
    return entity;
  }

  /**
   * {@link #initialize(Object) Initializes} each entity in the provided list,
   * returning a <i>new list</i> containing
   * the initialized entities.
   *
   * @param list
   *             a list containing 0 or more entities to initialize
   * @param <T>
   *             The entity's declared type (which is assumed to be identical to
   *             or a superclass of its actual type)
   * @return a <i>new</i> list containing initialized entities
   */
  public static <T> List<T> initializeList(final List<T> list) {
    if (list == null) {
      return null;
    }
    return list.stream().map(HibernateUtils::initialize).collect(Collectors.toUnmodifiableList());
  }

  /**
   * {@link #initialize(Object) Initializes} each entity in the provided page,
   * returning a <i>new page</i> containing
   * the initialized entities.
   *
   * @param page
   *             a page containing 0 or more entities to initialize
   * @param <T>
   *             The entity's declared type (which is assumed to be identical to
   *             or a superclass of its actual type)
   * @return a <i>new</i> page containing initialized entities
   */
  public static <T> Page<T> initializePage(final Page<T> page) {
    if (page == null) {
      return null;
    }
    return page.map(HibernateUtils::initialize);
  }

  /**
   * {@link #initialize(Object) Initializes} each entity in the provided set,
   * returning a <i>new set</i> containing
   * the initialized entities.
   *
   * @param set
   *            a set containing 0 or more entities to initialize
   * @param <T>
   *            The entity's declared type (which is assumed to be identical to or
   *            a superclass of its actual type)
   * @return a <i>new</i> set containing initialized entities
   */
  public static <T> Set<T> initializeSet(final Set<T> set) {
    if (set == null) {
      return null;
    }
    return set.stream().map(HibernateUtils::initialize).collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Casts the provided entity to the specified derived class, <i>if it is an
   * instance of that type</i>, in a way that
   * is {@code HibernateProxy}-aware. If the provided entity is not an instance of
   * the specified type, {@code null}
   * will be returned instead of throwing a {@code ClassCastException}.
   * <p/>
   * When working with joined inheritance, Hibernate proxies entities using the
   * mapped class, rather than instance-
   * by-instance. In such a situation, entities cannot be directly cast to derived
   * classes because the proxy is not an
   * instance of that type. This method detects Hibernate proxies and initializes
   * them, casting and returning the
   * fully-initialized instance instead of the proxy. If the provided entity is
   * not a proxy it is cast and returned
   * directly.
   *
   * @param baseEntity
   *                     the entity returned by Hibernate, which may be a proxy
   * @param derivedClass
   *                     the class the fully initialized entity may be
   * @param <B>
   *                     The base entity type, as returned by Hibernate
   * @return the provided entity if it can be cast to the specified type, or
   *         {@code null} the provided entity was
   *         {@code null} or was not an instance of the specified type
   * @throws java.lang.NullPointerException
   *                                        if the provided {@code derivedClass}
   *                                        is {@code null}
   * @see #cast(Object, Class)
   * @param <D>
   *            a D object.
   */
  @Nullable
  public static <B, D extends B> D tryCast(@Nullable B baseEntity, @Nonnull final Class<D> derivedClass) {
    baseEntity = unwrap(baseEntity);

    // There is no need to null-check the value:
    // - instanceof returns false for null
    // - Class.cast and Class.isInstance are both null-safe to be symmetrical with
    // instanceof
    return Assert.checkNotNull(derivedClass, "derivedClass").isInstance(baseEntity) ? derivedClass.cast(baseEntity)
        : null;
  }

  /**
   * Unwraps the provided {@code entity}, if it is a {@code HibernateProxy},
   * returning the initialized instance.
   *
   * @param entity
   *               the entity to unwrap, if proxied
   * @param <T>
   *               The entity's declared type (which is assumed to be identical to
   *               or a superclass of its actual type)
   * @return the provided entity, if it is not proxied, or the initialized
   *         instance if it is
   */
  @SuppressWarnings("unchecked")
  private static <T> T unwrap(final T entity) {
    if (entity instanceof HibernateProxy) {
      // The entity returned here will be _at least_ a T. For example, if the provided
      // entity
      // is an InternalProject
      // but its implementation type is InternalPersonalProject, it's still an
      // InternalProject. As a result, while
      // this cast is, unfortunately, unchecked, it should always be safe
      return (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
    }
    return entity;
  }

}
