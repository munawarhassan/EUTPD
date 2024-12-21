package com.pmi.tpd.core.elasticsearch;

import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public interface IIndexerOperations {

    /**
     *
     */
    void clearAll();

    /**
     *
     */
    void optimize();

    /**
     * @param pageable
     * @return
     */
    @Nonnull
    Page<SubmissionIndexed> findAllSubmission(@Nonnull Pageable pageable);

    /**
     * @param pageable
     * @return
     */
    @Nonnull
    Stream<SubmissionIndexed> findAllSubmissionForStream(@Nonnull final Pageable pageable, long maxEelement);

    /**
     * @param pageable
     * @return
     */
    @Nonnull
    Page<ProductIndexed> findAllProduct(@Nonnull Pageable pageable);

    /**
     * @param pageable
     * @return
     */
    @Nonnull
    Page<SubmitterIndexed> findAllSubmitter(@Nonnull Pageable pageable);

    /**
     * @param pageable
     * @return
     */
    Page<AttachmentIndexed> findAllAttachment(@Nonnull Pageable pageable);

    /**
     * @param entity
     */
    void saveProduct(@Nonnull ProductIndexed entity);

    /**
     * @param entity
     */
    void saveAllProduct(@Nonnull Iterable<ProductIndexed> entities);

    /**
     * @param id
     */
    void deleteProduct(@Nonnull String id);

    /**
     * @param entity
     */
    void saveSubmission(@Nonnull SubmissionIndexed entity);

    /**
     * @param entity
     */
    void saveAllSubmission(@Nonnull Iterable<SubmissionIndexed> entities);

    /**
     * @param id
     */
    void deleteSubmission(@Nonnull Long id);

    /**
     * @param entity
     */
    void saveSubmitter(@Nonnull SubmitterIndexed entity);

    /**
     * @param entity
     */
    void saveAllSubmitter(@Nonnull Iterable<SubmitterIndexed> entities);

    /**
     * @param id
     */
    void deleteSubmitter(@Nonnull String id);

    /**
     * @param entity
     * @since 2.2
     */
    void saveAttachment(@Nonnull AttachmentIndexed entity);

    /**
     * @param entity
     * @since 2.2
     */
    void saveAllAttachment(@Nonnull Iterable<AttachmentIndexed> entities);

    /**
     * @param id
     * @since 2.2
     */
    void deleteAttachment(@Nonnull String id);

}
