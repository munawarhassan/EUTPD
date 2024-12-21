package com.pmi.tpd.euceg.api.entity;

import javax.annotation.Nonnull;

import org.eu.ceg.EcigProduct;
import org.eu.ceg.Product;
import org.eu.ceg.TobaccoProduct;

/**
 * <p>
 * IProductVisitor interface.
 * </p>
 *
 * @param <T>
 *          a returned object type.
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IProductVisitor<T> {

  /**
   * @param entity
   * @return
   */
  T visit(@Nonnull IProductEntity entity);

  /**
   * @param product
   * @return
   */
  Product visit(@Nonnull Product product);

  /**
   * <p>
   * visit.
   * </p>
   *
   * @param product
   *          a {@link TobaccoProduct} object.
   * @return a T object.
   */
  Product visit(@Nonnull TobaccoProduct product);

  /**
   * <p>
   * visit.
   * </p>
   *
   * @param product
   *          a {@link EcigProduct} object.
   * @return a T object.
   */
  Product visit(@Nonnull EcigProduct product);
}
