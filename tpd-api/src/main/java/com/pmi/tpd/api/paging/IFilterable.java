package com.pmi.tpd.api.paging;

import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface allowing extends default Spring implementation {@link org.springframework.data.domain.PageRequest
 * PageRequest} with filtering and predicate query.
 *
 * @author Christophe Friederich
 * @since 1.0
 * @see org.springframework.data.domain.PageRequest
 * @see FilteredPagingRequest
 */
public interface IFilterable {

    /** Constant <code>PAGING_NOT_DEFINED=Integer.MIN_VALUE</code>. */
    int PAGING_NOT_DEFINED = Integer.MIN_VALUE;

    /**
     * The maximum size of any page limit.
     */
    int MAX_PAGE_LIMIT = 1048576;

    /**
     * Gets the query used as predicate.
     *
     * @return Returns a {@link String} representing a predicate query or {@code null}.
     */
    @Nullable
    String getQuery();

    /**
     * Gets all filters containing in the page request.
     *
     * @return Returns a {@link List} of all filters, which may be empty but never {@code null}.
     */
    @Nonnull
    Map<String, Filters> getFilterMap();

    /**
     * Gets all filters containing in the page request.
     *
     * @return Returns a {@link List} of all filters, which may be empty but never {@code null}.
     */
    @Nonnull
    Filters getFilters();

    /**
     * Gets the {@link Filter} for specific property name.
     *
     * @param propertyName
     *                     a property name to use (can be {@code null}).
     * @return Returns the {@link Filter} for a specific property name if exists, otherwise {@code null}.
     */
    @Nullable
    @CheckReturnValue
    Iterable<Filter<?>> getFilter(@Nullable String propertyName);

    /**
     * Gets indicating whether the page request contains {@link Filter}.
     *
     * @return Returns {@code true} whether the page request contains {@link Filter}, otherwise {@code false}.
     */
    boolean hasFilter();

}
