package com.pmi.tpd.api.paging;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.pmi.tpd.api.paging.Filter.Operator;
import com.pmi.tpd.api.util.Assert;
import com.pmi.tpd.api.util.BuilderSupport;

/**
 * <p>
 * FilteredPagingRequest class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
@Immutable
public class FilteredPagingRequest extends PageRequest implements IFilterable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** */
    private final String query;

    /** */
    private final Filters filters;

    /** */
    @Nonnull
    private final Map<String, Filters> filterMap;

    /**
     * <p>
     * Constructor for FilteredPagingRequest.
     * </p>
     *
     * @param builder
     *                a {@link com.pmi.tpd.api.paging.FilteredPagingRequest.Builder} object.
     */
    protected FilteredPagingRequest(final Builder builder) {
        super(builder.page <= 0 ? 0 : builder.page,
                builder.pageSize == PAGING_NOT_DEFINED ? PAGING_NOT_DEFINED
                        : builder.pageSize <= 0 ? 1 : builder.pageSize,
                builder.sort == null ? Sort.unsorted() : builder.sort);

        Assert.isTrue(this.getPageSize() <= MAX_PAGE_LIMIT, "Limit must be less than " + MAX_PAGE_LIMIT);

        this.filters = builder.filters;
        if (this.filters != null) {
            this.filterMap = this.filters.stream()
                    .collect(Collectors.groupingBy(Filter::getProperty, Collectors.toCollection(Filters::new)));
        } else {
            this.filterMap = ImmutableMap.of();

        }

        this.query = builder.query;
    }

    /** {@inheritDoc} */
    @Override
    public String getQuery() {
        return query;
    }

    /** {@inheritDoc} */
    @Override
    public @Nonnull Map<String, Filters> getFilterMap() {
        return filterMap;
    }

    public @Nonnull Filters getFilters() {
        return filters;
    }

    /** {@inheritDoc} */
    @Override
    public Iterable<Filter<?>> getFilter(final @Nullable String propertyName) {
        if (filters == null || Strings.isNullOrEmpty(propertyName)) {
            return ImmutableList.of();
        }
        return this.filterMap.get(propertyName);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasFilter() {
        if (filters == null) {
            return false;
        }
        return filters.size() > 0;
    }

    /**
     * <p>
     * copy.
     * </p>
     *
     * @return a {@link com.pmi.tpd.api.paging.FilteredPagingRequest.Builder} object.
     */
    public Builder copy() {
        return new Builder(this);
    }

    @Override
    public PageRequest withPage(final int pageNumber) {
        return copy().page(pageNumber).build();
    }

    @Override
    public PageRequest next() {
        return copy().page(getPageNumber() + 1).build();
    }

    @Override
    public PageRequest previous() {
        return getPageNumber() == 0 ? this : copy().page(getPageNumber() - 1).build();
    }

    @Override
    public PageRequest first() {
        return copy().page(0).build();
    }

    @Override
    public PageRequest withSort(final Direction direction, final String... properties) {
        return copy().sort(Sort.by(direction, properties)).build();
    }

    @Override
    public PageRequest withSort(final Sort sort) {
        return copy().sort(sort).build();
    }

    /**
     * <p>
     * builder.
     * </p>
     *
     * @return a {@link com.pmi.tpd.api.paging.FilteredPagingRequest.Builder} object.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author Christophe Friederich
     */
    public static class Builder extends BuilderSupport {

        /** */
        private int page = 0;

        /** */
        private int pageSize = PAGING_NOT_DEFINED;

        /** */
        private String query;

        /** */
        @Nonnull
        private final Filters filters;

        /** */
        private Sort sort;

        /**
         * Default constructor.
         */
        protected Builder() {
            filters = new Filters();
        }

        /**
         * @param request
         *                page request to use.
         */
        protected Builder(final FilteredPagingRequest request) {
            page = request.getPageNumber();
            pageSize = request.getPageSize();
            query = request.query;
            filters = new Filters(
                    request.filterMap.values().stream().flatMap(List::stream).collect(Collectors.toList()));
            sort = request.getSort();
        }

        /**
         * @param value
         *              zero-based page index.
         * @return Returns fluent {@link Builder}.
         */
        public Builder page(final int value) {
            this.page = value;
            return this;
        }

        /**
         * @param value
         *              the size of the page to be returned.
         * @return Returns fluent {@link Builder}.
         */
        public Builder pageSize(final int value) {
            this.pageSize = value;
            return this;
        }

        /**
         * @param value
         *              a predicate query.
         * @return Returns fluent {@link Builder}.
         */
        public Builder query(final String value) {
            this.query = value;
            return this;
        }

        /**
         * @param property
         *                  the property to sort (can <b>not</b> be {@code null} or empty).
         * @param direction
         *                  the direction of sort (can be {@code null}. Use {@link Sort#DEFAULT_DIRECTION} by default.
         * @return Returns fluent {@link Builder}.
         */
        public Builder sort(@Nonnull final String property, @Nullable final Direction direction) {
            return sort(Sort.by(new Order(direction, property)));
        }

        /**
         * @param orders
         *               list of order (can <b>not</b> be null).
         * @return Returns fluent {@link Builder}.
         */
        public Builder sort(@Nonnull final Order... orders) {
            return sort(Sort.by(orders));
        }

        /**
         * @param value
         *              Sort option for queries.
         * @return Returns fluent {@link Builder}.
         */
        public Builder sort(@Nullable final Sort value) {
            if (this.sort == null) {
                this.sort = value;
            } else {
                this.sort = this.sort.and(value);
            }
            return this;
        }

        /**
         * @param property
         *                 the property to filter (can <b>not</b> be {@code null} or empty).
         * @param value
         *                 the filter value used to match.
         * @param operator
         *                 the type of operator used for matching (can be {@code null}). Use {@link Operator#eq} by
         *                 default.
         * @return Returns fluent {@link Builder}.
         * @see Filter
         */
        @Nonnull
        public Builder filter(@Nonnull final String property,
            @Nullable final Object value,
            @Nullable final Operator operator) {
            return filters(new Filter<>(property, value, operator));
        }

        /**
         * @param property
         *                 the property to filter (can <b>not</b> be {@code null} or empty).
         * @param value
         *                 the filter value used to match.
         * @return Returns fluent {@link Builder}.
         * @see Filter
         */
        @Nonnull
        public Builder filter(@Nonnull final String property, final Object value) {
            return filters(new Filter<>(property, value));
        }

        /**
         * @param value
         *              a filter to add (can be {@code null}).
         * @return Returns fluent {@link Builder}.
         * @see Filter
         */
        @Nonnull
        public Builder filter(@Nullable final Filter<?> value) {
            return filters(value);
        }

        /**
         * @param values
         *               a list of filters to add (can be {@code null}).
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder filters(@Nullable final Filters values) {
            addIf(Predicates.<Filter<?>> notNull(), filters, values);
            return this;
        }

        /**
         * @param value
         *               the first filter to add (can be {@code null}).
         * @param values
         *               a varargs array containing 0 or more filters to add after the first (can be {@code null}).
         * @return Returns fluent {@link Builder}.
         */
        @Nonnull
        public Builder filters(@Nullable final Filter<?> value, @Nullable final Filter<?>... values) {
            addIf(Predicates.<Filter<?>> notNull(), filters, value, values);
            return this;
        }

        /**
         * @return Returns new filled instance of {@link FilteredPagingRequest}.
         */
        @Nonnull
        public FilteredPagingRequest build() {
            return new FilteredPagingRequest(this);
        }
    }

}
