package com.pmi.tpd.core.euceg.impl;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.euceg.spi.IProductIdGeneratorRepository;
import com.pmi.tpd.core.model.euceg.ProductIdEntity;
import com.pmi.tpd.core.model.euceg.QProductIdEntity;
import com.pmi.tpd.database.jpa.DefaultJpaRepository;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
@Repository
public class JpaProductIdGeneratorRepository extends DefaultJpaRepository<ProductIdEntity, String>
        implements IProductIdGeneratorRepository {

    /**
     * @param entityManager
     *            the JPA entity manager.
     */
    public JpaProductIdGeneratorRepository(final EntityManager entityManager) {
        super(ProductIdEntity.class, entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QProductIdEntity entity() {
        return QProductIdEntity.productIdEntity;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String getNextProductId(final String submitterId) {
        Assert.checkNotNull(submitterId, "submitterId");
        ProductIdEntity productId = null;
        final Optional<ProductIdEntity> option = this.findById(submitterId);
        if (!option.isPresent()) {
            productId = new ProductIdEntity(submitterId);
            productId = this.saveAndFlush(productId);
        } else {
            productId = option.get();
        }
        final String id = SubmissionProductIdGenerator.generate(submitterId, productId.getCurrentValue());
        productId.incr();
        this.saveAndFlush(productId);
        return id;
    }

}
