package com.pmi.tpd.api.model;

import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.pmi.tpd.api.util.BuilderSupport;

/**
 * <p>
 * Abstract AbstractEntityBuilder class.
 * </p>
 *
 * @author Christophe Friederich
 * @param <K>
 * @param <T>
 * @param <B>
 * @since 1.0
 */
public abstract class AbstractEntityBuilder<K, T extends IIdentityEntity<K>, B extends AbstractEntityBuilder<K, T, B>>
    extends BuilderSupport {

  /** */
  private K id;

  /**
   * <p>
   * Constructor for AbstractEntityBuilder.
   * </p>
   */
  public AbstractEntityBuilder() {
    id = null;
  }

  /**
   * <p>
   * Constructor for AbstractEntityBuilder.
   * </p>
   *
   * @param obj
   *            a T object.
   */
  public AbstractEntityBuilder(@Nonnull final T obj) {
    this.id = obj.getId();
  }

  /**
   * <p>
   * id.
   * </p>
   *
   * @param value
   *              a K object.
   * @return a B object.
   */
  @VisibleForTesting
  public B id(@Nonnull final K value) {
    id = value;
    return self();
  }

  /**
   * <p>
   * id.
   * </p>
   *
   * @return a K object.
   */
  public K id() {
    return id;
  }

  /**
   * <p>
   * self.
   * </p>
   *
   * @return a B object.
   */
  protected abstract B self();

  /**
   * <p>
   * build.
   * </p>
   *
   * @return a T object.
   */
  public abstract T build();
}
