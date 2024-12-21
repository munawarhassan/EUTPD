package com.pmi.tpd.core.elasticsearch;

import static com.pmi.tpd.core.elasticsearch.utils.IdGenerator.nextIdAsString;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableLong;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.util.CloseableIterator;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.Lists;
import com.pmi.tpd.api.paging.Filter;
import com.pmi.tpd.api.paging.PageUtils;
import com.pmi.tpd.core.elasticsearch.junit.jupiter.ElasticsearchRestTemplateConfiguration;
import com.pmi.tpd.core.elasticsearch.junit.jupiter.SpringTestcontainers;
import com.pmi.tpd.testing.junit5.TestCase;

@SpringTestcontainers(disabledWithoutDocker = true)
@ContextConfiguration(classes = { ElasticsearchRestTemplateConfiguration.class })
public class SearchQuerySupportTestIT extends TestCase {

    @Inject
    private ElasticsearchOperations operations;

    private IndexOperations indexOperations;

    @BeforeEach
    public void before() {
        indexOperations = operations.indexOps(SampleEntity.class);
        indexOperations.delete();
        indexOperations.create();
        indexOperations.putMapping(SampleEntity.class);
        indexOperations.refresh();
    }

    @AfterEach
    void after() {
        indexOperations.delete();
    }

    @Test
    public void shouldPerformAndOperationOnEqualFilter() {

        // given
        final SampleEntity sampleEntity1 = SampleEntity.builder().id(nextIdAsString()).type("person").build();
        operations.save(sampleEntity1);
        final SampleEntity sampleEntity2 = SampleEntity.builder().id(nextIdAsString()).type("shape").build();
        operations.save(sampleEntity2);
        indexOperations.refresh();

        // when
        final Pageable request = PageUtils.newRequest(0, 5, null, Filter.eq("type", "person"));

        final Page<SampleEntity> response = findAll(request, SampleEntity.class);

        // then
        assertThat(response, Matchers.notNullValue());
        assertThat(response.getNumberOfElements(), Matchers.is(1));
        assertThat(response.get().findFirst().get().getId(), Matchers.is(sampleEntity1.getId()));
    }

    @Test
    public void shouldPerformAndOperationOnContainFilter() {

        // given
        final SampleEntity sampleEntity1 = SampleEntity.builder().id(nextIdAsString()).type("person").build();
        operations.save(sampleEntity1);
        final SampleEntity sampleEntity2 = SampleEntity.builder().id(nextIdAsString()).type("shape").build();
        operations.save(sampleEntity2);
        indexOperations.refresh();

        // when
        final Pageable request = PageUtils.newRequest(0, 5, null, Filter.contains("type", "per*"));

        final Page<SampleEntity> response = findAll(request, SampleEntity.class);

        // then
        assertThat(response, Matchers.notNullValue());
        assertThat(response.getNumberOfElements(), Matchers.is(1));
        assertThat(response.get().findFirst().get().getId(), Matchers.is(sampleEntity1.getId()));
    }

    @Test
    public void shouldPerformAndOperationOnBetweenFilter() {

        final DateTime from = DateTime.now();
        final DateTime to = from.plusDays(5);

        // given
        final SampleEntity sampleEntity1 = SampleEntity.builder()
                .id(nextIdAsString())
                .createdDate(from.minusDays(1))
                .build();
        operations.save(sampleEntity1);
        final SampleEntity sampleEntity2 = SampleEntity.builder().id(nextIdAsString()).createdDate(from).build();
        operations.save(sampleEntity2);
        final SampleEntity sampleEntity3 = SampleEntity.builder()
                .id(nextIdAsString())
                .createdDate(to.minusDays(1))
                .build();
        operations.save(sampleEntity3);
        final SampleEntity sampleEntity4 = SampleEntity.builder().id(nextIdAsString()).createdDate(to).build();
        operations.save(sampleEntity4);
        final SampleEntity sampleEntity5 = SampleEntity.builder()
                .id(nextIdAsString())
                .createdDate(to.plusDays(1))
                .build();
        operations.save(sampleEntity5);

        indexOperations.refresh();

        // when

        final Pageable request = PageUtils.newRequest(0, 5, null, Filter.between("createdDate", from, to));

        final Page<SampleEntity> response = findAll(request, SampleEntity.class);

        // then
        assertThat(response, Matchers.notNullValue());
        assertThat(response.getNumberOfElements(), Matchers.is(3));
        assertThat(response.map(SampleEntity::getId).getContent(),
            Matchers.contains(sampleEntity2.getId(), sampleEntity3.getId(), sampleEntity4.getId()));
    }

    @Test
    public void shouldStreamReturnNotLimitMaxLimit() {
        // given
        for (int i = 0; i < 100; i++) {
            final List<SampleEntity> list = Lists.newArrayListWithCapacity(200);
            for (int j = 0; j < 200; j++) {
                final SampleEntity sampleEntity = SampleEntity.builder()
                        .id(nextIdAsString())
                        .createdDate(DateTime.now())
                        .build();
                list.add(sampleEntity);
            }
            operations.save(list);

        }
        indexOperations.refresh();

        // when

        final Pageable pageable = PageUtils.newRequest(0, 20);

        final Stream<SampleEntity> samples = findAllForStream(pageable, SampleEntity.class);

        // then
        assertThat(samples, Matchers.notNullValue());

        final MutableLong count = new MutableLong(0);
        samples.forEach(s -> {
            count.increment();
            assertThat(s, Matchers.isA(SampleEntity.class));
        });
        assertThat(count.getValue(), Matchers.is(20000L));

    }

    @Test
    public void shouldFailWhenPageReturnNotLimitMaxLimit() {
        // given
        for (int i = 0; i < 100; i++) {
            final List<SampleEntity> list = Lists.newArrayListWithCapacity(200);
            for (int j = 0; j < 200; j++) {
                final SampleEntity sampleEntity = SampleEntity.builder()
                        .id(nextIdAsString())
                        .createdDate(DateTime.now())
                        .build();
                list.add(sampleEntity);
            }
            operations.save(list);

        }
        indexOperations.refresh();

        // when

        final Pageable pageable = PageUtils.newRequest(0, 20);

        final Iterable<SampleEntity> samples = PageUtils.asIterable(request -> findAll(request, SampleEntity.class),
            pageable);
        final MutableLong count = new MutableLong(0);
        Assertions.assertThrows(UncategorizedElasticsearchException.class,
            () -> samples.forEach(s -> count.increment()));

    }

    private <T> Page<T> findAll(@Nonnull final Pageable pageable, final Class<T> entityClass) {
        return queryForPage(SearchQuerySupport.queryBuilder(entityClass, pageable, true), entityClass);
    }

    @SuppressWarnings("unchecked")
    private <T> Stream<T> findAllForStream(@Nonnull final Pageable pageable, final Class<T> entityClass) {
        final CloseableIterator<T> it = (CloseableIterator<T>) SearchHitSupport.unwrapSearchHits(
            searchForStream(SearchQuerySupport.queryBuilder(entityClass, pageable, true), entityClass));
        return it.stream();
    }

    @SuppressWarnings("unchecked")
    private <T> Page<T> queryForPage(final NativeSearchQuery query, final Class<T> entityClass) {
        final SearchHits<T> searchHits = operations
                .search(query, entityClass, operations.getIndexCoordinatesFor(entityClass));
        final SearchPage<T> page = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        return (Page<T>) SearchHitSupport.unwrapSearchHits(page);
    }

    private <T> SearchHitsIterator<T> searchForStream(final NativeSearchQuery query, final Class<T> entityClass) {
        final SearchHitsIterator<T> searchHits = operations
                .searchForStream(query, entityClass, operations.getIndexCoordinatesFor(entityClass));
        return searchHits;
    }

}
