package com.pmi.tpd.core.elasticsearch.listener;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import com.pmi.tpd.core.elasticsearch.IIndexerOperations;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.euceg.api.entity.ISubmissionEntity;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public class SubmissionIndexingListener {

    private final IIndexerOperations indexerOperations;

    public SubmissionIndexingListener(final IIndexerOperations indexerOperations) {
        super();
        this.indexerOperations = indexerOperations;
    }

    @PostPersist
    @PostUpdate
    public void postUpdate(final ISubmissionEntity entity) {
        indexerOperations.saveSubmission(SubmissionIndexed.from(entity));
        synchronizeOlderSubmission(entity);
        indexerOperations.saveProduct(ProductIndexed.from(entity.getProduct()));
    }

    @PostRemove
    public void postRemove(final ISubmissionEntity entity) {
        this.indexerOperations.deleteSubmission(entity.getId());
        synchronizeOlderSubmission(entity);
    }

    public void synchronizeOlderSubmission(final ISubmissionEntity entity) {
        final List<ISubmissionEntity> l = entity.getProduct()
                .getSubmissions()
                .stream()
                .sorted(ISubmissionEntity.LAST_MODIFICATION_DESC_ORDERING)
                .filter(s -> !s.equals(entity))
                .collect(Collectors.toList());
        l.stream()
                .sequential()
                .takeWhile(s -> !SubmissionStatus.SUBMITTED.equals(s.getSubmissionStatus()))
                .forEach(s -> indexerOperations.saveSubmission(SubmissionIndexed.from(s)));
        l.stream()
                .filter(s -> SubmissionStatus.SUBMITTED.equals(s.getSubmissionStatus()))
                .findFirst()
                .ifPresent(s -> indexerOperations.saveSubmission(SubmissionIndexed.from(s)));
    }
}
