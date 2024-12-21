package com.pmi.tpd.api.paging;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author devacfr<christophefriederich@mac.com>
 * @since 2.0
 * @param <T>
 */
public class ListPageProvider<T> implements IPageProvider<T> {

    /** */
    private final List<T> values;

    /**
     * @param values
     */
    public ListPageProvider(final List<T> values) {
        this.values = values;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nonnull Page<T> get(final @Nonnull Pageable request) {
        final List<T> slice = values.subList((int) request.getOffset(),
            (int) Math.min(request.getOffset() + request.getPageSize() + 1, values.size()));

        return PageUtils.createPage(slice, request, values.size());
    }
}
