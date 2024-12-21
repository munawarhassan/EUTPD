package com.pmi.tpd.core.elasticsearch.listener;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.model.euceg.ProductEntity;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public class ProductIndexingListener {

    /** */
    private final IIndexerOperations indexerOperations;

    public ProductIndexingListener(final IIndexerOperations indexerOperations) {
        super();
        this.indexerOperations = indexerOperations;
    }

    @PostPersist
    @PostUpdate
    public void postUpdate(final ProductEntity entity) {
        indexerOperations.saveProduct(ProductIndexed.from(entity));
    }

    @PostRemove
    public void postRemove(final ProductEntity entity) {
        this.indexerOperations.deleteProduct(entity.getId());
    }

}
