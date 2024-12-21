package com.pmi.tpd.core.elasticsearch;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchEntityInformation;
import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;

public class BaseElasticsearchRepository<T, ID> extends SimpleElasticsearchRepository<T, ID> {

    public BaseElasticsearchRepository(final ElasticsearchEntityInformation<T, ID> metadata,
            final ElasticsearchOperations operations) {
        super(metadata, operations);
    }

    @Override
    @Nonnull
    public Page<T> findAll(@Nonnull final Pageable pageable) {
        return queryForPage(SearchQuerySupport.queryBuilder(entityClass, pageable, true), entityClass);
    }

    @SuppressWarnings("unchecked")
    protected Page<T> queryForPage(final NativeSearchQuery query, final Class<T> entityClass) {
        final SearchHits<T> searchHits = execute(
            operations -> operations.search(query, entityClass, operations.getIndexCoordinatesFor(entityClass)));
        final SearchPage<T> page = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

}
