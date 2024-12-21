package com.pmi.tpd.core.elasticsearch.listener;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.model.euceg.AttachmentEntity;

/**
 * @author Christophe Friederich
 * @since 2.2
 */
public class AttachmentIndexingListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentIndexingListener.class);

    /** */
    private final IIndexerOperations indexerOperations;

    public AttachmentIndexingListener(final IIndexerOperations indexerOperations) {
        super();
        this.indexerOperations = indexerOperations;
    }

    @PostPersist
    @PostUpdate
    public void postUpdate(final AttachmentEntity entity) {
        final AttachmentIndexed att = AttachmentIndexed.from(entity);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("IndexedAttachment: {}", att);
        }
        indexerOperations.saveAttachment(att);
    }

    @PostRemove
    public void postRemove(final AttachmentEntity entity) {
        this.indexerOperations.deleteAttachment(entity.getId());
    }

}
