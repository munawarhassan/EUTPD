package com.pmi.tpd.core.elasticsearch.listener;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;
import com.pmi.tpd.core.model.euceg.SubmitterEntity;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public class SubmitterIndexingListener {

    private final IIndexerOperations indexerOperations;

    public SubmitterIndexingListener(final IIndexerOperations indexerOperations) {
        super();
        this.indexerOperations = indexerOperations;
    }

    @PostPersist
    @PostUpdate
    public void postUpdate(final SubmitterEntity entity) {
        indexerOperations.saveSubmitter(SubmitterIndexed.from(entity));
    }

    @PostRemove
    public void postRemove(final SubmitterEntity entity) {
        this.indexerOperations.deleteSubmitter(entity.getId());
    }

}
