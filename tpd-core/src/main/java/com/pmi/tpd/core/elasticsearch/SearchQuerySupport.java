package com.pmi.tpd.core.elasticsearch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import com.google.common.base.Strings;
import com.pmi.tpd.api.paging.Filter;
import com.pmi.tpd.api.paging.Filter.Operator;
import com.pmi.tpd.api.paging.Filters;
import com.pmi.tpd.api.paging.IFilterable;

public final class SearchQuerySupport {

    @Nonnull
    public static NativeSearchQuery queryBuilder(@Nonnull final Class<?> entityClass,
        @Nonnull final Pageable pageable,
        final boolean totalHits) {
        final NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        searchQuery.withPageable(pageable);
        if (pageable instanceof IFilterable) {
            final IFilterable filterable = (IFilterable) pageable;

            if (!Strings.isNullOrEmpty(filterable.getQuery())) {
                searchQuery.withQuery(QueryBuilders.queryStringQuery(filterable.getQuery()));
            } else {
                searchQuery.withQuery(QueryBuilders.matchAllQuery());
            }
            searchQuery.withFilter(buildFilter(filterable.getFilterMap()));

        }
        searchQuery.withTrackTotalHits(totalHits);
        return searchQuery.build();
    }

    @Nonnull
    public static BoolQueryBuilder buildFilter(@Nonnull final Filters filters) {
        return buildFilter(filters.stream()
                .collect(Collectors.groupingBy(Filter::getProperty, Collectors.toCollection(Filters::new))));

    }

    @Nonnull
    public static BoolQueryBuilder buildFilter(@Nonnull final Map<String, Filters> filters) {
        final BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (!filters.isEmpty()) {

            filters.forEach((property, filterList) -> {

                // merge all exact terms for one property to produce a filter like:
                // "post_filter": {
                // "bool": {
                // "must": [
                // {
                // "terms": {
                // "pirStatus": [
                // "AWAITING",
                // "ACTIVE"
                // ],
                // ...
                // }
                final List<String> values = filterList.stream()
                        .filter(f -> Operator.eq == f.getOperator())
                        .map(f -> f.getValue().filter(v -> !Strings.isNullOrEmpty(v.toString())))
                        .filter(v -> v.isPresent())
                        .map(v -> v.get().toString())
                        .collect(Collectors.toList());
                if (!values.isEmpty()) {
                    addQueryBuilder(queryBuilder, property, QueryBuilders.termsQuery(property, values), false);
                }

                for (final Filter<?> filter : filterList) {
                    if (filter.isEmptyOrNull()) {
                        continue;
                    }

                    switch (filter.getOperator()) {
                        case eq: {
                            // skip already done
                            break;
                        }
                        case noteq: {
                            String value = filter.getValue().get().toString();
                            addQueryBuilder(queryBuilder, property, QueryBuilders.termsQuery(property, value), true);
                            break;
                        }
                        case in: {
                            Collection<?> value = filter.getValues();
                            addQueryBuilder(queryBuilder,
                                property,
                                QueryBuilders.termsQuery(property,
                                    value.stream().map(v -> v.toString().toLowerCase()).collect(Collectors.toList())),
                                false);
                            break;
                        }
                        case notin: {
                            Collection<?> value = filter.getValues();
                            addQueryBuilder(queryBuilder,
                                property,
                                QueryBuilders.termsQuery(property,
                                    value.stream().map(v -> v.toString().toLowerCase()).collect(Collectors.toList())),
                                true);
                            break;
                        }
                        case between: {
                            Object from = filter.from().orElse(null);
                            Object to = filter.to().orElse(null);
                            RangeQueryBuilder range = QueryBuilders.rangeQuery(property);
                            if (from != null) {
                                range.from(from);
                            }
                            if (to != null) {
                                range.to(to);
                            }
                            addQueryBuilder(queryBuilder, property, range, false);
                            break;
                        }
                        case contains:
                        default: {
                            String value = filter.getValue().get().toString();
                            addQueryBuilder(queryBuilder,
                                property,
                                QueryBuilders.wildcardQuery(property, value.toLowerCase()),
                                false);

                            break;
                        }
                    }
                }
            });
        }
        return queryBuilder;
    }

    // public <T> Page<T> findAll(final Class<T> entityClass, @Nonnull final Pageable pageable) {
    // final CriteriaQuery criteriaQuery = queryFilter(entityClass, pageable);
    // if (criteriaQuery != null) {
    // return elasticsearchTemplate.queryForPage(criteriaQuery, entityClass);
    // } else {
    // final SearchQuery query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery())
    // .withPageable(pageable)
    // .build();
    // return elasticsearchTemplate.queryForPage(query, entityClass);
    // }
    // }

    /**
     * @param entityClass
     * @param pageable
     * @return
     */
    @Nonnull
    public static CriteriaQuery queryFilter(@Nonnull final Class<?> entityClass, @Nonnull final Pageable pageable) {

        Criteria criteria = null;
        if (pageable instanceof IFilterable) {
            final IFilterable filterable = (IFilterable) pageable;
            final Map<String, Filters> filters = filterable.getFilterMap();
            if (filters.isEmpty()) {
                return new CriteriaQuery(new Criteria(), pageable);
            }
            if (!Strings.isNullOrEmpty(filterable.getQuery())) {
                criteria = new Criteria();
                criteria.expression(filterable.getQuery());
            }
            for (final List<Filter<?>> filterList : filters.values()) {

                Criteria anotherCriteria = null;
                for (final Filter<?> filter : filterList) {
                    final Optional<?> val = filter.getValue();
                    if (!val.isPresent() || Strings.isNullOrEmpty(val.get().toString())) {
                        continue;
                    }
                    final String property = filter.getProperty();
                    final String value = val.get().toString();
                    Criteria aCriteria = new Criteria(property);
                    switch (filter.getOperator()) {
                        case eq:
                            aCriteria = aCriteria.is(value);
                            break;
                        case between: {
                            final Object from = filter.from().orElse(null);
                            final Object to = filter.to().orElse(null);
                            aCriteria = aCriteria.between(from, to);
                            break;
                        }
                        case contains:
                        default:
                            aCriteria = aCriteria.contains(value.toLowerCase());

                            break;
                    }
                    if (anotherCriteria == null) {
                        anotherCriteria = aCriteria;
                    } else {
                        anotherCriteria = anotherCriteria.or(aCriteria);
                    }
                }
                if (criteria == null) {
                    criteria = anotherCriteria;
                } else {
                    criteria = criteria.and(anotherCriteria);
                }
            } ;
        }
        return new CriteriaQuery(criteria, pageable);
    }

    private static void addQueryBuilder(final BoolQueryBuilder queryBuilder,
        final String property,
        final QueryBuilder query,
        final boolean not) {
        final boolean isNestedPath = property.contains(".");
        if (!not) {
            if (isNestedPath) {
                queryBuilder.must(QueryBuilders.nestedQuery(property.substring(0, property.lastIndexOf('.')),
                    QueryBuilders.boolQuery().must(query),
                    ScoreMode.Total));
            } else {
                queryBuilder.must(query);
            }
        } else {
            if (isNestedPath) {
                queryBuilder.mustNot(QueryBuilders.nestedQuery(property.substring(0, property.lastIndexOf('.')),
                    QueryBuilders.boolQuery().must(query),
                    ScoreMode.Total));
            } else {
                queryBuilder.mustNot(query);
            }
        }
    }
}
