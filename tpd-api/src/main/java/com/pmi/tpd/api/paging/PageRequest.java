package com.pmi.tpd.api.paging;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.pmi.tpd.api.paging.Filter.Operator;

/**
 * @author Christophe Friederich
 * @since 1.0
 */
public class PageRequest {

    /** [property]::[operator]==[value(s)] */
    private static final Pattern FILTER_PATTERN = Pattern.compile("(.*)::(.*)==(.*)");

    /** */
    private int page;

    /** */
    private int size;

    /** */
    private Sort sort;

    /** */
    private Filters filters;

    /** */
    private String query;

    /**
     * @param json
     *             JSON representing {@link Pageable} object,
     * @return
     * @throws JsonParseException
     *                              if underlying input contains invalid content of type {@link JsonParser} supports
     *                              (JSON for default case)
     * @throws JsonMappingException
     *                              if the input JSON structure does not match structure expected for result type (or
     *                              has other mismatch issues)
     * @throws IOException
     *                              if a low-level I/O problem (unexpected end-of-input, network error) occurs (passed
     *                              through as-is without additional wrapping -- note that this is one case where
     *                              {@link DeserializationFeature#WRAP_EXCEPTIONS} does NOT result in wrapping of
     *                              exception even if enabled)
     */
    @Nullable
    @CheckReturnValue
    public static Pageable toPageable(final String json) throws JsonParseException, JsonMappingException, IOException {
        final ObjectMapper mapper = new ObjectMapper();
        if (json == null) {
            return null;
        }
        final PageRequest request = mapper.readValue(json, PageRequest.class);
        if (request == null) {
            return null;
        }
        return request.toPageable();
    }

    /**
     * Default constructor.
     */
    /* package */ PageRequest() {

    }

    /**
     * @param page
     *             zero-based page index.
     * @param size
     *             the size of the page to be returned.
     */
    /* package */ PageRequest(final int page, final int size) {
        this(page, size, null, null, null);
    };

    /**
     * @param page
     *                  zero-based page index.
     * @param size
     *                  the size of the page to be returned.
     * @param sort
     *                  the property to sort (can be {@literal null}).
     * @param filtering
     *                  the list of filter properties to use (can be {@literal null}).
     */
    /* package */ PageRequest(final int page, final int size, final String sort, final String filtering) {
        this(page, size, sort, filtering, null);
    };

    /**
     * @param page
     *                  zero-based page index.
     * @param size
     *                  the size of the page to be returned.
     * @param sort
     *                  the property to sort (can be {@literal null}).
     * @param filtering
     *                  the list of filter properties to use (can be {@literal null}).
     * @param query
     *                  can be {@literal null}.
     */
    /* package */ PageRequest(final int page, final int size, final String sort, final String filtering,
            final String query) {
        this.page = page;
        this.size = size;
        this.filters = buildFilter(filtering);
        this.sort = buildSort(sort);
        this.query = query;
    };

    /**
     * @return
     */
    public int getPage() {
        return page;
    }

    /**
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * @return
     */
    @Nonnull
    public Sort getSort() {
        return sort;
    }

    /**
     * @return
     */
    public Filters getFilters() {
        return filters;
    }

    /**
     * @return
     */
    public String getQuery() {
        return query;
    }

    @Nonnull
    public Pageable toPageable() {
        return PageUtils.newRequest(getPage(), getSize(), getSort(), getFilters(), getQuery());
    }

    public static Filters buildFilter(final String value) {

        if (Strings.isNullOrEmpty(value)) {
            return Filters.empty();
        }
        final String decodedFilter = URLDecoder.decode(value, Charsets.UTF_8);
        final Filters filters = new Filters();
        final String[] ar = decodedFilter.split("\\|");
        for (final String filter : ar) {
            final Matcher matcher = FILTER_PATTERN.matcher(filter);
            if (!matcher.matches()) {
                continue;
            }
            final String property = matcher.group(1);
            final String operator = matcher.group(2);
            final String val = matcher.group(3);
            List<String> values = null;
            if (val != null) {
                values = Arrays.asList(val.split(","));
            }
            filters.add(new Filter<String>(property, values, Operator.from(operator)));
        }
        return filters;
    }

    public static Sort buildSort(final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return Sort.unsorted();
        }
        final String decodedSort = URLDecoder.decode(value, Charsets.UTF_8);
        final String[] ar = decodedSort.split("\\|");
        final List<Sort.Order> orders = Lists.newArrayList();
        for (final String prop : ar) {
            if (Strings.isNullOrEmpty(prop)) {
                continue;
            }
            boolean ignoreCase = false;
            char ch = prop.charAt(0);
            int count = 0;
            if (ch == '!') {
                ignoreCase = true;
                ch = prop.charAt(1);
                count++;
            }
            Direction direction = Direction.ASC;
            if (ch == '-') {
                count++;
                direction = Direction.DESC;
            } else if (ch == '+') {
                count++;
                direction = Direction.ASC;
            }
            final String property = prop.substring(count);
            Sort.Order order = new Sort.Order(direction, property);
            if (ignoreCase) {
                order = order.ignoreCase();
            }
            orders.add(order);
        }
        return Sort.by(orders);
    }

}
