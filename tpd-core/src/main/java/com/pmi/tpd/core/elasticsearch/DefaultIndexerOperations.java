package com.pmi.tpd.core.elasticsearch;

import static com.pmi.tpd.api.util.Assert.checkHasText;
import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.elasticsearch.ElasticsearchException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.util.CloseableIterator;
import org.springframework.lang.Nullable;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.i18n.I18nService;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmitterIndexed;
import com.pmi.tpd.core.elasticsearch.repositories.IAttachmentIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.IProductIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.ISubmissionIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.ISubmitterIndexedRepository;

/**
 * @author Christophe Friederich
 * @since 1.4
 */
public class DefaultIndexerOperations implements IIndexerOperations {

    /** */
    private final I18nService i18nService;

    /** */
    private final ElasticsearchOperations elasticsearchTemplate;

    /** */
    private final ISubmissionIndexedRepository submissionIndexRepository;

    /** */
    private final IProductIndexedRepository productIndexRepository;

    /** */
    private final ISubmitterIndexedRepository submitterIndexedRepository;

    /** */
    private final IAttachmentIndexedRepository attachmentIndexedRepository;

    @Inject
    public DefaultIndexerOperations(@Nonnull final ElasticsearchOperations elasticsearchTemplate,
            @Nonnull final I18nService i18nService,
            @Nonnull final ISubmissionIndexedRepository submissionIndexRepository,
            @Nonnull final IProductIndexedRepository productIndexRepository,
            @Nonnull final ISubmitterIndexedRepository submitterIndexedRepository,
            @Nonnull final IAttachmentIndexedRepository attachmentIndexedRepository) {
        this.elasticsearchTemplate = checkNotNull(elasticsearchTemplate, "elasticsearchTemplate");
        this.submissionIndexRepository = checkNotNull(submissionIndexRepository, "submissionIndexRepository");
        this.productIndexRepository = checkNotNull(productIndexRepository, "productIndexRepository");
        this.submitterIndexedRepository = checkNotNull(submitterIndexedRepository, "submitterIndexedRepository");
        this.attachmentIndexedRepository = checkNotNull(attachmentIndexedRepository, "attachmentIndexedRepository");
        this.i18nService = checkNotNull(i18nService, "i18nService");
    }

    @Override
    public void clearAll() {
        final List<Class<?>> classes = Lists.newArrayList(SubmissionIndexed.class,
            SubmitterIndexed.class,
            ProductIndexed.class,
            AttachmentIndexed.class);
        classes.stream().forEach(cl -> {
            elasticsearchTemplate.indexOps(cl).delete();
            elasticsearchTemplate.indexOps(cl).create();
            elasticsearchTemplate.indexOps(cl).putMapping();
            elasticsearchTemplate.indexOps(cl).refresh();
        });
    }

    @Override
    public void optimize() {

    }

    @Override
    @Nonnull
    public Page<SubmissionIndexed> findAllSubmission(@Nonnull final Pageable pageable) {
        return execute(() -> this.submissionIndexRepository.findAll(pageable));
    }

    @Override
    @Nonnull
    public Stream<SubmissionIndexed> findAllSubmissionForStream(@Nonnull final Pageable pageable,
        final long maxElement) {
        return execute(() -> findAllForStream(pageable, SubmissionIndexed.class, maxElement));
    }

    @Override
    @Nonnull
    public Page<ProductIndexed> findAllProduct(@Nonnull final Pageable pageable) {
        return execute(() -> this.productIndexRepository.findAll(pageable));
    }

    @Override
    @Nonnull
    public Page<SubmitterIndexed> findAllSubmitter(@Nonnull final Pageable pageable) {
        return execute(() -> this.submitterIndexedRepository.findAll(pageable));
    }

    @Override
    public Page<AttachmentIndexed> findAllAttachment(final @Nonnull Pageable pageable) {
        return execute(() -> this.attachmentIndexedRepository.findAll(pageable));
    }

    @Override
    public void saveProduct(@Nonnull final ProductIndexed entity) {
        execute(() -> this.productIndexRepository.save(checkNotNull(entity, "entity")));
    }

    @Override
    public void saveAllProduct(final @Nonnull Iterable<ProductIndexed> entities) {
        execute(() -> this.productIndexRepository.saveAll(checkNotNull(entities, "entities")));
    }

    @Override
    public void deleteProduct(@Nonnull final String id) {
        execute(() -> {
            this.productIndexRepository.deleteById(checkHasText(id, "id"));
            return null;
        });
    }

    @Override
    public void saveSubmission(final @Nonnull SubmissionIndexed entity) {
        execute(() -> this.submissionIndexRepository.save(checkNotNull(entity, "entity")));
    }

    @Override
    public void saveAllSubmission(final @Nonnull Iterable<SubmissionIndexed> entities) {
        execute(() -> this.submissionIndexRepository.saveAll(checkNotNull(entities, "entities")));
    }

    @Override
    public void deleteSubmission(final @Nonnull Long id) {
        execute(() -> {
            this.submissionIndexRepository.deleteById(checkNotNull(id, "id"));
            return null;
        });
    }

    @Override
    public void saveSubmitter(final @Nonnull SubmitterIndexed entity) {
        execute(() -> this.submitterIndexedRepository.save(checkNotNull(entity, "entity")));
    }

    @Override
    public void saveAllSubmitter(final @Nonnull Iterable<SubmitterIndexed> entities) {
        execute(() -> this.submitterIndexedRepository.saveAll(checkNotNull(entities, "entities")));
    }

    @Override
    public void deleteSubmitter(@Nonnull final String id) {
        execute(() -> {
            this.submitterIndexedRepository.deleteById(checkHasText(id, "id"));
            return null;
        });
    }

    @Override
    public void saveAttachment(@Nonnull final AttachmentIndexed entity) {
        execute(() -> this.attachmentIndexedRepository.save(checkNotNull(entity, "entity")));
    }

    @Override
    public void saveAllAttachment(final @Nonnull Iterable<AttachmentIndexed> entities) {
        execute(() -> this.attachmentIndexedRepository.saveAll(checkNotNull(entities, "entities")));
    }

    @Override
    public void deleteAttachment(@Nonnull final String id) {
        execute(() -> {
            this.attachmentIndexedRepository.deleteById(checkHasText(id, "id"));
            return null;
        });
    }

    @FunctionalInterface
    public interface ExcecuteCallback<R> {

        @Nullable
        R apply();
    }

    @Nullable
    public <R> R execute(final ExcecuteCallback<R> callback) {
        try {
            return callback.apply();
        } catch (final ElasticsearchException e) {
            throw translateException(e);
        }
    }

    private RuntimeException translateException(final ElasticsearchException exception) {

        return new IndexingException(i18nService.createKeyedMessage("app.service.index.unrecognized"), exception);
    }

    @SuppressWarnings({ "unchecked", "resource" })
    @Nonnull
    private <T> Stream<T> findAllForStream(@Nonnull final Pageable pageable,
        final Class<T> entityClass,
        final long maxElements) {
        final CloseableIterator<T> it = (CloseableIterator<T>) SearchHitSupport.unwrapSearchHits(
            searchForStream(SearchQuerySupport.queryBuilder(entityClass, pageable, true), entityClass));
        if (maxElements > 0) {
            return new LengthLimitedIterator<T>(it, maxElements).stream();
        }
        return it.stream();
    }

    private <T> SearchHitsIterator<T> searchForStream(final NativeSearchQuery query, final Class<T> entityClass) {
        final SearchHitsIterator<T> searchHits = this.elasticsearchTemplate
                .searchForStream(query, entityClass, elasticsearchTemplate.getIndexCoordinatesFor(entityClass));
        return searchHits;
    }

    public static class LengthLimitedIterator<T> implements CloseableIterator<T> {

        private CloseableIterator<T> wrapped;

        private long length;

        private long count;

        public LengthLimitedIterator(final CloseableIterator<T> wrapped, final long length) {
            Assert.state(length > 0, "length should be greater than 0");
            this.wrapped = wrapped;
            this.length = length;
        }

        @Override
        public boolean hasNext() {
            if (count < length) {
                return wrapped.hasNext();
            }
            return false;
        }

        @Override
        public T next() {
            final T value = wrapped.next();
            count++;
            return value;
        }

        @Override
        public void close() {
            this.wrapped.close();
        }

    }
}
