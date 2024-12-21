package com.pmi.tpd.api.paging;

import javax.annotation.Nonnull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <p>
 * Allows to provide a page corresponding to page request.
 * </p>
 *
 * @param <T>
 *            a object type.
 * @author Christophe Friederich
 * @since 1.0
 */
@FunctionalInterface
public interface IPageProvider<T> {

    /**
     * Gets the Page corresponding to the request.
     *
     * @param request
     *            a {@link Pageable} object.
     * @return a {@link Page} object.
     */
    @Nonnull
    Page<T> get(@Nonnull Pageable request);
}
