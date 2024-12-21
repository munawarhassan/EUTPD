package com.pmi.tpd.core.euceg.spi;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.QProductEntity;
import com.pmi.tpd.database.hibernate.envers.IEnversRevisionRepository;
import com.pmi.tpd.database.jpa.IDslAccessor;
import com.pmi.tpd.euceg.api.ProductType;

/**
 * <p>
 * IProductRepository interface.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public interface IProductRepository
        extends IDslAccessor<ProductEntity, String>, IEnversRevisionRepository<ProductEntity, String, Integer> {

    /**
     *
     */
    @Override
    @Nonnull
    QProductEntity entity();

    /**
     * @return
     */
    @Nonnull
    List<ProductEntity> findAllNewProduct(@Nonnull ProductType productType);

    /**
     * {@inheritDoc}
     */
    @Nonnull
    Page<ProductEntity> findAllValidProduct(ProductType productType, @Nonnull Pageable pageRequest);

    /**
     * Gets the indicating whether the product child is already link to another product than {@code productNumber}.
     *
     * @param previousProductNumber
     *                              the previous product number to verify.
     * @param productNumber
     *                              the possible product number parent.
     * @return Returns {@code true} whether the product child is already link to another product, otherwise
     *         {@code false}.
     * @since 1.7
     */
    boolean hasChildWithAnotherProduct(String previousProductNumber, String productNumber);

    /**
     * @param pageable
     * @param uuid
     * @return
     * @since 3.0
     */
    @Nonnull
    Page<ProductEntity> findAllUseAttachment(@Nonnull Pageable pageable, @Nonnull final String uuid);

}
