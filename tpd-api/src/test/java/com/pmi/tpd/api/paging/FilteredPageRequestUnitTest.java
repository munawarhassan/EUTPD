package com.pmi.tpd.api.paging;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

class FilteredPageRequestUnitTest extends AbstractPageRequestUnitTest {

    @Override
    public Pageable newPageRequest(final int page, final int size) {
        return PageUtils.newRequest(page, size);
    }

    @Test
    void equalsRegardsSortCorrectly() {

        final var sort = Sort.by(Direction.DESC, "foo");
        final AbstractPageRequest request = PageUtils.ofSize(10).withPage(1).withSort(sort);

        // Equals itself
        assertEqualsAndHashcode(request, request);

        // Equals another instance with same setup
        assertEqualsAndHashcode(request, PageUtils.newRequest(1, 10, sort));

        // Equals another instance with same sort by properties
        assertEqualsAndHashcode(request, PageUtils.ofSize(10).withPage(1).withSort(Direction.DESC, "foo"));

        // Equals without sort entirely
        assertEqualsAndHashcode(PageUtils.newRequest(0, 10), PageUtils.newRequest(0, 10));

        // Is not equal to instance without sort
        assertNotEqualsAndHashcode(request, PageUtils.newRequest(1, 10));

        // Is not equal to instance with another sort
        assertNotEqualsAndHashcode(request, PageUtils.newRequest(1, 10, Sort.by(Direction.ASC, "foo")));
    }

}
