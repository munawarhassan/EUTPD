package com.pmi.tpd.core.euceg.impl;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.pmi.tpd.api.paging.DslPagingHelper;
import com.pmi.tpd.core.euceg.spi.IProductRepository;
import com.pmi.tpd.core.model.euceg.ProductEntity;
import com.pmi.tpd.core.model.euceg.QProductEntity;
import com.pmi.tpd.database.hibernate.envers.DefaultJpaEnversRevisionRepository;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.ProductStatus;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Repository
public class JpaProductRepository extends DefaultJpaEnversRevisionRepository<ProductEntity, String, Integer>
        implements IProductRepository {

    /**
     * @param entityManager
     *                      the JPA entity manager.
     */
    public JpaProductRepository(final EntityManager entityManager) {
        super(ProductEntity.class, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public QProductEntity entity() {
        return QProductEntity.productEntity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProductEntity> findAllNewProduct(@Nonnull final ProductType productType) {
        checkNotNull(productType, "productType");
        return from()
                .where(entity().submissions.isEmpty()
                        .and(entity().status.eq(ProductStatus.VALID).and(entity().productType.eq(productType))))
                .orderBy(entity().lastModifiedDate.desc())
                .fetch();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Page<ProductEntity> findAllValidProduct(@Nonnull final ProductType productType,
        @Nonnull final Pageable pageRequest) {
        checkNotNull(productType, "productType");
        return toPage(
            from().where(DslPagingHelper.createPredicates(pageRequest, entity()))
                    .where(entity().status.eq(ProductStatus.VALID).and(entity().productType.eq(productType)))
                    .orderBy(entity().lastModifiedDate.desc()),
            pageRequest);
    }

    @Nonnull
    public Page<ProductEntity> findAllUseAttachment(@Nonnull final Pageable pageable, @Nonnull final String uuid) {
        checkNotNull(uuid, "uuid");
        return findAll(entity().attachments.contains(uuid), pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasChildWithAnotherProduct(final String previousProductNumber, final String productNumber) {
        return from()
                .where(entity().child.id.equalsIgnoreCase(previousProductNumber)
                        .and(entity().id.notEqualsIgnoreCase(productNumber)))
                .fetchCount() > 0;
    }
}
