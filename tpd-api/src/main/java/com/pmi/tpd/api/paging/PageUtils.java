package com.pmi.tpd.api.paging;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.pmi.tpd.api.util.FluentIterable.from;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.pmi.tpd.api.util.Assert;

/**
 * <p>
 * PageUtils class.
 * </p>
 *
 * @author Christophe Friederich
 * @since 1.0
 */
public class PageUtils {

    /** Constant <code>SUBPAGE_MULTIPLIER=2</code>. */
    public static final int SUBPAGE_MULTIPLIER = 2;

    /**
     * <p>
     * Constructor for PageUtils.
     * </p>
     */
    protected PageUtils() {
        throw new UnsupportedOperationException(
                getClass().getName() + " is a utility class and should not be instantiated");
    }

    /**
     * Safely cast {@code page} to a page of type {@code <S>}, where {@code S} is a super type of {@code ST}.
     * <p/>
     * This method is for convenient casting of {@link IPage pages} holding values of particular subtype {@code S} to a
     * chosen supertype {@code ST} of {@code S}. For instance, given a {@code Page<Integer>}, this method can be used to
     * conveniently obtain a reference to the same page instance as a {@code Page<Number>}:
     *
     * <pre>
     * {@code
     *     Page<Integer> ints = // ...
     *     Page<Number> numbers = PageUtils.asPageOf(Number.class, ints);
     * }
     * </pre>
     * <p/>
     * NOTE: this method enforces compile-type safety only and will not raise an error if the supplied page contains
     * values that are not assignable to the requested resulting type. As an example, if the {@code Page<Number>}
     * contained {@code String} values (e.g. as a result of unchecked assignments), this method will still return a
     * reference to {@link com.pmi.tpd.core.paging.IPage} of requested component type {@code Number}, even though the
     * values are not compatible with it.
     * <p/>
     * This method does not iterate over the values of the supplied {@code page} or modify it in any other way, and it
     * <i>always</i> returns a reference to the supplied {@code page} instance.
     *
     * @param superType
     *                  super type to coerce page component type to; may not be {@literal null}
     * @param page
     *                  page reference to coerce, can be {@literal null}, in which case {@literal null} will be returned
     * @param <S>
     *                  super type parameter
     * @return the same {@code page} instance, as a page of type {@code <S>}, {@literal null} will be returned if
     *         {@code page} was {@literal null}.
     * @since 1.0
     * @param <ST>
     *             a ST object.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <S, ST extends S> Page<S> asPageOf(@Nonnull final Class<S> superType, @Nullable final Page<ST> page) {
        checkNotNull(superType, "superType");
        return (Page<S>) page;
    }

    /**
     * Create a paged iterable associated to {@link IPageProvider provider}.
     *
     * @param provider
     *                 the provider of page.
     * @param pageSize
     *                 the size of page.
     * @return Returns a new instance of paged {@link Iterable}.
     * @param <T>
     *            the type of elements returned by the iterator
     * @see PagedIterable
     */
    @Nonnull
    public static <T> PagedIterable<T> asIterable(@Nonnull final IPageProvider<T> provider, final int pageSize) {
        return new PagedIterable<>(provider, pageSize);
    }

    /**
     * Create a paged iterable associated to {@link IPageProvider provider}.
     *
     * @param provider
     *                 the provider of page.
     * @param request
     *                 a page request.
     * @return Returns a new instance of paged {@link Iterable}.
     * @param <T>
     *            the type of elements returned by the iterator
     * @see PagedIterable
     */
    @Nonnull
    public static <T> PagedIterable<T> asIterable(@Nonnull final IPageProvider<T> provider, final Pageable request) {
        return new PagedIterable<>(provider, request);
    }

    /**
     * Create a stream associated to {@link IPageProvider provider}.
     *
     * @param provider
     *                 the provider of page.
     * @param request
     *                 a page request.
     * @return Returns a new instance of paged {@link Stream}.
     * @param <T>
     *            the type of elements returned by the stream.
     * @see PagedIterable
     */
    @Nonnull
    public static <T> Stream<T> asStream(@Nonnull final IPageProvider<T> provider, final @Nonnull Pageable request) {
        return asStream(provider, request, -1);
    }

    @Nonnull
    public static <T> Stream<T> asStream(@Nonnull final IPageProvider<T> provider,
        final @Nonnull Pageable request,
        final long maxElement) {
        return Streams.stream(new PagedIterable<>(provider, request).setLimit(maxElement));
    }

    /**
     * <p>
     * createEmptyPage.
     * </p>
     *
     * @param pageRequest
     *                    a {@link Pageable} object.
     * @param <T>
     *                    a T object.
     * @return a {@link Page} object.
     */
    @Nonnull
    public static <T> Page<T> createEmptyPage(@Nonnull final Pageable pageRequest) {
        return createPage(Collections.<T> emptyList(), pageRequest);
    }

    /**
     * @param elements
     * @param request
     * @return
     */
    @Nonnull
    public static <T> Page<T> createPage(@Nonnull final Iterable<T> elements, @Nonnull final Pageable request) {
        final int size = Iterables.size(elements);
        return createPage(elements, request, size);
    }

    /**
     * <p>
     * createPage.
     * </p>
     *
     * @param elements
     *                 a {@link java.lang.Iterable} object.
     * @param request
     *                 a {@link Pageable} object.
     * @param <T>
     *                 a T object.
     * @return a {@link Page} object.
     */
    @Nonnull
    public static <T> Page<T> createPage(@Nonnull final Iterable<T> elements,
        @Nonnull final Pageable request,
        final long totalElement) {
        final int size = Iterables.size(elements);
        final boolean lastPage = size <= request.getPageSize();
        final int pageSize = lastPage ? size : request.getPageSize();

        return new PageImpl<>(from(elements).limit(pageSize).toList(), request, totalElement);
    }

    /**
     * <p>
     * createPage.
     * </p>
     *
     * @param request
     *                         a {@link Pageable} object.
     * @param elementGenerator
     *                         a {@link com.google.common.base.Function} object.
     * @param <T>
     *                         a T object.
     * @return a {@link Page} object.
     */
    @Nonnull
    public static <T> Page<T> createPage(@Nonnull final Pageable request,
        @Nonnull final Function<Long, T> elementGenerator,
        final long totalnumber) {
        final List<T> values = Lists.newArrayList();
        final int num = request.getPageSize() + 1;
        for (int i = 0; i < num; ++i) {
            final long index = request.getOffset() + i;
            final T element = elementGenerator.apply(index);
            if (element != null) {
                values.add(element);
            } else {
                break;
            }
        }
        return createPage(values, request, totalnumber);
    }

    /**
     * A utility method to create a new page request without having to explicitly choose an implementation class.
     *
     * @param page
     *              zero-based page index
     * @param limit
     *              the limit of the page request
     * @return the non-null page request
     */
    @Nonnull
    public static Pageable newRequest(final int page, final int limit) {
        return FilteredPagingRequest.builder().page(page).pageSize(limit).build();
    }

    /**
     * A utility method to create a new page request without having to explicitly choose an implementation class.
     *
     * @param page
     *              zero-based page index
     * @param limit
     *              the limit of the page request
     * @return the non-null page request
     */
    @Nonnull
    public static Pageable newRequest(final int page, final int limit, @Nullable final Sort sort) {
        return FilteredPagingRequest.builder().page(page).pageSize(limit).sort(sort).build();
    }

    /**
     * A utility method to create a new page request without having to explicitly choose an implementation class.
     *
     * @param page
     *              zero-based page index
     * @param limit
     *              the limit of the page request
     * @return the non-null page request
     */
    @Nullable
    @CheckReturnValue
    public static Pageable newRequest(final int page,
        final int limit,
        @Nullable final Sort sort,
        @Nullable final Filters filters,
        @Nullable final String predicate) {
        if (page < 0 || limit < 1) {
            return null;
        }
        return FilteredPagingRequest.builder()
                .page(page)
                .pageSize(limit)
                .sort(sort)
                .filters(filters)
                .query(predicate)
                .build();
    }

    /**
     * A utility method to create a new page request without having to explicitly choose an implementation class.
     *
     * @param page
     *              zero-based page index
     * @param limit
     *              the limit of the page request
     * @return the non-null page request
     */
    @Nullable
    @CheckReturnValue
    public static Pageable newRequest(final int page,
        final int limit,
        @Nullable final Sort sort,
        @Nullable final Filter<?>... filters) {
        return newRequest(page, limit, sort, new Filters(filters), null);
    }

    /**
     * A utility method to create a new page request without having to explicitly choose an implementation class.
     *
     * @param page
     *              zero-based page index
     * @param limit
     *              the limit of the page request
     * @return the non-null page request
     */
    @Nonnull
    @CheckReturnValue
    public static Pageable newRequest(final int page,
        final int limit,
        @Nullable final String sort,
        @Nullable final String filters,
        @Nullable final String predicate) {
        Assert.state(page >= 0 && limit >= 1);
        return FilteredPagingRequest.builder()
                .page(page)
                .pageSize(limit)
                .sort(com.pmi.tpd.api.paging.PageRequest.buildSort(sort))
                .filters(com.pmi.tpd.api.paging.PageRequest.buildFilter(filters))
                .query(predicate)
                .build();
    }

    /**
     * Creates a new {@link PageRequest} for the first page (page number {@code 0}) given {@code pageSize} .
     *
     * @param pageSize
     *                 the size of the page to be returned, must be greater than 0.
     * @return a new {@link PageRequest}.
     */
    public static PageRequest ofSize(final int pageSize) {
        return FilteredPagingRequest.builder().page(0).pageSize(pageSize).build();
    }

    /**
     * A utility method to reduce the size of a page.
     *
     * @param page
     *                the original page
     * @param limit
     *                the new limit of the page (must be lower than the original page's limit)
     * @param request
     *                the original page request
     * @return the non-null page request
     * @param <T>
     *            a T object.
     */
    @Nonnull
    public static <T> Page<T> reducePage(@Nonnull final Page<T> page,
        final int limit,
        @Nonnull final Pageable request) {
        checkArgument(request.getPageSize() >= limit);
        return new PageImpl<>(from(page.getContent()).limit(limit).toList(), buildRestrictedPageRequest(request, limit),
                page.getTotalElements());
    }

    /**
     * Create a restricted page request.
     *
     * @param request
     *                 a base request to use.
     * @param maxLimit
     *                 a limit of element.
     * @return Create a new instance of {@link Pageable} representing a restricted page request.
     */
    public static Pageable buildRestrictedPageRequest(final Pageable request, final int maxLimit) {
        return PageRequest.of(request.getPageNumber(), Math.min(request.getPageSize(), maxLimit));
    }

    /**
     * Get a ordinal map indexed.
     *
     * @param page
     *             the page to use.
     * @return Returns a {@link SortedMap} representing a ordinal map.
     * @param <T>
     *            the type of elements returned by the map.
     */
    public static <T> SortedMap<Integer, T> getOrdinalIndexedValues(final Page<T> page) {
        final SortedMap<Integer, T> valueMap = Maps.newTreeMap();
        int index = page.getNumber() * page.getSize();
        for (final T value : page.getContent()) {
            valueMap.put(index++, value);
        }
        return valueMap;
    }

}
