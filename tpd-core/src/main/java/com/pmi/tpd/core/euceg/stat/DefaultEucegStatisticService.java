package com.pmi.tpd.core.euceg.stat;

import static com.pmi.tpd.api.util.Assert.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.eu.ceg.SubmissionTypeEnum;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.core.elasticsearch.SearchQuerySupport;
import com.pmi.tpd.core.elasticsearch.model.AttachmentIndexed;
import com.pmi.tpd.core.elasticsearch.model.ProductIndexed;
import com.pmi.tpd.core.elasticsearch.model.SubmissionIndexed;
import com.pmi.tpd.core.elasticsearch.repositories.IProductIndexedRepository;
import com.pmi.tpd.core.elasticsearch.repositories.ISubmissionIndexedRepository;
import com.pmi.tpd.euceg.api.ProductType;
import com.pmi.tpd.euceg.api.entity.AttachmentSendStatus;
import com.pmi.tpd.euceg.api.entity.ProductPirStatus;
import com.pmi.tpd.euceg.api.entity.SubmissionStatus;

public class DefaultEucegStatisticService implements IEucegStatisticService {

    private final ElasticsearchOperations operations;

    @SuppressWarnings("unused")
    private final ISubmissionIndexedRepository submissionIndexedRepository;

    @SuppressWarnings("unused")
    private final IProductIndexedRepository productIndexedRepository;

    public DefaultEucegStatisticService(@Nonnull final ElasticsearchOperations operations,
            @Nonnull final ISubmissionIndexedRepository submissionIndexedRepository,
            @Nonnull final IProductIndexedRepository productIndexedRepository) {
        this.submissionIndexedRepository = checkNotNull(submissionIndexedRepository, "submissionIndexedRepository");
        this.productIndexedRepository = checkNotNull(productIndexedRepository, "productIndexedRepository");
        this.operations = checkNotNull(operations, "operations");
    }

    @Override
    public CountResult countSubmissionByStatus() {
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.terms("submissionStatus").field("submissionStatus"))
                .withMaxResults(0)
                .withTrackTotalHits(true);

        final SearchHits<?> result = operations.search(searchQuery.build(), SubmissionIndexed.class);

        final Terms agg = result.getAggregations().get("submissionStatus");

        final List<SubmissionStatus> submissionStatus = Arrays.asList(SubmissionStatus.values());
        final Map<String, Long> data = submissionStatus.stream()
                .collect(Collectors.toMap(status -> status.name(), status -> 0L));
        submissionStatus.forEach(status -> {
            String name = status.name();
            Bucket b = agg.getBucketByKey(name);
            if (b != null) {
                data.put(name, b.getDocCount());
            }
        });

        return CountResult.builder().count(result.getTotalHits()).partitions(data).build();
    }

    @Override
    public CountResult countProductByPirStatus(@Nonnull final ProductType productType) {
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
                .withQuery(productTypeMatcher(productType))
                .addAggregation(buildPirsStatusAggregation())
                .withMaxResults(0)
                .withTrackTotalHits(true);

        final SearchHits<?> result = operations.search(searchQuery.build(), ProductIndexed.class);

        final Terms agg = result.getAggregations().get("pirstatus");

        final List<ProductPirStatus> productStatus = Arrays.asList(ProductPirStatus.values());
        final Map<String, Long> data = productStatus.stream()
                .collect(Collectors.toMap(status -> status.name(), status -> 0L));
        productStatus.forEach(status -> {
            String name = status.name();
            Bucket b = agg.getBucketByKey(name);
            if (b != null) {
                data.put(name, b.getDocCount());
            }
        });

        return CountResult.builder().count(result.getTotalHits()).partitions(data).build();
    }

    @Override
    public CountResult countAttachmentByStatus() {
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.terms("status").field("status"))
                .withMaxResults(0)
                .withTrackTotalHits(true);

        final SearchHits<?> result = operations.search(searchQuery.build(), AttachmentIndexed.class);

        final Terms agg = result.getAggregations().get("status");

        final List<AttachmentSendStatus> sendStatus = Arrays.asList(AttachmentSendStatus.values());
        final Map<String, Long> data = sendStatus.stream()
                .collect(Collectors.toMap(status -> status.name(), status -> 0L));
        sendStatus.forEach(status -> {
            String name = status.name();
            Bucket b = agg.getBucketByKey(name);
            if (b != null) {
                data.put(name, b.getDocCount());
            }
        });

        return CountResult.builder().count(result.getTotalHits()).partitions(data).build();
    }

    @Override
    public CountResult countProductBySubmissionType(@Nullable final Filters filters, final String query) {
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
                .addAggregation(AggregationBuilders.terms("submissionType").field("submissionType"))
                .withMaxResults(0)
                .withTrackTotalHits(true);
        if (filters != null) {
            searchQuery.withQuery(SearchQuerySupport.buildFilter(filters));
        } else {
            searchQuery.withQuery(QueryBuilders.matchAllQuery());
        }

        final SearchHits<?> result = operations.search(searchQuery.build(), ProductIndexed.class);

        final Terms agg = result.getAggregations().get("submissionType");

        final List<SubmissionTypeEnum> submissionTypes = Arrays.asList(SubmissionTypeEnum.values());
        final Map<String, Long> data = submissionTypes.stream()
                .collect(Collectors.toMap(type -> String.valueOf(type.value()), status -> 0L));
        submissionTypes.forEach(type -> {
            String value = String.valueOf(type.value());
            Bucket b = agg.getBucketByKey(value);
            if (b != null) {
                data.put(value, b.getDocCount());
            }
        });

        return CountResult.builder().count(result.getTotalHits()).partitions(data).build();
    }

    @Override
    public HistogramResult getHistogramCreatedSubmission(final HistogramRequest request) {
        final String serieName = "submission";
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();

        searchQuery.withQuery(request.createRangeQuery())
                .addAggregation(request.createAggregation(serieName, "createdDate"))
                .withMaxResults(0)
                .withTrackTotalHits(true);
        return getHistogramResult(searchQuery.build(), SubmissionIndexed.class, serieName);
    }

    @Override
    public HistogramResult getHistogramCreatedTobaccoProduct(final HistogramRequest request) {
        final String serieName = "tobacco_product";
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();

        searchQuery.withQuery(
            QueryBuilders.boolQuery().must(productTypeMatcher(ProductType.TOBACCO)).must(request.createRangeQuery()))
                .addAggregation(request.createAggregation(serieName, "createdDate"))
                .withMaxResults(0)
                .withTrackTotalHits(true);
        return getHistogramResult(searchQuery.build(), ProductIndexed.class, serieName);
    }

    @Override
    public HistogramResult getHistogramCreatedEcigProduct(final HistogramRequest request) {
        final String serieName = "ecig_product";
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();

        searchQuery.withQuery(
            QueryBuilders.boolQuery().must(productTypeMatcher(ProductType.ECIGARETTE)).must(request.createRangeQuery()))
                .addAggregation(request.createAggregation(serieName, "createdDate"))
                .withMaxResults(0)
                .withTrackTotalHits(true);
        return getHistogramResult(searchQuery.build(), ProductIndexed.class, serieName);
    }

    private static QueryBuilder productTypeMatcher(final ProductType productType) {
        return QueryBuilders.termQuery("productType", productType.toString());
    }

    private AbstractAggregationBuilder<?> buildPirsStatusAggregation() {
        return AggregationBuilders.terms("pirstatus").field("pirStatus");
    }

    private HistogramResult getHistogramResult(Query query, Class<?> cl, String serieName) {
        final SearchHits<?> result = operations.search(query, cl);

        final Histogram agg = (Histogram) result.getAggregations().get(serieName);

        final HistogramResult.HistogramResultBuilder histo = HistogramResult.builder();
        agg.getBuckets()
                .forEach(bucket -> histo.series(bucket.getKeyAsString()).addData(serieName, bucket.getDocCount()));
        return histo.build();

    }
}
